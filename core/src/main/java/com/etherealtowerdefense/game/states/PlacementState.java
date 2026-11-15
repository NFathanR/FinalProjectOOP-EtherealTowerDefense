package com.etherealtowerdefense.game.states;
import com.etherealtowerdefense.game.entities.Tower;

public class PlacementState implements GameState {
    private final Tower preview;
    public PlacementState(Tower preview) {
        this.preview = preview;
    }
    public Tower getPreview() {
        return preview;
    }
    public String name() {
        return "Placement";
    }
}
