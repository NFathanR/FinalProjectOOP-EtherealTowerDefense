package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

/** Reusable projectile for homing cannon shots and fixed-destination mortar shells. */
public class Projectile {
    private final Vector2 position = new Vector2();
    private final Vector2 destination = new Vector2();
    private Enemy target;
    private Texture texture;
    private float speed, damage, splashRadius, travelDuration, progress;
    private float baseSize = 90f, lobScale = 1f;
    private float startX, startY;
    private float impactVisualTime;
    private boolean active, lobbed, showingImpact;

    public void init(Texture texture, Vector2 start, Enemy target, float speed, float damage) {
        this.texture = texture;
        position.set(start);
        this.target = target;
        this.speed = speed;
        this.damage = damage;
        splashRadius = 0f;
        progress = 0f;
        lobbed = false;
        showingImpact = false;
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
        lobbed = true;
        showingImpact = false;
        impactVisualTime = 0f;
        active = true;
    }

    public void update(float delta) {
        if (!active || lobbed || showingImpact) return;
        if (target == null || !target.isActive()) { active = false; return; }
        Vector2 direction = new Vector2(target.getPosition()).sub(position);
        float step = speed * delta;
        if (direction.len() <= step + 10f) {
            target.damage(damage);
            active = false;
        } else position.mulAdd(direction.nor(), step);
    }

    public void updateMortar(float delta, List<Enemy> enemies) {
        if (!active || !lobbed) return;
        if (showingImpact) {
            impactVisualTime -= delta;
            if (impactVisualTime <= 0f) active = false;
            return;
        }
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
            showingImpact = true;
            impactVisualTime = 0.38f;
        }
    }

    public void draw(SpriteBatch batch) {
        if (!active || showingImpact) return;
        float size = baseSize;
        if (lobbed) size *= 1f + 1.35f * lobScale * (float)Math.sin(Math.PI * progress);
        batch.draw(texture, position.x - size / 2f, position.y - size / 2f, size, size);
    }

    /** Draws a temporary expanding/fading splash-radius indicator at the impact point. */
    public void drawImpact(ShapeRenderer shapes) {
        if (!active || !showingImpact) return;
        float alpha = Math.max(0f, impactVisualTime / 0.38f);
        float expansion = 1f + (1f - alpha) * 0.12f;
        shapes.setColor(1f, 0.58f, 0.12f, alpha);
        shapes.circle(destination.x, destination.y, splashRadius * expansion, 48);
    }

    public boolean isLobbed() {
        return lobbed;
    }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public void setMortarStart(Vector2 start) { startX = start.x; startY = start.y; }
}
