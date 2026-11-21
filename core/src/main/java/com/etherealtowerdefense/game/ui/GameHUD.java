package com.etherealtowerdefense.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.etherealtowerdefense.game.factories.FontFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD saat bermain (tanpa file skin/asset tambahan):
 *  - kiri atas  : HP base (label + bar yang berubah warna)
 *  - kanan atas : jumlah gold (ikon koin + angka)
 *  - bawah      : menu untuk memilih tower yang tersedia
 *  - popup      : angka "+gold" yang naik dan memudar di posisi musuh yang mati
 *
 * Pemakaian (lihat EtherealTDGame):
 *   hud = new GameHUD(new StretchViewport(1280, 900), -180f, options, id -> ...);  // id == null = batal pilih
 *   Koordinat HUD = koordinat dunia game (peta y 0..720, Tower Rack y -180..0), jadi worldMinY = -RACK_HEIGHT.
 *   Gdx.input.setInputProcessor(new InputMultiplexer(hud.getStage(), gameInput));
 *   hud.onBaseHpChanged(hp, maxHp);
 *   render(): gambar game dulu, lalu hud.render(delta);
 *   resize(): hud.resize(w, h);   dispose(): hud.dispose();
 */
public class GameHUD implements Disposable {

    /** Satu tombol tower di menu bawah. */
    public static class TowerOption {
        public final String id;
        public final String name;

