package com.etherealtowerdefense.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import com.etherealtowerdefense.game.commands.*;
import com.etherealtowerdefense.game.entities.*;
import com.etherealtowerdefense.game.factories.*;
import com.etherealtowerdefense.game.map.GameMap;
import com.etherealtowerdefense.game.observers.*;
import com.etherealtowerdefense.game.pools.*;
import com.etherealtowerdefense.game.states.*;
import com.etherealtowerdefense.game.ui.GameHUD;
import com.etherealtowerdefense.game.ui.MainMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EtherealTDGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private GameMap map;

    private Texture cannonTexture;
    private Texture mortarTexture;
    private Texture cannonballTexture;
    private Texture mortarRoundTexture;
    private Texture enemyTexture;

    private TowerFactory towerFactory;
    private EnemyFactory enemyFactory;

    private ProjectilePool projectilePool;

    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    private GameEvents events;
    private GameState state = new PlayingState();

    private Tower previewTower;
    private boolean previewEnemy; // this should be unused tbh
    private final Vector2 mouseWorld = new Vector2();

    private boolean placementValid;

    // menu
    private MainMenu menu;
    private boolean inMenu = true;
    private InputAdapter gameInput;

    // hud
    private GameHUD hud;
    private static final int MAX_BASE_HP = 100;
    private int baseHp = MAX_BASE_HP;
    private int gold = 1000;

    @Override
    public void create() {

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        map = new GameMap();

        cannonTexture = new Texture("towers/cannon.png");
        mortarTexture = new Texture("towers/mortar.png");

        cannonballTexture = new Texture("projectiles/cannonball.png");
        mortarRoundTexture = new Texture("projectiles/mortar_round.png");

        enemyTexture = new Texture("enemies/dummy.png");

        projectilePool = new ProjectilePool();

        towerFactory = new TowerFactory(
            projectilePool,
            cannonTexture,
            cannonballTexture,
            mortarTexture,
            mortarRoundTexture
        );

        enemyFactory = new EnemyFactory(enemyTexture, map.getWaypoints());

        events = new GameEvents();

        events.add(event ->
            Gdx.app.log("GameEvent", event.getType().name())
        );

        events.add(event -> {
            if (event.getType() == GameEvent.Type.ENEMY_ESCAPED) {
                damageBase(1);
            }
        });

        gameInput = new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.ESCAPE) {
                    cancelPlacement();
                    return true;
                }

                if (keycode == Input.Keys.NUM_1) {
                    selectTower(BuyTowerCommand.Kind.CANNON);
                    return true;
                }

                if (keycode == Input.Keys.NUM_2) {
                    selectTower(BuyTowerCommand.Kind.MORTAR);
                    return true;
                }

                if (keycode >= Input.Keys.NUM_3 && keycode <= Input.Keys.NUM_7) {
                    EnemyFactory.Type[] types = {
                        EnemyFactory.Type.BLAST_CRAWLER,
                        EnemyFactory.Type.ALPECA,
                        EnemyFactory.Type.FAUN,
                        EnemyFactory.Type.HOPLITAI,
                        EnemyFactory.Type.FREDDY_FAZBEAR
                    };
                    EnemyFactory.Type type = types[keycode - Input.Keys.NUM_3];
                    new SpawnEnemyCommand(enemyFactory, enemies, type).execute();
                    events.publish(new GameEvent(GameEvent.Type.ENEMY_SPAWNED));
                    return true;
                }

                if (keycode == Input.Keys.SPACE) {

                    if (state instanceof PausedState) {
                        setState(new PlayingState());
                    } else if (state instanceof PlayingState) {
                        setState(new PausedState());
                    }

                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDown(
                int screenX,
                int screenY,
                int pointer,
                int button
            ) {

                updateMouse(screenX, screenY);

                if (button == Input.Buttons.RIGHT) {
                    cancelPlacement();
                    return true;
                }

                if (button == Input.Buttons.LEFT
                    && state instanceof PlacementState) {

                    if (previewEnemy) {

                        new SpawnEnemyCommand(
                            enemyFactory,
                            enemies,
                            mouseWorld
                        ).execute();

                        events.publish(
                            new GameEvent(GameEvent.Type.ENEMY_SPAWNED)
                        );

                        cancelPlacement();

                    } else if (previewTower != null && placementValid) {

                        new PlaceTowerCommand(
                            previewTower,
                            mouseWorld.x,
                            mouseWorld.y,
                            towers
                        ).execute();

                        events.publish(
                            new GameEvent(GameEvent.Type.TOWER_PLACED)
                        );

                        previewTower = null;
                        cancelPlacement();
                    }

                    return true;
                }

                return false;
            }

            @Override
            public boolean mouseMoved(int x, int y) {
                updateMouse(x, y);
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                updateMouse(x, y);
                return true;
            }
        };

        hud = new GameHUD(
            new StretchViewport(GameMap.WORLD_WIDTH, GameMap.WORLD_HEIGHT),
            Arrays.asList(
                new GameHUD.TowerOption(BuyTowerCommand.Kind.CANNON.name(), "1: Cannon"),
                new GameHUD.TowerOption(BuyTowerCommand.Kind.MORTAR.name(), "2: Mortar")
            ),
            id -> {
                if (id == null) {
                    cancelPlacement();
                } else {
                    selectTower(BuyTowerCommand.Kind.valueOf(id));
                }
            }
        );
        hud.onBaseHpChanged(baseHp, MAX_BASE_HP);
        hud.onGoldChanged(gold);

        menu = new MainMenu(() -> {
            inMenu = false;
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            hud.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.input.setInputProcessor(new InputMultiplexer(hud.getStage(), gameInput));
        });
        Gdx.input.setInputProcessor(menu.getStage());
    }

    // tower selector

    private void selectTower(BuyTowerCommand.Kind kind) {

        hud.setSelectedTower(kind.name());

        new BuyTowerCommand(
            kind,
            towerFactory,
            tower -> {
                previewTower = tower;
                previewEnemy = false;

                setState(new PlacementState(tower));
            }
        ).execute();
    }

    private void setState(GameState next) {
        state.exit();
        state = next;
        state.enter();
    }

    private void cancelPlacement() {
        previewTower = null;
        previewEnemy = false;
        hud.clearSelection();

        if (state instanceof PlacementState) {
            setState(new PlayingState());
        }
    }

    private void damageBase(int amount) {
        baseHp = Math.max(0, baseHp - amount);
        hud.onBaseHpChanged(baseHp, MAX_BASE_HP);
    }

    private void rewardKill(Enemy e) {
        int reward = rewardFor(e);
        gold += reward;
        hud.onGoldChanged(gold);
        hud.showGoldPopup(e.getPosition().x, e.getPosition().y + 24f, reward);
    }

    private int rewardFor(Enemy e) {
        return Math.max(1, Math.round(e.getMaxHealth() / 10f));
    }

    private void updateMouse(int sx, int sy) {

        mouseWorld.set(
            sx,
            Gdx.graphics.getHeight() - sy
        );

        mouseWorld.scl(
            1280f / Gdx.graphics.getWidth(),
            720f / Gdx.graphics.getHeight()
        );

        updatePlacementValidity();
    }

    private void updatePlacementValidity() {

        if (previewTower == null) {
            placementValid = false;
            return;
        }

        float r = previewTower.getCollisionRadius();

        placementValid =
            mouseWorld.x >= r
                && mouseWorld.y >= r
                && mouseWorld.x <= GameMap.WORLD_WIDTH - r
                && mouseWorld.y <= GameMap.WORLD_HEIGHT - r
                && !map.intersectsPath(mouseWorld.x, mouseWorld.y, r);

        if (placementValid) {

            for (Tower t : towers) {

                if (t.getPosition().dst(mouseWorld)
                    < r + t.getCollisionRadius()) {

                    placementValid = false;
                    break;
                }
            }
        }
    }

    // game loop

    @Override
    public void render() {

        if (inMenu) {
            menu.render(Gdx.graphics.getDeltaTime());
            return;
        }

        float delta = Math.min(
            Gdx.graphics.getDeltaTime(),
            0.033f
        );

        ScreenUtils.clear(0.12f, 0.17f, 0.14f, 1);

        if (state instanceof PlayingState
            || state instanceof PlacementState) {

            for (Enemy e : enemies) {
                e.update(delta);
                e.applyTowerEffects(towers);
            }

            for (Tower t : towers) {
                t.update(delta, enemies);
            }

            projectilePool.update(delta, enemies);
            removeInactiveEnemies();
        }

        drawMap();

        batch.begin();

        //cannon renders first so it goes below towers epicly
        projectilePool.drawCannonProjectiles(batch);

        for (Tower t : towers) {
            t.draw(batch);
        }

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        projectilePool.drawMortarProjectiles(batch);

        // placement ghooost
        if (state instanceof PlacementState) {

            if (previewTower != null) {

                previewTower.place(mouseWorld.x, mouseWorld.y);
                previewTower.setRotation(0f);

                batch.setColor(1, 1, 1, 0.48f);
                previewTower.draw(batch);
                batch.setColor(Color.WHITE);

            } else if (previewEnemy) {

                batch.setColor(1, 1, 1, 0.45f);

                batch.draw(
                    enemyTexture,
                    mouseWorld.x - 22,
                    mouseWorld.y - 22,
                    44,
                    44
                );

                batch.setColor(Color.WHITE);
            }
        }

        batch.end();

        drawEnemyEffects();
        drawPlacementFeedback();

        hud.render(delta);
    }

    private void removeInactiveEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (!e.isActive()) {
                if (e.getHealth() > 0) {
                    events.publish(new GameEvent(GameEvent.Type.ENEMY_ESCAPED));
                } else {
                    events.publish(new GameEvent(GameEvent.Type.ENEMY_DEFEATED));
                    rewardKill(e);
                }
                enemies.remove(i);
            }
        }
    }

    private void drawMap() {

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (int y = 0; y < GameMap.HEIGHT; y++) {

            for (int x = 0; x < GameMap.WIDTH; x++) {

                if (map.getTile(x, y) == 1) {
                    shapes.setColor(0.62f, 0.43f, 0.25f, 1);
                } else {
                    shapes.setColor(0.20f, 0.43f, 0.25f, 1);
                }

                shapes.rect(
                    x * 40,
                    (17 - y) * 40,
                    40,
                    40
                );
            }
        }

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);

        shapes.setColor(0.12f, 0.29f, 0.17f, 0.35f);

        for (int x = 0; x <= 32; x++) {
            shapes.line(x * 40, 0, x * 40, 720);
        }

        for (int y = 0; y <= 18; y++) {
            shapes.line(0, y * 40, 1280, y * 40);
        }

        shapes.end();
    }

    private void drawEnemyEffects() {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (Enemy enemy : enemies) enemy.drawEffect(shapes);
        shapes.end();
    }

    // placement thing ghost oooo

    private void drawPlacementFeedback() {

        if (state instanceof PlacementState && previewTower != null) {

            shapes.begin(ShapeRenderer.ShapeType.Line);

            shapes.setColor(
                placementValid
                    ? new Color(0.25f, 0.95f, 0.45f, 0.8f)
                    : new Color(1f, 0.2f, 0.2f, 0.85f)
            );

            shapes.circle(
                mouseWorld.x,
                mouseWorld.y,
                previewTower.getRange(),
                72
            );

            shapes.circle(
                mouseWorld.x,
                mouseWorld.y,
                previewTower.getCollisionRadius(),
                48
            );

            float deadzone = previewTower.getStrategy().getDeadzoneRadius();

            if (deadzone > 0f) {

                shapes.setColor(new Color(1f, 0.65f, 0.15f, 0.9f));

                shapes.circle(
                    mouseWorld.x,
                    mouseWorld.y,
                    deadzone,
                    48
                );
            }

            shapes.end();
        }

        //  mortar porjectile impact
        shapes.begin(ShapeRenderer.ShapeType.Line);
        projectilePool.drawImpacts(shapes);
        shapes.end();
    }

    // window stuff

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            if (inMenu) {
                menu.resize(width, height);
            } else {
                hud.resize(width, height);
            }
        }
    }

    @Override
    public void dispose() {

        menu.dispose();
        hud.dispose();

        batch.dispose();
        shapes.dispose();

        cannonTexture.dispose();
        mortarTexture.dispose();

        cannonballTexture.dispose();
        mortarRoundTexture.dispose();

        enemyTexture.dispose();
    }
}
