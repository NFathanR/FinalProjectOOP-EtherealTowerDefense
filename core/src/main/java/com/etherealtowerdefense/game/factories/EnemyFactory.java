package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.enemyType.common.Alpeca;
import com.etherealtowerdefense.game.entities.enemyType.common.BlastCrawler;
import com.etherealtowerdefense.game.entities.enemyType.common.Faun;
import com.etherealtowerdefense.game.entities.enemyType.common.Hoplitai;
import com.etherealtowerdefense.game.entities.enemyType.elite.FreddyFazbear;

import java.util.List;

/** Creates concrete enemy classes; each enemy type owns its own class and stats. */
public class EnemyFactory {
    public enum Type { BLAST_CRAWLER, ALPECA, FAUN, HOPLITAI, FREDDY_FAZBEAR }

    private final Texture texture;
    private final List<Vector2> path;

    public EnemyFactory(Texture texture, List<Vector2> path) {
        this.texture = texture;
        this.path = path;
    }

    public Enemy create() { return create(Type.BLAST_CRAWLER); }

    public Enemy create(Type type) {
        switch (type) {
            case ALPECA: return new Alpeca(texture, path);
            case FAUN: return new Faun(texture, path);
            case HOPLITAI: return new Hoplitai(texture, path);
            case FREDDY_FAZBEAR: return new FreddyFazbear(texture, path);
            case BLAST_CRAWLER:
            default: return new BlastCrawler(texture, path);
        }
    }

    public Enemy createAt(Vector2 position) {
        Enemy enemy = create();
        enemy.getPosition().set(position);
        return enemy;
    }

    public Enemy createAt(Type type, Vector2 position) {
        Enemy enemy = create(type);
        enemy.getPosition().set(position);
        return enemy;
    }
}
