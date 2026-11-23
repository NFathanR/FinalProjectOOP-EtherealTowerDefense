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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.GL20; //Bantu transparan(sen)

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.etherealtowerdefense.game.commands.*;
import com.etherealtowerdefense.game.entities.*;
import com.etherealtowerdefense.game.factories.*;
import com.etherealtowerdefense.game.map.GameMap;
import com.etherealtowerdefense.game.observers.*;
import com.etherealtowerdefense.game.pools.*;
import com.etherealtowerdefense.game.effects.SplashIndicatorManager;
import com.etherealtowerdefense.game.states.*;
import com.etherealtowerdefense.game.ui.GameHUD;
import com.etherealtowerdefense.game.ui.TowerSidebar;
import com.etherealtowerdefense.game.ui.MainMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EtherealTDGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private GameMap map;
    // Tinggi area Tower Rack di bawah map (sen)
    private static final int RACK_HEIGHT = 180;
    private static final float WORLD_WIDTH = 1600f;
    private static final float WORLD_HEIGHT = 900f;
    private Viewport worldViewport;
    private OrthographicCamera worldCamera;
    // Font untuk label placeholder tower rack (sen)
    private BitmapFont rackFont;
    private BitmapFont waveFont;

    private Texture cannonTexture;
    private Texture mortarTexture;
    private Texture caesarShieldTexture;
    private Texture cannonballTexture;
    private Texture mortarRoundTexture;
    private Texture blastCrawlerTexture;
    private Texture alpecaTexture;
    private Texture faunTexture;
    private Texture hoplitaiTexture;
    private Texture freddyTexture;

    private TowerFactory towerFactory;
    private EnemyFactory enemyFactory;

    private ProjectilePool projectilePool;
    private SplashIndicatorManager splashIndicators;

    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    private GameEvents events;
    private GameState state = new PlayingState();

    private Tower previewTower;
    private Tower selectedTower;
    private Tower movingTower;
    private final Vector2 movingOriginalPosition = new Vector2();
    private boolean previewEnemy; // this should be unused tbh
    private final Vector2 mouseWorld = new Vector2();

    private boolean placementValid;
    private BuyTowerCommand.Kind placingKind;

    // menu
    private MainMenu menu;
    private boolean inMenu = true;
    private InputAdapter gameInput;

    // hud
    private GameHUD hud;
    private static final int MAX_BASE_HP = 100;
    private int baseHp = MAX_BASE_HP;
    private int denny = 1000;

    @Override
    public void create() {

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        rackFont = FontFactory.get(22);
        waveFont = FontFactory.get(30);
        map = new GameMap();

        // Keep Gerald's 1280px playfield intact; reserve 320px on the right for the sidebar.
        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, worldCamera);
        worldCamera.position.set(WORLD_WIDTH / 2f, GameMap.WORLD_HEIGHT / 2f - RACK_HEIGHT / 2f, 0f);
        worldCamera.update();
        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        shapes.setProjectionMatrix(worldCamera.combined);


        cannonTexture = new Texture("towers/cannon.png");
        mortarTexture = new Texture("towers/mortar.png");
        caesarShieldTexture = new Texture("towers/caesarshield.png");

        cannonballTexture = new Texture("projectiles/cannonball.png");
        mortarRoundTexture = new Texture("projectiles/mortar_round.png");

        blastCrawlerTexture = new Texture("enemies/BlastCrawler.png");
        alpecaTexture = new Texture("enemies/Alpeca.png");
        faunTexture = new Texture("enemies/Faun.png");
        hoplitaiTexture = new Texture("enemies/Hoplitai.png");
        freddyTexture = new Texture("enemies/EthericNightmare-FreddyFazbear.png");

        splashIndicators = new SplashIndicatorManager();
        projectilePool = new ProjectilePool(splashIndicators);

        towerFactory = new TowerFactory(
            projectilePool,
            cannonTexture,
            cannonballTexture,
            mortarTexture,
            mortarRoundTexture,
            caesarShieldTexture,
            splashIndicators
        );

        enemyFactory = new EnemyFactory(
            blastCrawlerTexture,
            alpecaTexture,
            faunTexture,
            hoplitaiTexture,
            freddyTexture,
            map.getWaypoints()
        );

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
                    onTowerButton(BuyTowerCommand.Kind.CANNON);
                    return true;
                }

                if (keycode == Input.Keys.NUM_2) {
                    onTowerButton(BuyTowerCommand.Kind.MORTAR);
                    return true;
                }

                if (keycode == Input.Keys.NUM_3) {
                    onTowerButton(BuyTowerCommand.Kind.CAESAR_SHIELD);
                    return true;
                }

                int[] spawnKeys = {
                    Input.Keys.Y,
                    Input.Keys.U,
                    Input.Keys.I,
                    Input.Keys.O,
                    Input.Keys.P
                };
                EnemyFactory.Type[] spawnTypes = {
                    EnemyFactory.Type.BLAST_CRAWLER,
                    EnemyFactory.Type.ALPECA,
                    EnemyFactory.Type.FAUN,
                    EnemyFactory.Type.HOPLITAI,
                    EnemyFactory.Type.FREDDY_FAZBEAR
                };

                for (int i = 0; i < spawnKeys.length; i++) {
                    if (keycode == spawnKeys[i]) {
                        new SpawnEnemyCommand(enemyFactory, enemies, spawnTypes[i]).execute();
                        events.publish(new GameEvent(GameEvent.Type.ENEMY_SPAWNED));
                        return true;
                    }
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

                        if (movingTower != null) {
                            movingTower.place(mouseWorld.x, mouseWorld.y);
                            movingTower = null;
                        } else {
                            new PlaceTowerCommand(previewTower, mouseWorld.x, mouseWorld.y, towers).execute();
                            events.publish(new GameEvent(GameEvent.Type.TOWER_PLACED));
                        }
                        previewTower = null;
                        cancelPlacement();
                    }

                    return true;
                }

                if (button == Input.Buttons.LEFT && state instanceof PlayingState) {
                    Tower hit = findTowerAt(mouseWorld);
                    selectPlacedTower(hit);
                    return hit != null;
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
    new FitViewport(WORLD_WIDTH, WORLD_HEIGHT),
    Arrays.asList(
        new GameHUD.TowerOption(BuyTowerCommand.Kind.CANNON.name(), "1: Cannon"),
        new GameHUD.TowerOption(BuyTowerCommand.Kind.MORTAR.name(), "2: Mortar"),
        new GameHUD.TowerOption(BuyTowerCommand.Kind.CAESAR_SHIELD.name(), "3: Caesar\nShield")
    ),
    id -> {
        if (id == null) {
            cancelPlacement();
        } else {
            onTowerButton(BuyTowerCommand.Kind.valueOf(id));
        }
    },
    new TowerSidebar.ActionsListener() {
        @Override public void onMove(Tower tower) {
            if (tower == null || !towers.contains(tower)) return;
            movingTower = tower;
            movingOriginalPosition.set(tower.getPosition());
            previewTower = tower;
            selectedTower = null;
            tower.setSelected(false);
            hud.clearSelection();
            setState(new PlacementState(tower));
        }

        @Override public void onSell(Tower tower) {
            if (tower == null) return;
            towers.remove(tower);
            if (selectedTower == tower) selectedTower = null;
            hud.clearSelection();
        }

        @Override public void onUpgrade(Tower tower, String element) {
            // Upgrade controls are available; upgrade economy is not implemented in Gerald yet.
        }

        @Override public void onPriorityChanged(Tower tower) { }
    }
);

        hud.onBaseHpChanged(baseHp, MAX_BASE_HP);
        hud.onDennyChanged(denny);

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
        if (selectedTower != null) selectedTower.setSelected(false);
        selectedTower = null;
        hud.clearSelection();
        hud.setSelectedTower(kind.name());
        placingKind = kind;

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

    private Tower findTowerAt(Vector2 point) {
        Tower closest = null;
        float best = Float.MAX_VALUE;
        for (Tower tower : towers) {
            float distance = point.dst2(tower.getPosition());
            float radius = tower.getCollisionRadius() + 24f;
            if (distance <= radius * radius && distance < best) {
                closest = tower;
                best = distance;
            }
        }
        return closest;
    }

    private void selectPlacedTower(Tower tower) {
        if (selectedTower != null) selectedTower.setSelected(false);
        selectedTower = tower;
        if (selectedTower == null) hud.clearSelection();
        else {
            selectedTower.setSelected(true);
            hud.showTowerSidebar(selectedTower);
        }
    }

    private void drawSelectedTowerRange() {
        if (selectedTower == null) return;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 0.22f);
        shapes.circle(selectedTower.getPosition().x, selectedTower.getPosition().y,
            selectedTower.getRange(), 96);
        float deadzone = selectedTower.getStrategy().getDeadzoneRadius();
        if (deadzone > 0f) {
            shapes.setColor(1f, 1f, 1f, 0.12f);
            shapes.circle(selectedTower.getPosition().x, selectedTower.getPosition().y, deadzone, 64);
        }
        shapes.end();
    }

    private void onTowerButton(BuyTowerCommand.Kind kind) {
        if (state instanceof PlacementState && kind == placingKind) {
            cancelPlacement();
        } else {
            selectTower(kind);
        }
    }

    private void setState(GameState next) {
        state.exit();
        state = next;
        state.enter();
    }

    private void cancelPlacement() {
        if (movingTower != null) movingTower.place(movingOriginalPosition.x, movingOriginalPosition.y);
        if (selectedTower != null) selectedTower.setSelected(false);
        selectedTower = null;
        previewTower = null;
        previewEnemy = false;
        movingTower = null;
        hud.clearSelection();
        placingKind = null;

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
        denny += reward;
        hud.onDennyChanged(denny);
        hud.showDennyPopup(e.getPosition().x, e.getPosition().y + 24f, reward);
    }

    private int rewardFor(Enemy e) {
        return Math.max(1, Math.round(e.getMaxHealth() / 10f));
    }

    private void updateMouse(int sx, int sy) {
        Vector3 world = new Vector3(sx, sy, 0f);
        worldViewport.unproject(world);
        mouseWorld.set(world.x, world.y);

        mouseWorld.set(sx, Gdx.graphics.getHeight() - sy);


        // Menyesuaikan posisi mouse dengan ukuran area game (Sen)
        mouseWorld.scl(GameMap.WORLD_WIDTH / Gdx.graphics.getWidth(), (GameMap.WORLD_HEIGHT + RACK_HEIGHT) / Gdx.graphics.getHeight());

        // Mengurangi offset agar posisi mouse sesuai dengan map (Sen)
        mouseWorld.y -= RACK_HEIGHT;

        updatePlacementValidity();
    }

    private void updatePlacementValidity() {

        if (previewTower == null) {
            placementValid = false;
            return;
        }

        float r = previewTower.getCollisionRadius();

        placementValid = mouseWorld.x >= r && mouseWorld.y >= r && mouseWorld.x <= GameMap.WORLD_WIDTH - r && mouseWorld.y <= GameMap.WORLD_HEIGHT - r && !map.intersectsPath(mouseWorld.x, mouseWorld.y, r);

        if (placementValid) {

            for (Tower t : towers) {
                if (t == movingTower) continue;

                if (t.getPosition().dst(mouseWorld) < r + t.getCollisionRadius()) {
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

        if (state instanceof PlayingState || state instanceof PlacementState) {

            for (Tower t : towers) {
                t.setStunned(false);
            }

            for (Enemy e : enemies) {
                e.update(delta);
                e.applyTowerEffects(towers);
            }

            for (Tower t : towers) {
                t.update(delta, enemies);

            }
            projectilePool.update(delta, enemies);
            splashIndicators.update(delta);
            removeInactiveEnemies();
        }

        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        shapes.setProjectionMatrix(worldCamera.combined);
        drawMap();

        // Menggambar background tower rack di bawah map (sen)

        // Menggambar panel dan tiga slot Tower Rack semua sen
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Background panel bawah
        shapes.setColor(0.34f, 0.37f, 0.42f, 1f);
        shapes.rect(0, -RACK_HEIGHT, WORLD_WIDTH, RACK_HEIGHT);

        // Tiga slot tower
        int rackStartX = Math.round((WORLD_WIDTH - (3 * 160 + 2 * 18)) / 2f);

        for (int i = 0; i < 3; i++) {
            int x = rackStartX + i * 178;

            // Outline slot
            shapes.setColor(0.78f, 0.81f, 0.86f, 1f);
            shapes.rect(x, -160, 160, 140);

            // Bagian dalam slot
            shapes.setColor(0.48f, 0.52f, 0.58f, 1f);
            shapes.rect(x + 4, -156, 152, 132);
        }

        shapes.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawSelectedTowerRange();
        batch.begin();


        // Menampilkan placeholder HP dan Money (sen)
        //rackFont.setColor(Color.WHITE);

        //rackFont.draw(batch, "Base HP: 100", 25, GameMap.WORLD_HEIGHT - 18);
        //rackFont.draw(batch, "Money: $ 1000", 25, GameMap.WORLD_HEIGHT - 53);

        // Menampilkan placeholder Wave di pojok kanan atas (sen)
        waveFont.draw(batch, "Wave: 1", GameMap.WORLD_WIDTH - 130, GameMap.WORLD_HEIGHT - 25);

        // Tower selection labels are rendered by the HUD buttons.

        //cannon renders first so it goes below towers epicly
        projectilePool.drawCannonProjectiles(batch);

        for (Enemy e : enemies) {
            e.draw(batch);
        }

        for (Tower t : towers) {
            t.draw(batch);
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
                    blastCrawlerTexture,
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
        shapes.setColor(0.055f, 0.075f, 0.09f, 1f);
        shapes.rect(GameMap.WORLD_WIDTH, -RACK_HEIGHT, WORLD_WIDTH - GameMap.WORLD_WIDTH, WORLD_HEIGHT);

        for (int y = 0; y < GameMap.HEIGHT; y++) {

            for (int x = 0; x < GameMap.WIDTH; x++) {

                if (map.getTile(x, y) == 1) {
                    shapes.setColor(0.62f, 0.43f, 0.25f, 1);
                } else {
                    shapes.setColor(0.20f, 0.43f, 0.25f, 1);
                }

                shapes.rect(x * 40, (17 - y) * 40, 40, 40);
            }
        }

        shapes.end();
        //gambar outline pathnya SEN
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.35f, 0.22f, 0.12f, 1f);

        for (int  y = 0; y < GameMap.HEIGHT; y++) {
            for (int x = 0; x < GameMap.WIDTH; x++) {
                if(map. getTile(x, y) != 1)  {
                    continue;
                }

                float px = x * GameMap.TILE_SIZE;
                float py = (GameMap.HEIGHT - 1 - y) * GameMap.TILE_SIZE;
                float size = GameMap.TILE_SIZE;

                if(map.getTile(x, y - 1) != 1) {
                    shapes.line(px, py + size, px +size, py + size);
                }
            }
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

        // Shared splash indicators (Mortar, Caesar Shield, and future splash towers).
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        splashIndicators.draw(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // window stuff

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (worldViewport != null) {
            worldViewport.update(width, height, true);
            worldCamera.position.set(WORLD_WIDTH / 2f, GameMap.WORLD_HEIGHT / 2f - RACK_HEIGHT / 2f, 0f);
            worldCamera.update();
            batch.setProjectionMatrix(worldCamera.combined);
            shapes.setProjectionMatrix(worldCamera.combined);
        }
        if (hud != null) hud.resize(width, height);
        if (menu != null) menu.resize(width, height);
    }

    @Override
    public void dispose() {

        menu.dispose();
        hud.dispose();

        batch.dispose();
        shapes.dispose();

        cannonTexture.dispose();
        mortarTexture.dispose();
        caesarShieldTexture.dispose();

        cannonballTexture.dispose();
        mortarRoundTexture.dispose();

        blastCrawlerTexture.dispose();
        alpecaTexture.dispose();
        faunTexture.dispose();
        hoplitaiTexture.dispose();
        freddyTexture.dispose();

        FontFactory.dispose();
    }
}
