package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;
import java.util.List;

public class CannonRotorStrategy implements AttackStrategy {
    private static final float INITIAL_ACQUISITION_DELAY = 0.8f;
    private static final float BETWEEN_BURSTS_DELAY = 1.6f;
    private static final float BURST_INTERVAL = 0.18f;
    private static final int BURST_SIZE = 4;
    private static final float PROJECTILE_SPEED = 750f;
    private static final float BARREL_OFFSET = 10f;

    private final ProjectilePool pool;
    private final Texture projectile;
    private Enemy lockedTarget;
    private float acquisitionTimer;
    private float burstTimer;
    private int shotsFired;
    private boolean firingBurst;
    private boolean firstTargetAcquired;
    private int barrel = 1;

    public CannonRotorStrategy(ProjectilePool pool, Texture projectile) {
        this.pool = pool;
        this.projectile = projectile;
    }

    @Override public void fire(Tower tower, Enemy target) { /* update() owns firing */ }
    @Override public boolean usesCustomUpdate() { return true; }

    @Override
    public void update(Tower tower, List<Enemy> enemies, float delta) {
        if (firingBurst) {
            // Keep the burst locked to its original target. A dead or out-of-range
            // target cancels the remaining shots and starts a fresh 1.6s cooldown.
            if (!isValidTarget(tower, lockedTarget)) {
                cancelBurst();
                acquisitionTimer = BETWEEN_BURSTS_DELAY;
                return;
            }
        } else {
            // During cooldown, continue tracking a valid target so the rotor faces it.
            lockedTarget = findTarget(tower, enemies);
            if (lockedTarget == null) {
                acquisitionTimer = Math.max(0f, acquisitionTimer - delta);
                return;
            }

            if (!firstTargetAcquired) {
                firstTargetAcquired = true;
                acquisitionTimer = INITIAL_ACQUISITION_DELAY;
            }
        }

        Vector2 direction = new Vector2(lockedTarget.getPosition()).sub(tower.getPosition());
        if (direction.isZero()) return;

        float desiredRotation = direction.angleDeg() + 90f;
        float difference = angleDifference(tower.getRotation(), desiredRotation);
        float maxTurn = 360f * delta;
        tower.setRotation(tower.getRotation() + MathUtils.clamp(difference, -maxTurn, maxTurn));
        boolean aimed = Math.abs(angleDifference(tower.getRotation(), desiredRotation)) <= 2f;

        if (!firingBurst) {
            acquisitionTimer = Math.max(0f, acquisitionTimer - delta);
            if (acquisitionTimer > 0f || !aimed) return;

            firingBurst = true;
            shotsFired = 0;
            burstTimer = 0f;
        }

        burstTimer -= delta;
        while (firingBurst && burstTimer <= 0f) {
            if (!isValidTarget(tower, lockedTarget)) {
                cancelBurst();
                acquisitionTimer = BETWEEN_BURSTS_DELAY;
                break;
            }

            fireProjectile(tower, lockedTarget, direction);
            shotsFired++;

            if (shotsFired >= BURST_SIZE) {
                firingBurst = false;
                lockedTarget = null;
                acquisitionTimer = BETWEEN_BURSTS_DELAY;
                burstTimer = 0f;
            } else {
                burstTimer += BURST_INTERVAL;
            }
        }
    }

    private boolean isValidTarget(Tower tower, Enemy target) {
        return target != null && target.isActive()
            && tower.getPosition().dst(target.getPosition()) <= tower.getRange();
    }

    private void cancelBurst() {
        firingBurst = false;
        lockedTarget = null;
        shotsFired = 0;
        burstTimer = 0f;
    }

    private float angleDifference(float from, float to) {
        return ((to - from + 540f) % 360f) - 180f;
    }

    private void fireProjectile(Tower tower, Enemy target, Vector2 direction) {
        float perpendicular = direction.angleRad() + MathUtils.PI / 2f;
        float side = barrel > 0 ? BARREL_OFFSET : -BARREL_OFFSET;
        Vector2 spawn = new Vector2(tower.getPosition()).add(
            MathUtils.cos(perpendicular) * side,
            MathUtils.sin(perpendicular) * side
        );
        pool.obtain(projectile, spawn, target, PROJECTILE_SPEED, 37f);
        tower.triggerRecoil();
        barrel = -barrel;
    }

    private Enemy findTarget(Tower tower, List<Enemy> enemies) {
        Enemy best = null;
        float bestValue = 0f;
        for (Enemy enemy : enemies) {
            if (!isValidTarget(tower, enemy)) continue;
            float value;
            switch (tower.getTargetPriority()) {
                case LAST: value = -enemy.getDistanceTravelled(); break;
                case CLOSE: value = -tower.getPosition().dst2(enemy.getPosition()); break;
                case STRONG: value = enemy.getMaxHealth(); break;
                case FIRST:
                default: value = enemy.getDistanceTravelled(); break;
            }
            if (best == null || value > bestValue) {
                best = enemy;
                bestValue = value;
            }
        }
        return best;
    }

    @Override public Float getDamage() { return 37f; }

    // Effective per-projectile interval for the displayed DPS calculation.
    @Override public float getFireInterval() {
        return (BETWEEN_BURSTS_DELAY + BURST_INTERVAL * (BURST_SIZE - 1)) / BURST_SIZE;
    }
}
