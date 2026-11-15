package com.etherealtowerdefense.game.observers;

public class GameEvent {
    public enum Type{TOWER_PLACED, ENEMY_SPAWNED, ENEMY_ESCAPED, ENEMY_DEFEATED}
    private final Type type;

    public GameEvent(Type type) {
        this.type=type;
    }

    public Type getType() {
        return type;
    }
}