        public TowerOption(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /** Dipanggil saat pemain memilih tower (id) atau membatalkan pilihan (null). */
    public interface TowerSelectListener {
        void onTowerSelected(String towerId);
    }

    private static final float BAR_WIDTH = 240f;
    private static final float BAR_HEIGHT = 18f;
    // Tombol tower memakai ukuran/posisi yang sama dengan slot Tower Rack yang digambar di EtherealTDGame:
    // x = 26 + i * 178, y = -160, ukuran 160x140, tepi 4 px (dasar rack di y = -180).
    private static final float RACK_HEIGHT = 180f;   // tinggi Tower Rack; area ini juga menyerap klik
    private static final float SLOT_WIDTH = 160f;
    private static final float SLOT_HEIGHT = 140f;
    private static final float SLOT_PITCH = 178f;    // jarak antar slot (tepi kiri ke tepi kiri)
    private static final float SLOT_LEFT = 26f;      // jarak slot pertama dari tepi kiri
    private static final float SLOT_BOTTOM = 20f;    // jarak slot dari dasar rack (y = -160)

    private final Stage stage;
    private final float worldMinY;
    private final BitmapFont font;
    private final Texture whiteTexture;
    private final TextureRegionDrawable white;
    private final List<Texture> extraTextures = new ArrayList<>();

    private final ProgressBar.ProgressBarStyle hpStyle;
    private final ProgressBar hpBar;
    private final Drawable hpGreen;
    private final Drawable hpYellow;
    private final Drawable hpRed;
    private final Label hpLabel;

    private final Texture coinTexture;
    private final TextureRegionDrawable coin;
    private final Label.LabelStyle goldStyle;
    private final Label goldLabel;

    private final List<TowerOption> options;
    private final List<TextButton> buttons = new ArrayList<>();
    private final TowerSelectListener listener;
    private String selectedId;

    /**
     * @param worldMinY batas bawah dunia HUD (mis. -180 agar Tower Rack berada di y -180..0, sama dengan game)
     */
    public GameHUD(Viewport viewport, float worldMinY, List<TowerOption> options, TowerSelectListener listener) {
        this.worldMinY = worldMinY;
        this.options = new ArrayList<>(options);
        this.listener = listener;
        this.stage = new Stage(viewport);

        // Texture 1x1 putih untuk menggambar bar dan background tanpa file gambar.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        white = new TextureRegionDrawable(new TextureRegion(whiteTexture));

        // Font TrueType via FreeType (lihat FontFactory).
        font = FontFactory.get(22);   // font bersama (tajam, tanpa setScale); di-dispose oleh FontFactory

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        // Ikon koin emas digambar dari kode (tanpa file gambar).
        Pixmap coinPixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        coinPixmap.setColor(0.72f, 0.50f, 0.05f, 1f);
        coinPixmap.fillCircle(16, 16, 15);   // tepi koin
        coinPixmap.setColor(0.98f, 0.80f, 0.20f, 1f);
        coinPixmap.fillCircle(16, 16, 12);   // badan koin
        coinPixmap.setColor(1f, 0.93f, 0.55f, 1f);
        coinPixmap.fillCircle(12, 12, 4);    // kilau
        coinTexture = new Texture(coinPixmap);
        coinPixmap.dispose();
        coinTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        coin = new TextureRegionDrawable(new TextureRegion(coinTexture));

        goldStyle = new Label.LabelStyle(font, new Color(1f, 0.85f, 0.2f, 1f));
        goldLabel = new Label("0", goldStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.disabledFontColor = Color.GRAY;
        // Warna sama dengan slot Tower Rack: isi (0.58, 0.40, 0.25), tepi (0.78, 0.60, 0.42); terpilih = tepi emas.
        buttonStyle.up = bordered(new Color(0.58f, 0.40f, 0.25f, 1f), new Color(0.78f, 0.60f, 0.42f, 1f));
        buttonStyle.down = bordered(new Color(0.46f, 0.32f, 0.20f, 1f), new Color(0.66f, 0.50f, 0.35f, 1f));
        buttonStyle.checked = bordered(new Color(0.58f, 0.40f, 0.25f, 1f), new Color(1f, 0.85f, 0.30f, 1f));
        buttonStyle.disabled = bordered(new Color(0.36f, 0.26f, 0.18f, 1f), new Color(0.50f, 0.40f, 0.30f, 1f));

        // HP bar: background gelap + isi yang warnanya mengikuti sisa HP.
        hpGreen = tinted(new Color(0.25f, 0.80f, 0.30f, 1f), 0f, BAR_HEIGHT);
        hpYellow = tinted(new Color(0.95f, 0.80f, 0.20f, 1f), 0f, BAR_HEIGHT);
        hpRed = tinted(new Color(0.85f, 0.20f, 0.20f, 1f), 0f, BAR_HEIGHT);
        hpStyle = new ProgressBar.ProgressBarStyle();
        hpStyle.background = tinted(new Color(0.15f, 0.15f, 0.15f, 0.9f), 0f, BAR_HEIGHT);
        hpStyle.knobBefore = hpGreen;
        hpBar = new ProgressBar(0f, 1f, 0.001f, false, hpStyle);
        hpBar.setAnimateDuration(0.25f);
        hpLabel = new Label("", labelStyle);

        // Menu tower di bawah.
        Table towerBar = new Table();
        towerBar.left().bottom();   // tanpa background sendiri: memakai bar coklat yang sudah ada
        // Menyerap klik di area menu supaya tidak tembus ke peta di bawahnya.
        towerBar.setTouchable(Touchable.enabled);
        towerBar.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        for (final TowerOption option : this.options) {
            TextButton button = new TextButton(option.name, buttonStyle);
            // Perubahan status dari kode tidak memicu event; hanya klik pemain yang memicu.
            button.setProgrammaticChangeEvents(false);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Klik tombol yang sama sekali lagi = batal pilih.
                    select(option.id.equals(selectedId) ? null : option.id);
                }
            });
            buttons.add(button);
            towerBar.add(button).size(SLOT_WIDTH, SLOT_HEIGHT)
                .padLeft(buttons.size() == 1 ? SLOT_LEFT : SLOT_PITCH - SLOT_WIDTH)
                .padBottom(SLOT_BOTTOM);
        }

        // Susunan layar: baris atas (HP), pengisi ruang, menu tower di bawah.
        Table hpBox = new Table();
        hpBox.add(hpLabel).left().row();
        hpBox.add(hpBar).width(BAR_WIDTH).height(BAR_HEIGHT).left().padTop(4f);

        Table goldBox = new Table();
        goldBox.add(new Image(coin)).size(28f);
        goldBox.add(goldLabel).padLeft(8f);

        Table top = new Table();
        top.add(hpBox).left().top();
        top.add().expandX();
        top.add(goldBox).right().top().padTop(48f).padRight(10f);   // di bawah label "Wave"

