package com.etherealtowerdefense.game.commands;

import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.factories.EnemyFactory;
import java.util.List;

public class SpawnEnemyCommand implements GameCommand {
    private final EnemyFactory factory;
    private final List<Enemy> enemies;
    private final Vector2 position;
    private final EnemyFactory.Type type;

    public SpawnEnemyCommand(EnemyFactory factory, List<Enemy> enemies) {
        this(factory, enemies, EnemyFactory.Type.BLAST_CRAWLER, null);
    }

    public SpawnEnemyCommand(EnemyFactory factory, List<Enemy> enemies, Vector2 position) {
        this(factory, enemies, EnemyFactory.Type.BLAST_CRAWLER, position);
    }

    public SpawnEnemyCommand(EnemyFactory factory, List<Enemy> enemies, EnemyFactory.Type type) {
        this(factory, enemies, type, null);
    }

    public SpawnEnemyCommand(EnemyFactory factory, List<Enemy> enemies,
                             EnemyFactory.Type type, Vector2 position) {
        this.factory = factory;
        this.enemies = enemies;
        this.type = type;
        this.position = position == null ? null : new Vector2(position);
    }

    @Override
    public void execute() {
        Enemy enemy = position == null
            ? factory.create(type)
            : factory.createAt(type, position);
        enemies.add(enemy);
    }
}
