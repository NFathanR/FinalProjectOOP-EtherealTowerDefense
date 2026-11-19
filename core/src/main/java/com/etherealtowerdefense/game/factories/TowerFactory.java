package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Texture;

import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.pools.ProjectilePool;
import com.etherealtowerdefense.game.effects.SplashIndicatorManager;
import com.etherealtowerdefense.game.strategies.*;

public class TowerFactory {

    private final ProjectilePool projectiles;
    private final Texture cannon;
    private final Texture cannonball;
    private final Texture mortar;
    private final Texture mortarRound;
    private final Texture caesarShield;
    private final SplashIndicatorManager splashIndicators;

    public TowerFactory(
        ProjectilePool p,
        Texture c,
        Texture cb,
        Texture m,
        Texture mr,
        Texture cs,
        SplashIndicatorManager splashIndicators
    ) {
        projectiles = p;
        cannon = c;
        cannonball = cb;
        mortar = m;
        mortarRound = mr;
        caesarShield = cs;
        this.splashIndicators = splashIndicators;
    }

    public Tower createCannon() {
        return new Tower(
            cannon,
            250,
            325,
            90,
            150,
            new CannonStrategy(projectiles, cannonball)
        );
    }

    public Tower createCaesarShield() {
        return new Tower(
            caesarShield,
            250,
            250,
            110,
            105,
            new CaesarShieldStrategy(splashIndicators)
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
