package com.etherealtowerdefense.game.pools;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Projectile;
import java.util.ArrayList;
import java.util.List;

public class ProjectilePool extends Pool<Projectile> {
    private final List<Projectile> active = new ArrayList<>();

    public Projectile obtain(Texture texture, Vector2 start, Enemy target, float speed, float damage) {
        Projectile projectile = super.obtain();
        projectile.init(texture, start, target, speed, damage);
        active.add(projectile);
        return projectile;
    }

    public Projectile obtainMortar(Texture texture, Vector2 start, Vector2 impactPoint,
                                   float damage, float splashRadius, float travelSpeed) {
        Projectile projectile = super.obtain();
        projectile.initMortar(texture, start, impactPoint, damage, splashRadius, travelSpeed);
        projectile.setMortarStart(start);
        active.add(projectile);
        return projectile;
    }

    @Override
    protected Projectile newObject() { return new Projectile(); }

    public void update(float delta, List<Enemy> enemies) {
        for (int i = active.size() - 1; i >= 0; i--) {
            Projectile projectile = active.get(i);
            projectile.update(delta);
            projectile.updateMortar(delta, enemies);
            if (!projectile.isActive()) {
                active.remove(i);
                free(projectile);
            }
        }
    }

    public void drawCannonProjectiles(SpriteBatch batch) {
        for (Projectile projectile : active) {
            if (!projectile.isLobbed()) {
                projectile.draw(batch);
            }
        }
    }

    public void drawMortarProjectiles(SpriteBatch batch) {
        for (Projectile projectile : active) {
            if (projectile.isLobbed()) {
                projectile.draw(batch);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (Projectile projectile : active) projectile.draw(batch);
    }

    public void drawImpacts(ShapeRenderer shapes) {
        for (Projectile projectile : active) projectile.drawImpact(shapes);
    }
}
