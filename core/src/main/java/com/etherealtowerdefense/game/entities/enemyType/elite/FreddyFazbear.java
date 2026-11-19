package com.etherealtowerdefense.game.entities.enemyType.elite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import java.util.List;

public class FreddyFazbear extends Enemy {
    public static final float BASE_HP = 600f;
    public static final float BASE_SPEED = 38f;
    public static final float SECONDARY_HITBOX_RADIUS = 180f;
    private static final Color HITBOX_COLOR = new Color(0.12f, 0.55f, 1f, 0.95f);

    public FreddyFazbear(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP);
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void drawEffect(ShapeRenderer shapes) {
        if (!active) return;
        shapes.setColor(HITBOX_COLOR);
        shapes.circle(position.x, position.y, SECONDARY_HITBOX_RADIUS, 64);
    }
    @Override
    public void applyTowerEffects(List<Tower> towers) {
        if (!active) return;

        for (Tower tower : towers) {
            float distance = position.dst(tower.getPosition());

            if (distance <= SECONDARY_HITBOX_RADIUS) {
                tower.setStunned(true);
            }
        }
    }
}

