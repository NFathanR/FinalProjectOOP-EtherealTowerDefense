package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.enemyType.common.Alpeca;
import com.etherealtowerdefense.game.entities.enemyType.common.BlastCrawler;
import com.etherealtowerdefense.game.entities.enemyType.common.Faun;
import com.etherealtowerdefense.game.entities.enemyType.common.Hoplitai;
import com.etherealtowerdefense.game.entities.enemyType.elite.FreddyFazbear;
import com.etherealtowerdefense.game.entities.enemyType.elite.Girtablullu;

import java.util.List;

public class EnemyFactory {
    public enum Type { BLAST_CRAWLER, ALPECA, FAUN, HOPLITAI, FREDDY_FAZBEAR, GIRTABLULLU }

    private final Texture blastCrawlerTexture;
    private final Texture alpecaTexture;
    private final Texture faunTexture;
    private final Texture hoplitaiTexture;
    private final Texture freddyTexture;
    private final Texture girtablulluTexture;
    private final List<Vector2> path;

    public EnemyFactory(
        Texture blastCrawlerTexture,
        Texture alpecaTexture,
        Texture faunTexture,
        Texture hoplitaiTexture,
        Texture freddyTexture,
        Texture girtablulluTexture,
        List<Vector2> path
    ) {
        this.blastCrawlerTexture = blastCrawlerTexture;
        this.alpecaTexture = alpecaTexture;
        this.faunTexture = faunTexture;
        this.hoplitaiTexture = hoplitaiTexture;
        this.freddyTexture = freddyTexture;
        this.girtablulluTexture = girtablulluTexture;
        this.path = path;
    }

    public Enemy create() { return create(Type.BLAST_CRAWLER); }

    public Enemy create(Type type) {
        switch (type) {
            case ALPECA: return new Alpeca(alpecaTexture, path);
            case FAUN: return new Faun(faunTexture, path);
            case HOPLITAI: return new Hoplitai(hoplitaiTexture, path);
            case FREDDY_FAZBEAR: return new FreddyFazbear(freddyTexture, path);
            case GIRTABLULLU: return new Girtablullu(girtablulluTexture, path);
            case BLAST_CRAWLER:
            default: return new BlastCrawler(blastCrawlerTexture, path);
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
