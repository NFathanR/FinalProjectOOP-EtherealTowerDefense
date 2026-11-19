package com.etherealtowerdefense.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class GameHUD implements Disposable {

    public static class TowerOption {
        public final String id;
        public final String name;

        public TowerOption(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public interface TowerSelectListener {
        void onTowerSelected(String towerId);
    }

    private static final float BAR_WIDTH = 240f;
    private static final float BAR_HEIGHT = 18f;
    private static final float BUTTON_WIDTH = 130f;
    private static final float BUTTON_HEIGHT = 64f;

    private final Stage stage;
    private final BitmapFont font;
    private final Texture whiteTexture;
    private final TextureRegionDrawable white;

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

    public GameHUD(Viewport viewport, List<TowerOption> options, TowerSelectListener listener) {
        this.options = new ArrayList<>(options);
        this.listener = listener;
        this.stage = new Stage(viewport);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        white = new TextureRegionDrawable(new TextureRegion(whiteTexture));

        font = new BitmapFont();
        font.getData().setScale(1.4f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Pixmap coinPixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        coinPixmap.setColor(0.72f, 0.50f, 0.05f, 1f);
        coinPixmap.fillCircle(16, 16, 15);
        coinPixmap.setColor(0.98f, 0.80f, 0.20f, 1f);
        coinPixmap.fillCircle(16, 16, 12);
        coinPixmap.setColor(1f, 0.93f, 0.55f, 1f);
        coinPixmap.fillCircle(12, 12, 4);
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
        buttonStyle.up = tinted(new Color(0.20f, 0.24f, 0.30f, 0.95f), 0f, 0f);
        buttonStyle.down = tinted(new Color(0.12f, 0.15f, 0.20f, 0.95f), 0f, 0f);
        buttonStyle.checked = tinted(new Color(0.20f, 0.55f, 0.35f, 0.95f), 0f, 0f);
        buttonStyle.disabled = tinted(new Color(0.12f, 0.12f, 0.12f, 0.60f), 0f, 0f);

        hpGreen = tinted(new Color(0.25f, 0.80f, 0.30f, 1f), 0f, BAR_HEIGHT);
        hpYellow = tinted(new Color(0.95f, 0.80f, 0.20f, 1f), 0f, BAR_HEIGHT);
        hpRed = tinted(new Color(0.85f, 0.20f, 0.20f, 1f), 0f, BAR_HEIGHT);
        hpStyle = new ProgressBar.ProgressBarStyle();
        hpStyle.background = tinted(new Color(0.15f, 0.15f, 0.15f, 0.9f), 0f, BAR_HEIGHT);
        hpStyle.knobBefore = hpGreen;
        hpBar = new ProgressBar(0f, 1f, 0.001f, false, hpStyle);
        hpBar.setAnimateDuration(0.25f);
        hpLabel = new Label("", labelStyle);

        Table towerBar = new Table();
        towerBar.setBackground(tinted(new Color(0f, 0f, 0f, 0.6f), 0f, 0f));
        towerBar.setTouchable(Touchable.enabled);
        towerBar.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        for (final TowerOption option : this.options) {
            TextButton button = new TextButton(option.name, buttonStyle);
            button.setProgrammaticChangeEvents(false);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    select(option.id.equals(selectedId) ? null : option.id);
                }
            });
            buttons.add(button);
            towerBar.add(button).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(8f);
        }

        Table hpBox = new Table();
        hpBox.add(hpLabel).left().row();
        hpBox.add(hpBar).width(BAR_WIDTH).height(BAR_HEIGHT).left().padTop(4f);

        Table goldBox = new Table();
        goldBox.add(new Image(coin)).size(28f);
        goldBox.add(goldLabel).padLeft(8f);

        Table top = new Table();
        top.add(hpBox).left().top();
        top.add().expandX();
        top.add(goldBox).right().top();

        Table root = new Table();
        root.setFillParent(true);
        root.add(top).growX().pad(12f).row();
        root.add().grow().row();
        root.add(towerBar).growX();
        stage.addActor(root);

        onBaseHpChanged(1, 1);
    }


    public void onBaseHpChanged(int current, int max) {
        int safeMax = Math.max(max, 1);
        int safeCurrent = Math.max(0, Math.min(current, safeMax));
        float percent = safeCurrent / (float) safeMax;

        hpBar.setValue(percent);
        hpLabel.setText("Base HP: " + safeCurrent + " / " + safeMax);
        hpStyle.knobBefore = percent > 0.5f ? hpGreen : (percent > 0.25f ? hpYellow : hpRed);
    }

    public void onGoldChanged(int gold) {
        goldLabel.setText(String.valueOf(gold));
    }

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

    public void setSelectedTower(String id) {
        selectedId = id;
        refreshSelection();
    }

    public void clearSelection() {
        setSelectedTower(null);
    }

    public String getSelectedTowerId() {
        return selectedId;
    }


    public Stage getStage() {
        return stage;
    }

    public void render(float delta) {
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        whiteTexture.dispose();
        coinTexture.dispose();
    }


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

    private Drawable tinted(Color color, float minWidth, float minHeight) {
        Drawable drawable = white.tint(color);
        drawable.setMinWidth(minWidth);
        drawable.setMinHeight(minHeight);
        return drawable;
    }
}
