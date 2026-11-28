package com.etherealtowerdefense.game.entities.enemyType.common;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import java.util.List;

public class BlastCrawler extends Enemy {
    public static final float BASE_HP = 50f;
    public static final float BASE_SPEED = 95f;
    public static final int BASE_DAMAGE = 1;
    public static final int DENNY_REWARD = 5;

    public BlastCrawler(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP, BASE_DAMAGE, DENNY_REWARD);
    }
}
