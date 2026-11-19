package com.etherealtowerdefense.game.commands;

import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.factories.TowerFactory;

public class BuyTowerCommand implements GameCommand {

    public enum Kind {
        CANNON,
        MORTAR,
        CAESAR_SHIELD
    }

    private final Kind kind;
    private final TowerFactory factory;
    private final java.util.function.Consumer<Tower> result;

    public BuyTowerCommand(
        Kind k,
        TowerFactory f,
        java.util.function.Consumer<Tower> r
    ) {
        kind = k;
        factory = f;
        result = r;
    }

    @Override
    public void execute() {
        Tower tower;
        switch (kind) {
            case CANNON:
                tower = factory.createCannon();
                break;
            case MORTAR:
                tower = factory.createMortar();
                break;
            case CAESAR_SHIELD:
                tower = factory.createCaesarShield();
                break;
            default:
                throw new IllegalStateException("Unknown tower kind: " + kind);
        }
        result.accept(tower);
    }
}
