package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Projectile {
    private final Vector2 position = new Vector2();
    private final Vector2 destination = new Vector2();
    private Enemy target;
    private Texture texture;
    private float speed, damage, splashRadius, travelDuration, progress;
    private float baseSize = 90f, lobScale = 1f, rotation;
    private float startX, startY;
    private boolean active, lobbed;

    public void init(Texture texture, Vector2 start, Enemy target, float speed, float damage) {
        this.texture = texture;
        position.set(start);
        this.target = target;
        this.speed = speed;
        this.damage = damage;
        splashRadius = 0f;
        progress = 0f;
        lobbed = false;
        rotation = 0f;
        active = true;
    }

    public void initMortar(Texture texture, Vector2 start, Vector2 impactPoint,
                           float damage, float splashRadius, float travelSpeed) {
        this.texture = texture;
        position.set(start);
        startX = start.x;
        startY = start.y;
        destination.set(impactPoint);
        target = null;
        this.damage = damage;
        this.splashRadius = splashRadius;
        float distance = start.dst(impactPoint);
        travelDuration = Math.max(0.18f, distance / travelSpeed);
        // Short shots have a deliberately smaller apparent arc, avoiding a huge shell on close targets.
        lobScale = Math.min(1f, distance / 260f);
        progress = 0f;
        rotation = 0f;
        lobbed = true;
        active = true;
    }

    public void update(float delta) {
        if (!active || lobbed) return;
        if (target == null || !target.isActive()) { active = false; return; }
        Vector2 direction = new Vector2(target.getPosition()).sub(position);
        if (!direction.isZero()) {
            // rotor_bullet.png points downward in its unrotated artwork (270 degrees).
            rotation = direction.angleDeg() - 270f;
        }
        float step = speed * delta;
        if (direction.len() <= step + 10f) {
            target.damage(damage);
            active = false;
        } else position.mulAdd(direction.nor(), step);
    }

    public boolean updateMortar(float delta, List<Enemy> enemies) {
        if (!active || !lobbed) return false;
        float averageFactor = 1f - 0.65f * (2f / (float)Math.PI);
        float speedFactor = 1f - 0.65f * (float)Math.sin(Math.PI * progress);
        progress = Math.min(1f, progress + (delta / travelDuration) * speedFactor / averageFactor);
        position.set(startX + (destination.x - startX) * progress,
                     startY + (destination.y - startY) * progress);
        if (progress >= 1f) {
            for (Enemy enemy : enemies) {
                if (enemy.isActive() && enemy.getPosition().dst(destination) <= splashRadius)
                    enemy.damage(damage);
            }
            active = false;
            return true;
        }
        return false;
    }

    public void draw(SpriteBatch batch) {
        if (!active) return;
        float size = baseSize;
        if (lobbed) size *= 1f + 1.35f * lobScale * (float)Math.sin(Math.PI * progress);
        batch.draw(
            new TextureRegion(texture),
            position.x - size / 2f,
            position.y - size / 2f,
            size / 2f,
            size / 2f,
            size,
            size,
            1f,
            1f,
            rotation
        );
    }

    public boolean isLobbed() {
        return lobbed;
    }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public Vector2 getImpactPosition() { return destination; }
    public float getSplashRadius() { return splashRadius; }
    public void setMortarStart(Vector2 start) { startX = start.x; startY = start.y; }
}
