package com.etherealtowerdefense.game.effects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/** Shared lifecycle manager for splash indicators spawned by any tower. */
public class SplashIndicatorManager {

    private final List<SplashIndicator> active = new ArrayList<>();

    public void spawn(Vector2 position, float radius) {
        active.add(new SplashIndicator(position, radius));
    }

    public void update(float delta) {
        for (int i = active.size() - 1; i >= 0; i--) {
            SplashIndicator indicator = active.get(i);
            indicator.update(delta);
            if (indicator.isExpired()) active.remove(i);
        }
    }

    public void draw(ShapeRenderer shapes) {
        for (SplashIndicator indicator : active) indicator.draw(shapes);
    }
}
