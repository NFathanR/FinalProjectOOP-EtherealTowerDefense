package com.etherealtowerdefense.game.entities.enemyType.common;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import java.util.List;

public class BlastCrawler extends Enemy {
    public static final float BASE_HP = 100f;
    public static final float BASE_SPEED = 95f;

    public BlastCrawler(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP);
    }
}
