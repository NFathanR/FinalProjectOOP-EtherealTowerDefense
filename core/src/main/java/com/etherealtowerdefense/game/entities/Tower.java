package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.strategies.AttackStrategy;
import java.util.List;
import com.badlogic.gdx.graphics.Color;

public class Tower {
    private final Vector2 position = new Vector2();
    private final Texture texture;
    private final float originX, originY, drawSize, range;
    private float rotation;
    private float visualScale = 1f;
    private float turnSpeed = 360f; // in degrees per second yall
    private static final float AIM_TOLERANCE_DEGREES = 2f;
    private final AttackStrategy strategy;
    private float cooldown;
    private static final float RECOIL_KICK_DURATION = 4f / 60f;
    private static final float RECOIL_RECOVERY_DURATION = 15f / 60f;
    private static final float RECOIL_DISTANCE = 6f;
    private static final float RECOIL_SCALE = 0.08f;
    private boolean stunned;

    private float recoilElapsed = -1f;

    public Tower(Texture texture, float originX, float originY, float drawSize,
                 float range, AttackStrategy strategy) {
        this.texture = texture;
        this.originX = originX;
        this.originY = originY;
        this.drawSize = drawSize;
        this.range = range;
        this.strategy = strategy;
    }

    public void place(float x, float y) { position.set(x, y); }

    public void update(float delta, List<Enemy> enemies) {
        if (stunned) return;

        if (strategy.usesCustomUpdate()) {
            strategy.update(this, enemies, delta);
            return;
        }
        cooldown -= delta;

        if (recoilElapsed >= 0f) {
            recoilElapsed += delta;

            if (recoilElapsed >= RECOIL_KICK_DURATION + RECOIL_RECOVERY_DURATION) {
                recoilElapsed = -1f;
            }
        }

        Enemy target = findTarget(enemies);
        if (target == null) return;

        float desiredRotation = target.getPosition().cpy().sub(position).angleDeg() + 90f;
        float angleDifference = ((desiredRotation - rotation + 540f) % 360f) - 180f;
        float maxTurn = turnSpeed * delta;
        rotation = MathUtils.lerpAngleDeg(rotation, desiredRotation,
                Math.abs(angleDifference) <= maxTurn ? 1f : maxTurn / Math.abs(angleDifference));

        float remainingAngle = ((desiredRotation - rotation + 540f) % 360f) - 180f;
        boolean isAimedAtTarget = Math.abs(remainingAngle) <= AIM_TOLERANCE_DEGREES;

        if (cooldown <= 0f && isAimedAtTarget) {
            strategy.fire(this, target);
            cooldown = strategy.getFireInterval();
        }
    }

    private Enemy findTarget(List<Enemy> enemies) {
        Enemy first = null;
        float greatestProgress = -1f;
        for (Enemy enemy : enemies) {
            if (!enemy.isActive() || position.dst(enemy.getPosition()) > range
                    || !strategy.canTarget(this, enemy)) continue;
            if (enemy.getDistanceTravelled() > greatestProgress) {
                greatestProgress = enemy.getDistanceTravelled();
                first = enemy;
            }
        }
        return first;
    }

    public void draw(SpriteBatch batch) {
        float recoil = getRecoilFactor();

        float animatedSize = drawSize * visualScale * (1f + RECOIL_SCALE * recoil);

        float ox = originX * animatedSize / 500f;
        float oy = originY * animatedSize / 500f;

        float recoilX = -MathUtils.sinDeg(rotation) * RECOIL_DISTANCE * recoil;
        float recoilY = MathUtils.cosDeg(rotation) * RECOIL_DISTANCE * recoil;

        Color previousColor = batch.getColor().cpy();

        if (stunned) {
            batch.setColor(0.45f, 0.65f, 1f, 1f);
        }

        batch.draw(
            texture,
            position.x + recoilX - ox,
            position.y + recoilY - oy,
            ox,
            oy,
            animatedSize,
            animatedSize,
            1f,
            1f,
            rotation,
            0,
            0,
            500,
            500,
            false,
            false
        );
        batch.setColor(previousColor);
    }

    private float getRecoilFactor() {
        if (recoilElapsed < 0f) {
            return 0f;
        }

        if (recoilElapsed <= RECOIL_KICK_DURATION) {
            return recoilElapsed / RECOIL_KICK_DURATION;
        }

        float recoveryProgress =
            (recoilElapsed - RECOIL_KICK_DURATION)
                / RECOIL_RECOVERY_DURATION;

        recoveryProgress = MathUtils.clamp(recoveryProgress, 0f, 1f);

        // Smoothly ease back to the original position.
        return 1f - recoveryProgress * recoveryProgress
            * (3f - 2f * recoveryProgress);
    }

    public void triggerRecoil() {
        recoilElapsed = 0f;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public boolean isStunned() {
        return stunned;
    }

    public Vector2 getPosition() { return position; }
    public float getRange() { return range; }
    public AttackStrategy getStrategy() { return strategy; }
    public float getCollisionRadius() { return 28f; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public float getRotation() { return rotation; }
    public void setVisualScale(float visualScale) { this.visualScale = visualScale; }
    public float getVisualScale() { return visualScale; }
    public void setTurnSpeed(float turnSpeed) { this.turnSpeed = turnSpeed; }
}
