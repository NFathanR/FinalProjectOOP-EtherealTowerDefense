package com.etherealtowerdefense.game.entities.enemyType.common;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import java.util.List;

public class Alpeca extends Enemy {
    public static final float BASE_HP = 160f;
    public static final float BASE_SPEED = 110f;

    public Alpeca(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP);
    }
}
