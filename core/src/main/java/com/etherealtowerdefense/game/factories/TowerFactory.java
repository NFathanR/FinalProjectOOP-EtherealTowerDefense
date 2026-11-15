package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Texture;

import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;
import com.etherealtowerdefense.game.strategies.*;

public class TowerFactory {

    private final ProjectilePool projectiles;
    private final Texture cannon;
    private final Texture cannonball;
    private final Texture mortar;
    private final Texture mortarRound;

    public TowerFactory(ProjectilePool p, Texture c, Texture cb, Texture m, Texture mr) {
        projectiles = p;
        cannon = c;
        cannonball = cb;
        mortar = m;
        mortarRound = mr;
    }

    public Tower createCannon() {
        return new Tower(
            cannon,
            250,
            325,
            90,
            260,
            new CannonStrategy(projectiles, cannonball)
        );
    }

    public Tower createMortar() {
        return new Tower(
            mortar,
            250,
            300,
            90,
            330,
            new MortarStrategy(projectiles, mortarRound)
        );
    }
}
