package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.graphics.Texture;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;

/** Configurable fixed-destination splash attack used by the Mortar tower. */
public class MortarStrategy implements AttackStrategy {

    private final ProjectilePool pool;
    private final Texture projectile;
    private final float fireInterval;
    private final float damage;
    private final float projectileTravelSpeed;
    private final float splashRadius;
    private final float deadzoneRadius;

    public MortarStrategy(ProjectilePool pool, Texture projectile) {
        this(pool, projectile, 5f, 55f, 300f, 75f, 130f);
    }

    public MortarStrategy(
        ProjectilePool pool,
        Texture projectile,
        float fireInterval,
        float damage,
        float projectileTravelSpeed,
        float splashRadius,
        float deadzoneRadius
    ) {
        this.pool = pool;
        this.projectile = projectile;
        this.fireInterval = fireInterval;
        this.damage = damage;
        this.projectileTravelSpeed = projectileTravelSpeed;
        this.splashRadius = splashRadius;
        this.deadzoneRadius = deadzoneRadius;
    }

    @Override
    public void fire(Tower tower, Enemy target) {
        // Capture the target's position now; the shell does not track it afterward.
        pool.obtainMortar(
            projectile,
            tower.getPosition(),
            target.getPosition(),
            damage,
            splashRadius,
            projectileTravelSpeed
        );
    }

    @Override
    public boolean canTarget(Tower tower, Enemy enemy) {
        return tower.getPosition().dst(enemy.getPosition()) >= deadzoneRadius;
    }

    @Override
    public float getDeadzoneRadius() {
        return deadzoneRadius;
    }

    @Override
    public float getFireInterval() {
        return fireInterval;
    }
}
