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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20; //Bantu transparan(sen)

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
    // Tinggi area Tower Rack di bawah map (sen)
    private static final int RACK_HEIGHT = 180;
    // Font untuk label placeholder tower rack (sen)
    private BitmapFont rackFont;

    private Texture cannonTexture;
    private Texture mortarTexture;
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
        // Menggeser koordinat gambar agar tersedia ruang Tower Rack (sen)
        shapes.getProjectionMatrix().setToOrtho2D(0, -RACK_HEIGHT, 1280, 900);
        // Menyiapkan font putih untuk nama tower (sen)
        rackFont = new BitmapFont();
        rackFont.setColor(Color.WHITE);
        rackFont.getData().setScale(1.5f);
        map = new GameMap();


        // Menggeser tampilm game keatas bair muat buat rack towernya (sen)
        batch.getProjectionMatrix().setToOrtho2D(0, -RACK_HEIGHT, GameMap.WORLD_WIDTH, GameMap.WORLD_HEIGHT + RACK_HEIGHT);

        // Menyamakan koordinat ShapeRenderer dan SpriteBatch (sen)
        shapes.setProjectionMatrix(batch.getProjectionMatrix());


        cannonTexture = new Texture("towers/cannon.png");
        mortarTexture = new Texture("towers/mortar.png");

        cannonballTexture = new Texture("projectiles/cannonball.png");
        mortarRoundTexture = new Texture("projectiles/mortar_round.png");

        blastCrawlerTexture = new Texture("enemies/BlastCrawler.png");
        alpecaTexture = new Texture("enemies/Alpeca.png");
        faunTexture = new Texture("enemies/Faun.png");
        hoplitaiTexture = new Texture("enemies/Hoplitai.png");
        freddyTexture = new Texture("enemies/EthericNightmare-FreddyFazbear.png");

        projectilePool = new ProjectilePool();

        towerFactory = new TowerFactory(
            projectilePool,
            cannonTexture,
            cannonballTexture,
            mortarTexture,
            mortarRoundTexture
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


        // Menyesuaikan posisi mouse dengan ukuran area game (Sen)
        mouseWorld.scl(
            GameMap.WORLD_WIDTH / Gdx.graphics.getWidth(),
            (GameMap.WORLD_HEIGHT + RACK_HEIGHT) / Gdx.graphics.getHeight()
        );

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
            removeInactiveEnemies();
        }

        drawMap();

        // Mengaktifkan blending untuk panel transparan(sen)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Menggambar background placeholder HP dan Money (sen)
        shapes.begin(ShapeRenderer.ShapeType.Filled);


        // Warna cokelat gelap dengan transparansi 75% (sen)
        shapes.setColor(0.30f, 0.19f, 0.12f, 0.75f);

        // Panel rasio 7:2, menempel di pojok kiri atas (sen)
        shapes.rect(0, GameMap.WORLD_HEIGHT - 80, 280, 80);


        // Mengembalikan rendering ke kondisi normal (sen)
        Gdx.gl.glDisable(GL20.GL_BLEND);
        shapes.end();

        // Mengembalikan rendering ke kondisi normal (sen)
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Menggambar background tower rack di bawah map (sen)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.36f, 0.23f, 0.14f, 1f);
        shapes.rect(0, -RACK_HEIGHT, GameMap.WORLD_WIDTH, RACK_HEIGHT);


        // Menggambar 7 slot Tower Rack (sen)
        for (int i = 0; i < 7; i++) {
            int x = 26 + i * 178;

            // Outline cokelat muda (sen)
            shapes.setColor(0.78f, 0.60f, 0.42f, 1f);
            shapes.rect(x, -160, 160, 140);

            // Bagian dalam slot berwarna cokelat (sen)
            shapes.setColor(0.58f, 0.40f, 0.25f, 1f);
            shapes.rect(x + 4, -156, 152, 132);
        }

        shapes.end();
        batch.begin();


        // Menampilkan placeholder HP dan Money (sen)
        rackFont.setColor(Color.WHITE);

        rackFont.draw(batch, "Base HP: 100", 25, GameMap.WORLD_HEIGHT - 18);
        rackFont.draw(batch, "Money: $ 1000", 25, GameMap.WORLD_HEIGHT - 53);

        // Menampilkan placeholder Wave di pojok kanan atas (sen)
        rackFont.getData().setScale(2f);

        rackFont.draw(batch, "Wave: 1", GameMap.WORLD_WIDTH - 130, GameMap.WORLD_HEIGHT - 25);

        // Mengembalikan ukuran font untuk teks lainnya (Sen)
        rackFont.getData().setScale(1.5f);

        // Menggambar label untuk setiap slot tower rack (sen)
        for (int i = 0; i < 7; i++) {
            int x = 26 + i * 178;
            rackFont.draw(batch, "Tower " + (i + 1), x + 40, -80);
        }

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

        blastCrawlerTexture.dispose();
        alpecaTexture.dispose();
        faunTexture.dispose();
        hoplitaiTexture.dispose();
        freddyTexture.dispose();

        // Membersihkan resource font (sen)
        rackFont.dispose();
    }
}