        Table root = new Table();
        // Bukan setFillParent: dunia game mulai dari y = worldMinY (rack berada di bawah y = 0).
        root.setBounds(0f, worldMinY, viewport.getWorldWidth(), viewport.getWorldHeight());
        root.add(top).growX().pad(12f).row();
        root.add().grow().row();
        root.add(towerBar).growX().height(RACK_HEIGHT);
        stage.addActor(root);

        onBaseHpChanged(1, 1);
        updateCamera();
    }

    // ---------- API untuk game ----------

    /** Panggil saat HP base berubah (dan sekali di awal untuk sinkronisasi). */
    public void onBaseHpChanged(int current, int max) {
        int safeMax = Math.max(max, 1);
        int safeCurrent = Math.max(0, Math.min(current, safeMax));
        float percent = safeCurrent / (float) safeMax;

        hpBar.setValue(percent);
        hpLabel.setText("Base HP: " + safeCurrent + " / " + safeMax);
        hpStyle.knobBefore = percent > 0.5f ? hpGreen : (percent > 0.25f ? hpYellow : hpRed);
    }

    /** Memperbarui jumlah gold di kanan atas. */
    public void onGoldChanged(int gold) {
        goldLabel.setText(String.valueOf(gold));
    }

    /**
     * Memunculkan angka "+amount" dengan ikon koin di posisi dunia (x, y); naik pelan lalu memudar.
     * Koordinat dunia sama dengan koordinat viewport HUD (1280x720).
     */
    public void showGoldPopup(float worldX, float worldY, int amount) {
        Table popup = new Table();
        popup.setTouchable(Touchable.disabled);
        popup.add(new Image(coin)).size(20f);
        popup.add(new Label("+" + amount, goldStyle)).padLeft(4f);
        popup.pack();
        popup.setPosition(worldX - popup.getWidth() / 2f, worldY);
        popup.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0f, 48f, 0.9f, Interpolation.pow2Out),
                Actions.sequence(Actions.delay(0.4f), Actions.fadeOut(0.5f))
            ),
            Actions.removeActor()
        ));
        stage.addActor(popup);
    }

    /** Menandai tower yang sedang dipilih di menu (tanpa memanggil listener), mis. dari hotkey. */
    public void setSelectedTower(String id) {
        selectedId = id;
        refreshSelection();
    }

    /** Menghapus tanda pilihan (tanpa memanggil listener), mis. setelah tower ditaruh. */
    public void clearSelection() {
        setSelectedTower(null);
    }

    public String getSelectedTowerId() {
        return selectedId;
    }

    // ---------- Siklus hidup ----------

    public Stage getStage() {
        return stage;
    }

    public void render(float delta) {
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
        updateCamera();
    }

    /** Menggeser kamera HUD agar koordinatnya sama dengan dunia game (y mulai dari worldMinY). */
    private void updateCamera() {
        Viewport viewport = stage.getViewport();
        viewport.getCamera().position.set(
            viewport.getWorldWidth() / 2f,
            worldMinY + viewport.getWorldHeight() / 2f,
            0f
        );
        viewport.getCamera().update();
    }

    @Override
    public void dispose() {
        stage.dispose();
        whiteTexture.dispose();
        coinTexture.dispose();
        for (Texture texture : extraTextures) {
            texture.dispose();
        }
    }

    // ---------- Internal ----------

    private void select(String id) {
        selectedId = id;
        refreshSelection();
        if (listener != null) {
            listener.onTowerSelected(id);
        }
    }

    private void refreshSelection() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setChecked(options.get(i).id.equals(selectedId));
        }
    }

    /** Kotak berborder 4 piksel (isi + tepi) yang bisa diregangkan, digambar dari kode. */
    private Drawable bordered(Color fill, Color border) {
        Pixmap pixmap = new Pixmap(12, 12, Pixmap.Format.RGBA8888);
        pixmap.setColor(border);
        pixmap.fill();
        pixmap.setColor(fill);
        pixmap.fillRectangle(4, 4, 4, 4);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        extraTextures.add(texture);
        return new NinePatchDrawable(new NinePatch(texture, 4, 4, 4, 4));
    }

    private Drawable tinted(Color color, float minWidth, float minHeight) {
        Drawable drawable = white.tint(color);
        drawable.setMinWidth(minWidth);
        drawable.setMinHeight(minHeight);
        return drawable;
    }
}
