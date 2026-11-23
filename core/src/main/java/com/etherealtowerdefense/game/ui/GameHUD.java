package com.etherealtowerdefense.game.ui;

import com.badlogic.gdx.Gdx;
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
import com.etherealtowerdefense.game.entities.Tower;

import java.util.ArrayList;
import java.util.List;

public class GameHUD implements Disposable {
    public static class TowerOption {
        public final String id;
        public final String name;
        public TowerOption(String id, String name) { this.id = id; this.name = name; }
    }

    /** Dipanggil saat pemain memilih tower (id) atau membatalkan pilihan (null). */
    public interface TowerSelectListener {
        void onTowerSelected(String towerId);
    }

    private static final float BAR_WIDTH = 240f;
    private static final float BAR_HEIGHT = 18f;
    private static final float RACK_HEIGHT = 180f;
    private static final float SLOT_WIDTH = 160f;
    private static final float SLOT_HEIGHT = 140f;
    private static final float SLOT_PITCH = 178f;
    private static final float SLOT_LEFT = 26f;
    private static final float SLOT_BOTTOM = 20f;
    private static final float START_BUTTON_WIDTH = 220f;
    private static final float START_BUTTON_HEIGHT = 42f;
    private static final float TOP_MARGIN = 12f;

    private final Stage stage;
    private final float worldMinY;
    private final BitmapFont font;
    private final Texture whiteTexture;
    private final TextureRegionDrawable white;
    private final List<Texture> extraTextures = new ArrayList<>();

    private final ProgressBar.ProgressBarStyle hpStyle;
    private final ProgressBar hpBar;
    private final Drawable hpGreen, hpYellow, hpRed;
    private final Label hpLabel;
    private final Texture dennyTexture;
    private final TextureRegionDrawable denny;
    private final Label.LabelStyle dennyStyle;
    private final Label dennyLabel;
    private final List<TowerOption> options;
    private final List<TextButton> buttons = new ArrayList<>();
    private final TowerSelectListener listener;
    private final TowerSidebar towerSidebar;
    private String selectedId;
    private TextButton startWaveButton;

    public GameHUD(Viewport viewport, List<TowerOption> options,
                   TowerSelectListener listener, TowerSidebar.ActionsListener sidebarListener) {
        this.worldMinY = -RACK_HEIGHT;
        this.options = new ArrayList<>(options);
        this.listener = listener;
        stage = new Stage(viewport);

        // Texture 1x1 putih untuk menggambar bar dan background tanpa file gambar.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        white = new TextureRegionDrawable(new TextureRegion(whiteTexture));

        font = FontFactory.create(22f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        dennyTexture = new Texture(Gdx.files.internal("icons/icon_denny.png"));
        dennyTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        denny = new TextureRegionDrawable(new TextureRegion(dennyTexture));
        dennyStyle = new Label.LabelStyle(font, Color.WHITE);
        dennyLabel = new Label("0", dennyStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.disabledFontColor = Color.LIGHT_GRAY;
        buttonStyle.up = bordered(new Color(.58f,.61f,.66f,1f), new Color(.80f,.83f,.87f,1f));
        buttonStyle.down = bordered(new Color(.40f,.44f,.50f,1f), new Color(.68f,.72f,.78f,1f));
        buttonStyle.checked = bordered(new Color(.58f,.61f,.66f,1f), new Color(.94f,.96f,1f,1f));
        buttonStyle.disabled = bordered(new Color(.30f,.32f,.36f,1f), new Color(.48f,.50f,.54f,1f));

        hpGreen = tinted(new Color(.25f,.80f,.30f,1f), 0f, BAR_HEIGHT);
        hpYellow = tinted(new Color(.95f,.80f,.20f,1f), 0f, BAR_HEIGHT);
        hpRed = tinted(new Color(.85f,.20f,.20f,1f), 0f, BAR_HEIGHT);
        hpStyle = new ProgressBar.ProgressBarStyle();
        hpStyle.background = tinted(new Color(.15f,.15f,.15f,.9f), 0f, BAR_HEIGHT);
        hpStyle.knobBefore = hpGreen;
        hpBar = new ProgressBar(0f, 1f, .001f, false, hpStyle);
        hpBar.setAnimateDuration(.25f);
        hpLabel = new Label("", labelStyle);

        Table towerBar = new Table();
        towerBar.left().bottom();
        towerBar.setTouchable(Touchable.enabled);
        towerBar.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        float rackContentWidth = options.size() * SLOT_WIDTH + Math.max(0, options.size() - 1) * (SLOT_PITCH - SLOT_WIDTH);
        float firstSlotPadding = Math.max(0f, (1600f - rackContentWidth) / 2f);
        for (final TowerOption option : this.options) {
            TextButton button = new TextButton(option.name, buttonStyle);

            button.getLabel().setFontScale(0.75f);
            button.getLabel().setWrap(true);
            button.getLabel().setAlignment(com.badlogic.gdx.utils.Align.center);

            button.setProgrammaticChangeEvents(false);
            button.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    select(option.id.equals(selectedId) ? null : option.id);
                }
            });

            buttons.add(button);
            towerBar.add(button).size(SLOT_WIDTH, SLOT_HEIGHT)
                .padLeft(buttons.size() == 1 ? firstSlotPadding : SLOT_PITCH - SLOT_WIDTH)
                .padBottom(SLOT_BOTTOM);
        }

