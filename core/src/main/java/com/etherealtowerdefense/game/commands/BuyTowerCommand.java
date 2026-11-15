package com.etherealtowerdefense.game.commands;

import com.etherealtowerdefense.game.entities.Tower;
import com.etherealtowerdefense.game.factories.TowerFactory;

public class BuyTowerCommand implements GameCommand {

    public enum Kind {
        CANNON,
        MORTAR
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
        result.accept(
            kind == Kind.CANNON
                ? factory.createCannon()
                : factory.createMortar()
        );
    }
}
