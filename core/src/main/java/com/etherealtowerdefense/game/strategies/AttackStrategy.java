package com.etherealtowerdefense.game.strategies;

import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;

public interface AttackStrategy {
    void fire(Tower tower, Enemy target);
    float getFireInterval();

    default boolean canTarget(Tower tower, Enemy enemy) { return true; }
    default float getDeadzoneRadius() { return 0f; }
}
