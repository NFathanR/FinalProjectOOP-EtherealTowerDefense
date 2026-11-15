package com.etherealtowerdefense.game.states;

public interface GameState {
    default void enter() {};
    default void update(float delta) {};
    default void exit() {};
    default String name() {
        return "Unknown";
    }
}
