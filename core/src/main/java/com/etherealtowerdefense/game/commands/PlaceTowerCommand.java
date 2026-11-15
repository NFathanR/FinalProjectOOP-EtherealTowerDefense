package com.etherealtowerdefense.game.commands;

import com.etherealtowerdefense.game.entities.Tower;

import java.util.List;

public class PlaceTowerCommand implements GameCommand {

    private final Tower tower;
    private final float x;
    private final float y;
    private final List<Tower> towers;

    public PlaceTowerCommand(
        Tower t,
        float x,
        float y,
        List<Tower> list
    ) {
        tower = t;
        this.x = x;
        this.y = y;
        towers = list;
    }

    @Override
    public void execute() {
        tower.place(x, y);
        towers.add(tower);
    }
}
