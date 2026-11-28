package com.etherealtowerdefense.game.entities.enemyType.common;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import java.util.List;

public class Alpeca extends Enemy {
    public static final float BASE_HP = 80f;
    public static final float BASE_SPEED = 110f;
    public static final int BASE_DAMAGE = 2;
    public static final int DENNY_REWARD = 8;

    public Alpeca(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP, BASE_DAMAGE, DENNY_REWARD);
    }
}
