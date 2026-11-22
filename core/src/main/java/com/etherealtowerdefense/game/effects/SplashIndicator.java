package com.etherealtowerdefense.game.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/** A stationary, one-second fading circle for any splash-damage attack. */
public class SplashIndicator {

    private static final float DURATION = 1f;
    private static final Color INDICATOR_COLOR = new Color(1f, 0.58f, 0.12f, 1f);

    private final Vector2 position = new Vector2();
    private final float radius;
    private float elapsed;

    public SplashIndicator(Vector2 position, float radius) {
        this.position.set(position);
        this.radius = radius;
    }

    public void update(float delta) {
        elapsed += delta;
    }

    public void draw(ShapeRenderer shapes) {
        float alpha = Math.max(0f, 1f - elapsed / DURATION);
        shapes.setColor(INDICATOR_COLOR.r, INDICATOR_COLOR.g, INDICATOR_COLOR.b, alpha);
        shapes.circle(position.x, position.y, radius, 48);
    }

    public boolean isExpired() {
        return elapsed >= DURATION;
    }
}
