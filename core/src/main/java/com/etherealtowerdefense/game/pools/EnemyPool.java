
package com.etherealtowerdefense.game.pools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

import com.etherealtowerdefense.game.entities.Enemy;

import java.util.List;

public class EnemyPool extends Pool<Enemy> {

    private Texture texture;
    private List<Vector2> path;

    public void configure(Texture texture, List<Vector2> path) {
        this.texture = texture;
        this.path = path;
    }

    public Enemy spawn() {

        Enemy enemy = obtain();
        enemy.init(texture, path, 95f, 100f);

        return enemy;
    }

    public Enemy spawnAt(Vector2 start) {

        Enemy enemy = obtain();
        enemy.initAt(texture, path, start, 95f, 100f);

        return enemy;
    }


    @Override
    protected Enemy newObject() {
        return new Enemy();
    }


    public void freeInactive(List<Enemy> enemies) {

        for (int i = enemies.size() - 1; i >= 0; i--) {

            if (!enemies.get(i).isActive()) {
                free(enemies.remove(i));
            }
        }
    }
}
