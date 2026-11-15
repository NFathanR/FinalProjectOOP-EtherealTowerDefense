package com.etherealtowerdefense.game.factories;

import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.pools.EnemyPool;

public class EnemyFactory {

    private final EnemyPool pool;

    public EnemyFactory(EnemyPool pool) {
        this.pool = pool;
    }

    public Enemy create() {
        return pool.spawn();
    }

    public Enemy createAt(com.badlogic.gdx.math.Vector2 position) {
        return pool.spawnAt(position);
    }
}
