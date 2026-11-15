package com.etherealtowerdefense.game.observers;
import java.util.ArrayList;
import java.util.List;

public class GameEvents {
    private final List<GameObserver> observers = new ArrayList<>();
    public void add(GameObserver o) {
        observers.add(o);
    }

    public void remove(GameObserver o) {
        observers.remove(o);
    }

    public void publish(GameEvent e) {
        for (GameObserver o:new ArrayList<>(observers)) {
            o.onGameChanged(e);
        }
    }
}