        // Susunan layar: baris atas (HP), pengisi ruang, menu tower di bawah.
        Table hpBox = new Table();
        hpBox.add(hpLabel).left().row();
        hpBox.add(hpBar).width(BAR_WIDTH).height(BAR_HEIGHT).left().padTop(4f);
        Table dennyBox = new Table();
        dennyBox.add(new Image(denny)).size(28f);
        dennyBox.add(dennyLabel).padLeft(8f);
        Table top = new Table();
        top.add(hpBox).left().top();
        top.add().expandX();
        top.add(dennyBox).right().top().padTop(48f).padRight(10f);

        Table topArea = new Table();
        topArea.add(top).growX().pad(12f).row();
        topArea.add().grow();

        Table root = new Table();
        root.setBounds(
            0f,
            worldMinY,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );

        root.add(topArea).width(1280f).growY();
        root.add().width(320f).growY();
        root.row();

        root.add(towerBar).colspan(2).width(1600f)
            .height(RACK_HEIGHT).growX();

        stage.addActor(root);

        towerSidebar = new TowerSidebar(sidebarListener);
        stage.addActor(towerSidebar);
        onBaseHpChanged(1, 1);
        updateCamera();
    }

    public void onBaseHpChanged(int current, int max) {
        int safeMax = Math.max(max, 1);
        int safeCurrent = Math.max(0, Math.min(current, safeMax));
        float percent = safeCurrent / (float) safeMax;
        hpBar.setValue(percent);
        hpLabel.setText("Base HP: " + safeCurrent + " / " + safeMax);
        hpStyle.knobBefore = percent > .5f ? hpGreen : (percent > .25f ? hpYellow : hpRed);
    }

    public void onDennyChanged(int amount) { dennyLabel.setText(String.valueOf(amount)); }

    public void showDennyPopup(float worldX, float worldY, int amount) {
        Table popup = new Table();
        popup.setTouchable(Touchable.disabled);
        popup.add(new Image(denny)).size(20f);
        popup.add(new Label("+" + amount, dennyStyle)).padLeft(4f);
        popup.pack();
        popup.setPosition(worldX - popup.getWidth()/2f, worldY);
        popup.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0f, 48f, .9f, Interpolation.pow2Out),
                Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.5f))
            ), Actions.removeActor()
        ));
        stage.addActor(popup);
    }

    public void setSelectedTower(String id) { selectedId = id; refreshSelection(); }
    public void clearSelection() { setSelectedTower(null); towerSidebar.hide(); }
    public void showTowerSidebar(Tower tower) { towerSidebar.show(tower); }
    public String getSelectedTowerId() { return selectedId; }

    /** Adds the manual wave-start control. Boss testing is done with keyboard L. */
    public void setWaveControls(Runnable startWave) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.up = bordered(new Color(.18f, .48f, .25f, 1f), new Color(.72f, .88f, .74f, 1f));
        style.down = bordered(new Color(.12f, .34f, .18f, 1f), Color.WHITE);
        style.disabled = bordered(new Color(.28f, .28f, .28f, 1f), Color.GRAY);

        Table controls = new Table();
        startWaveButton = new TextButton("Start Next Wave", style);
        startWaveButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (startWave != null) startWave.run();
            }
        });
        controls.add(startWaveButton).width(START_BUTTON_WIDTH).height(START_BUTTON_HEIGHT);
        // Beri ukuran eksplisit supaya tombol tidak "melayang" di titik tengah tabel berukuran 0.
        controls.setSize(START_BUTTON_WIDTH, START_BUTTON_HEIGHT);
        // Tepi atas layar HUD = worldMinY + worldHeight (bukan 0), jadi tombol menempel di atas.
        float screenTop = worldMinY + stage.getViewport().getWorldHeight();
        controls.setPosition((stage.getViewport().getWorldWidth() - START_BUTTON_WIDTH) / 2f,
            screenTop - TOP_MARGIN - START_BUTTON_HEIGHT);
        stage.addActor(controls);
    }

    public Stage getStage() { return stage; }
    public void render(float delta) { stage.getViewport().apply(); stage.act(delta); stage.draw(); }
    public void resize(int width, int height) {
        // centerCamera=false: kalau true, kamera di-reset ke (w/2, h/2) sehingga seluruh HUD
        // (HP, Start Wave, Denny, rack tower) bergeser turun sebesar RACK_HEIGHT.
        stage.getViewport().update(width, height, false);
        updateCamera();
    }
    private void updateCamera() {
        Viewport viewport = stage.getViewport();

        viewport.getCamera().position.set(
            viewport.getWorldWidth() / 2f,
            worldMinY + viewport.getWorldHeight() / 2f,
            0f
        );

        viewport.getCamera().update();
    }

    @Override public void dispose() {
        stage.dispose();
        whiteTexture.dispose();
        dennyTexture.dispose();
        for (Texture texture : extraTextures) texture.dispose();
        towerSidebar.dispose();
    }

    private void select(String id) {
        selectedId = id;
        refreshSelection();
        if (listener != null) listener.onTowerSelected(id);
    }
    private void refreshSelection() {
        for (int i=0;i<buttons.size();i++) buttons.get(i).setChecked(options.get(i).id.equals(selectedId));
    }
    private Drawable bordered(Color fill, Color border) {
        Pixmap pixmap = new Pixmap(12,12,Pixmap.Format.RGBA8888);
        pixmap.setColor(border); pixmap.fill();
        pixmap.setColor(fill); pixmap.fillRectangle(4,4,4,4);
        Texture texture = new Texture(pixmap); pixmap.dispose();
        extraTextures.add(texture);
        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
            new com.badlogic.gdx.graphics.g2d.NinePatch(texture,4,4,4,4));
    }
    private Drawable tinted(Color color, float minWidth, float minHeight) {
        Drawable drawable = white.tint(color);
        drawable.setMinWidth(minWidth); drawable.setMinHeight(minHeight);
        return drawable;
    }
}
