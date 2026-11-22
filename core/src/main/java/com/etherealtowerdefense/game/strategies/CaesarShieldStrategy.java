
package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.effects.SplashIndicatorManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CaesarShieldStrategy implements AttackStrategy {

    private enum Phase { IDLE, WINDUP, SPIN }

    private final SplashIndicatorManager splashIndicators;
    private final float fireInterval;
    private final float damage;
    private final float splashRadius;
    private final float windupDuration;
    private final float windupAngle;
    private final float spinDuration;
    private final float totalSpinDegrees;
    private final float knockbackStrength;

    private final float normalScale = 1f;
    private final float maxScale = 1.25f;
    private final float growDuration = 0.15f;

    private final Set<Enemy> hitThisAttack = new HashSet<>();
    private final Vector2 knockbackDirection = new Vector2();

    private Phase phase = Phase.IDLE;
    private Enemy target;
    private float cooldown;
    private float phaseElapsed;
    private float spinDirection = -1f;
    private float windupStartRotation;
    private float spinStartRotation;

    public CaesarShieldStrategy(SplashIndicatorManager splashIndicators) {
        this(splashIndicators, 1.8f, 35f, 95f, 0.35f, 30f, 1f, 1440f, 260f);
    }

    public CaesarShieldStrategy(
        SplashIndicatorManager splashIndicators,
        float fireInterval,
        float damage,
        float splashRadius,
        float windupDuration,
        float windupAngle,
        float spinDuration,
        float totalSpinDegrees,
        float knockbackStrength
    ) {
        this.splashIndicators = splashIndicators;
        this.fireInterval = fireInterval;
        this.damage = damage;
        this.splashRadius = splashRadius;
        this.windupDuration = windupDuration;
        this.windupAngle = windupAngle;
        this.spinDuration = spinDuration;
        this.totalSpinDegrees = totalSpinDegrees;
        this.knockbackStrength = knockbackStrength;
    }

    @Override
    public boolean usesCustomUpdate() {
        return true;
    }

    @Override
    public void update(Tower tower, List<Enemy> enemies, float delta) {
        cooldown = Math.max(0f, cooldown - delta);

        if (phase == Phase.IDLE) {
            tower.setVisualScale(normalScale);

            target = findTarget(tower, enemies);

            if (target != null && cooldown <= 0f) {
                beginAttack(tower, target);
            }
            return;
        }

        phaseElapsed += delta;

        if (phase == Phase.WINDUP) {
            float progress = MathUtils.clamp(
                phaseElapsed / windupDuration, 0f, 1f
            );

            float eased = progress * progress * (3f - 2f * progress);

            float windupRotation =
                windupStartRotation - spinDirection * windupAngle * eased;

            tower.setRotation(windupRotation);
            tower.setVisualScale(MathUtils.lerp(1f, 0.95f, eased));

            if (progress >= 1f) {
                phase = Phase.SPIN;
                phaseElapsed = 0f;

                spinStartRotation = windupRotation;

                splashIndicators.spawn(tower.getPosition(), splashRadius);
                hitThisAttack.clear();
            }

            return;
        }

        if (phase == Phase.SPIN) {
            float progress = MathUtils.clamp(
                phaseElapsed / spinDuration, 0f, 1f
            );

            float eased = asymmetricSpinEase(progress, 0.35f);

            float compensatedSpinDegrees = totalSpinDegrees + windupAngle;

            tower.setRotation(
                spinStartRotation + spinDirection * compensatedSpinDegrees * eased
            );

            float growProgress = growDuration / spinDuration;

            if (progress < growProgress) {
                float growth = progress / growProgress;
                float growEased = 1f - (1f - growth) * (1f - growth);

                tower.setVisualScale(
                    MathUtils.lerp(0.95f, maxScale, growEased)
                );
            } else {
                float shrinkProgress =
                    (progress - growProgress) / (1f - growProgress);

                float shrinkEased = shrinkProgress * shrinkProgress;

                tower.setVisualScale(
                    MathUtils.lerp(maxScale, normalScale, shrinkEased)
                );
            }

            if (progress <= 0.2f) {
                damageNearbyEnemies(tower, enemies);
            }

            if (progress >= 1f) {
                phase = Phase.IDLE;
                phaseElapsed = 0f;
                tower.setVisualScale(normalScale);
                target = null;
            }
        }
    }

    private void beginAttack(Tower tower, Enemy selectedTarget) {
        target = selectedTarget;
        spinDirection = determineSpinDirection(tower, target);
        windupStartRotation = tower.getRotation();
        phaseElapsed = 0f;
        phase = Phase.WINDUP;
        cooldown = fireInterval;
    }

    private float determineSpinDirection(Tower tower, Enemy enemy) {
        Vector2 velocity = enemy.getVelocity();

        if (velocity.isZero(0.0001f)) {
            velocity.set(enemy.getLastMovementDirection());
        }

        Vector2 relativePosition =
            new Vector2(enemy.getPosition()).sub(tower.getPosition());

        float cross =
            velocity.x * relativePosition.y -
                velocity.y * relativePosition.x;

        if (cross < -0.0001f) return -1f;
        if (cross > 0.0001f) return 1f;

        return spinDirection;
    }

    private float asymmetricSpinEase(float progress, float accelerationShare) {
        if (progress <= accelerationShare) {
            float t = progress / accelerationShare;
            return 0.5f * t * t;
        }

        float t = (progress - accelerationShare) / (1f - accelerationShare);

        return 1f - 0.5f * (1f - t) * (1f - t);
    }

    private void damageNearbyEnemies(Tower tower, List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (!enemy.isActive() || hitThisAttack.contains(enemy)) continue;

            if (enemy.getPosition().dst(tower.getPosition()) > splashRadius) {
                continue;
            }

            enemy.damage(damage);
            hitThisAttack.add(enemy);

            if (!enemy.isBoss()) {
                knockbackDirection
                    .set(enemy.getVelocity())
                    .nor()
                    .scl(-1f);

                if (knockbackDirection.isZero(0.0001f)) {
                    knockbackDirection
                        .set(enemy.getLastMovementDirection())
                        .nor()
                        .scl(-1f);
                }

                enemy.applyKnockback(knockbackDirection, knockbackStrength);
            }
        }
    }

    private Enemy findTarget(Tower tower, List<Enemy> enemies) {
        Enemy best = null;
        float bestValue = 0f;
        for (Enemy enemy : enemies) {
            if (!enemy.isActive() || tower.getPosition().dst(enemy.getPosition()) > tower.getRange()) continue;
            float value;
            switch (tower.getTargetPriority()) {
                case LAST: value = -enemy.getDistanceTravelled(); break;
                case CLOSE: value = -tower.getPosition().dst2(enemy.getPosition()); break;
                case STRONG: value = enemy.getHealth(); break;
                case FIRST:
                default: value = enemy.getDistanceTravelled(); break;
            }
            if (best == null || value > bestValue) { best = enemy; bestValue = value; }
        }
        return best;
    }

    @Override
    public void fire(Tower tower, Enemy target) {
        // Caesar Shield starts its attack from update().
    }

    @Override
    public Float getDamage() { return damage; }

    @Override
    public float getFireInterval() {
        return fireInterval;
    }
}
