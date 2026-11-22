package com.etherealtowerdefense.game.strategies;

import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.Tower;
import java.util.List;

public interface AttackStrategy {
    void fire(Tower tower, Enemy target);
    float getFireInterval();

    default boolean usesCustomUpdate() { return false; }

    default void update(Tower tower, List<Enemy> enemies, float delta) { }

    default boolean canTarget(Tower tower, Enemy enemy) { return true; }
    default float getDeadzoneRadius() { return 0f; }

    default Float getDamage() { return null; }
}
