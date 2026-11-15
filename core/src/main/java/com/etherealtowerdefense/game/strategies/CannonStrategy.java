
package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.graphics.Texture;

import com.etherealtowerdefense.game.entities.*;
import com.etherealtowerdefense.game.pools.ProjectilePool;

public class CannonStrategy implements AttackStrategy {

    private final ProjectilePool pool;
    private final Texture projectile;
    private final float interval;
    private final float damage;

    public CannonStrategy(ProjectilePool pool, Texture projectile) {
        this(pool, projectile, 0.8f, 25f);
    }

    public CannonStrategy(
        ProjectilePool pool,
        Texture projectile,
        float interval,
        float damage
    ) {
        this.pool = pool;
        this.projectile = projectile;
        this.interval = interval;
        this.damage = damage;
    }

    @Override
    public void fire(Tower tower, Enemy target) {
        tower.triggerRecoil();

        pool.obtain(
            projectile,
            tower.getPosition(),
            target,
            430f,
            damage
        );
    }

    @Override
    public float getFireInterval() {
        return interval;
    }
}
