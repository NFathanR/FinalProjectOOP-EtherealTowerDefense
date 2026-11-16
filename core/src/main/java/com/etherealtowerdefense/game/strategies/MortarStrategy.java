package com.etherealtowerdefense.game.strategies;

import com.badlogic.gdx.graphics.Texture;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;

public class MortarStrategy implements AttackStrategy {
    private static final float FIRE_INTERVAL = 5f;
    private static final float DAMAGE = 55f;
    private static final float PROJECTILE_TRAVEL_SPEED = 300f;
    private static final float SPLASH_RADIUS = 75f;
    private static final float DEADZONE_RADIUS = 130f;

    private final ProjectilePool pool;
    private final Texture projectile;

    public MortarStrategy(ProjectilePool pool, Texture projectile) {
        this.pool = pool;
        this.projectile = projectile;
    }

    @Override
    public void fire(Tower tower, Enemy target) {
        // Capture the target's position now. The shell will never track this enemy afterward.
        pool.obtainMortar(projectile, tower.getPosition(), target.getPosition(),
                DAMAGE, SPLASH_RADIUS, PROJECTILE_TRAVEL_SPEED);
    }

    @Override
    public boolean canTarget(Tower tower, Enemy enemy) {
        return tower.getPosition().dst(enemy.getPosition()) >= DEADZONE_RADIUS;
    }

    @Override
    public float getDeadzoneRadius() {
        return DEADZONE_RADIUS;
    }

    @Override
    public float getFireInterval() {
        return FIRE_INTERVAL;
    }
}
