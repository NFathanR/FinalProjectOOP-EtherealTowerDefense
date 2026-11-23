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
    private final Texture cannonRotor;
    private final Texture rotorBullet;
    private final Texture mortar;
    private final Texture mortarRound;
    private final Texture caesarShield;
    private final SplashIndicatorManager splashIndicators;

    public TowerFactory(
        ProjectilePool p,
        Texture c,
        Texture cb,
        Texture cr,
        Texture rb,
        Texture m,
        Texture mr,
        Texture cs,
        SplashIndicatorManager splashIndicators
    ) {
        projectiles = p;
        cannon = c;
        cannonball = cb;
        cannonRotor = cr;
        rotorBullet = rb;
        mortar = m;
        mortarRound = mr;
        caesarShield = cs;
        this.splashIndicators = splashIndicators;
    }

    public Tower createCannon() {
        Tower tower = new Tower(cannon, 250, 325, 90, 200f,
            new CannonStrategy(projectiles, cannonball));
        tower.setDisplayInfo("Cannon", "Physical", "Long-range artillery");
        return tower;
    }

    public Tower createCannonRotor() {
        Tower tower = new Tower(cannonRotor, 250, 325, 90, 170f,
            new CannonRotorStrategy(projectiles, rotorBullet));
        tower.setDisplayInfo("Cannon Rotor", "Physical", "Double-barrel burst artillery");
        tower.setSidebarStats(
            new Tower.StatLine("ATK", () -> "37.1"),
            new Tower.StatLine("DPS", () -> "78"),
            new Tower.StatLine("ATK Speed", () -> "1.6s"),
            new Tower.StatLine("Range", () -> "115.5")
        );
        return tower;
    }

    public Tower createCaesarShield() {
        Tower tower = new Tower(caesarShield, 250, 250, 110, 105,
            new CaesarShieldStrategy(splashIndicators));
        tower.setDisplayInfo("Caesar Shield", "Physical", "Defensive support");
        return tower;
    }

    public Tower createMortar() {
        Tower tower = new Tower(mortar, 250, 300, 90, 330,
            new MortarStrategy(projectiles, mortarRound));
        tower.setDisplayInfo("Mortar", "Physical", "Area damage artillery");
        return tower;
    }
}
