package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.graphics.Texture;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;

/** Configurable homing-projectile attack used by the Cannon tower. */
public class CannonStrategy implements AttackStrategy {

    private final ProjectilePool pool;
    private final Texture projectile;
    private final float fireInterval;
    private final float damage;
    private final float projectileSpeed;

    public CannonStrategy(ProjectilePool pool, Texture projectile) {
        this(pool, projectile, 0.8f, 25f, 430f);
    }

    public CannonStrategy(
        ProjectilePool pool,
        Texture projectile,
        float fireInterval,
        float damage,
        float projectileSpeed
    ) {
        this.pool = pool;
        this.projectile = projectile;
        this.fireInterval = fireInterval;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
    }

    @Override
    public void fire(Tower tower, Enemy target) {
        tower.triggerRecoil();
        pool.obtain(projectile, tower.getPosition(), target, projectileSpeed, damage);
    }

    @Override
    public Float getDamage() { return damage; }

    @Override
    public float getFireInterval() {
        return fireInterval;
    }
}
