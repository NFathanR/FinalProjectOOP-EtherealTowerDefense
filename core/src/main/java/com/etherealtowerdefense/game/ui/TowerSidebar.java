package com.etherealtowerdefense.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.etherealtowerdefense.game.entities.Tower;

import java.util.ArrayList;
import java.util.List;

public class TowerSidebar extends Table implements Disposable {
    public interface ActionsListener {
        void onMove(Tower tower);
        void onSell(Tower tower);
        void onUpgrade(Tower tower, String element);
        void onPriorityChanged(Tower tower);
    }

    private static final String[] ELEMENTS = {"fire", "electric", "ice", "ether"};
    private final List<Texture> textures = new ArrayList<>();
    private final BitmapFont font = FontFactory.create(22f);
    private final BitmapFont smallFont = FontFactory.create(15f);
    private final Label.LabelStyle white = new Label.LabelStyle(font, Color.WHITE);
    private final Label.LabelStyle grey = new Label.LabelStyle(smallFont, new Color(.62f,.62f,.62f,1f));
    private final Label.LabelStyle statStyle = new Label.LabelStyle(smallFont, new Color(.68f,.68f,.68f,1f));
    private final Table stats = new Table();
    private final Table elementRow = new Table();
    private final Label elementLabel = new Label("Physical", grey);
    private final Label nameLabel = new Label("Tower", white);
    private final Label uniqueLabel = new Label("", grey);
    private final Label priorityLabel = new Label("First", white);
    private final Label moveLabel = new Label("Move", white);
    private final Label sellLabel = new Label("Sell", white);
    private final Label upgradeCost = new Label("NULL", white);
    private final Label sellValue = new Label("", white);
    private final Label buyCost = new Label("NULL", grey);
    private Image buyDenny;
    private Image sellDenny;
    private final Image towerIcon;
    private final ImageButton upgradeButton;
    private final ImageButton leftPriority;
    private final ImageButton rightPriority;
    private final ImageButton moveButton;
    private final ImageButton sellButton;
    private final ImageButton[] elementButtons = new ImageButton[4];
    private final ActionsListener listener;
    private Tower tower;
    private Texture dennyTexture;
    private boolean upgradeMode;
    private boolean confirmingSell;
    private String selectedElement = "fire";
    private boolean open;
    /** Bawah panel = bawah rack (HUD memakai kamera dengan layar y = -180 .. 720). */
    private static final float SIDEBAR_Y = -180f;
    private Image priorityBg;

    public TowerSidebar(ActionsListener listener) {
        this.listener = listener;
        setTransform(false);
        Texture background = load("ui/towersidebar.png");
        Image bg = new Image(background);
        addActor(bg);
        bg.setTouchable(Touchable.enabled);
        bg.setBounds(0, 0, 320, 900);

        bg.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(
                com.badlogic.gdx.scenes.scene2d.InputEvent event,
                float x,
                float y,
                int pointer,
                int button
            ) {
                return true;
            }
        });

        towerIcon = new Image();
        towerIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        addActor(towerIcon);
        towerIcon.setTouchable(Touchable.disabled);
        towerIcon.setBounds(196, 690, 108, 145);

        elementLabel.setFontScale(.95f);
        nameLabel.setWrap(true);
        nameLabel.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
        nameLabel.setBounds(14, 750, 176, 82);
        nameLabel.setFontScale(1.45f);
        uniqueLabel.setFontScale(.82f);
        addActor(elementLabel);
        elementLabel.setTouchable(Touchable.disabled);
        elementLabel.setPosition(40, 832);
        addActor(nameLabel);
        nameLabel.setTouchable(Touchable.disabled);
        nameLabel.setBounds(14, 744, 176, 70);
        nameLabel.setWrap(true);
        nameLabel.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
        addActor(uniqueLabel);
        uniqueLabel.setTouchable(Touchable.disabled);
        uniqueLabel.setPosition(14, 690);

        Image physical = new Image(new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(load("icons/icon_physical.png"))));
        physical.setSize(22,22); physical.setPosition(14,831);
        physical.setTouchable(Touchable.disabled);
        addActor(physical);
        elementLabel.setPosition(42,832);

        Image upgradeBase = new Image(load("ui/towersidebar_upgrade.png"));
        upgradeBase.setBounds(8, 578, 304, 49); upgradeBase.setTouchable(Touchable.disabled); addActor(upgradeBase);
        upgradeButton = imageButton("ui/towersidebar_upgrade_button.png", "ui/towersidebar_upgrade_button_click.png");
        upgradeButton.setBounds(228, 578, 76, 49);
        addActor(upgradeButton);
        upgradeButton.addListener(click(() -> toggleUpgrade()));

        addActor(elementRow); elementRow.setBounds(18, 530, 284, 36);
        elementRow.setTouchable(Touchable.childrenOnly);
        for (int i=0;i<ELEMENTS.length;i++) {
            final String element = ELEMENTS[i];
            ImageButton b = imageButton("icons/icon_"+element+".png", "icons/icon_"+element+".png");
            elementButtons[i] = b;
            elementRow.add(b).size(36,36).padRight(26);
            b.addListener(click(() -> selectElement(element)));
        }

        stats.setBounds(12, 365, 296, 205);
        stats.top().left();
        addActor(stats);

        priorityBg = new Image(makeSolid(new Color(.075f,.075f,.075f,1f)));
        priorityBg.setBounds(38, 108, 224, 45);
        priorityBg.setTouchable(Touchable.disabled);
        addActor(priorityBg);

        leftPriority = imageButton("ui/towersidebar_style_button.png", "ui/towersidebar_style_button_click.png");
        rightPriority = imageButton("ui/towersidebar_style_button.png", "ui/towersidebar_style_button_click.png");
        leftPriority.setBounds(2, 96, 80, 70);
        rightPriority.setBounds(238, 96, 80, 70);
        leftPriority.setTransform(true);
        leftPriority.setOrigin(
            com.badlogic.gdx.utils.Align.center
        );
        leftPriority.setRotation(180f);
        addActor(leftPriority); addActor(rightPriority);
        leftPriority.addListener(click(() -> cyclePriority(-1)));
        rightPriority.addListener(click(() -> cyclePriority(1)));

        priorityLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        priorityLabel.setFontScale(1.05f);
        priorityLabel.setBounds(68, 108, 180, 50);
        priorityLabel.setTouchable(Touchable.disabled); addActor(priorityLabel);

        moveButton = imageButton("ui/towersidebar_move_button.png", "ui/towersidebar_move_button_click.png");
        sellButton = imageButton("ui/towersidebar_sell_button.png", "ui/towersidebar_sell_button_click.png");
        moveButton.setBounds(13, 20, 184, 50); sellButton.setBounds(188,20,123,50);
        addActor(moveButton);
        addActor(sellButton);
        moveButton.addListener(click(() -> {
            if (tower == null || listener == null) return;
            if (upgradeMode) listener.onUpgrade(tower, selectedElement);
            else listener.onMove(tower);
        }));
        sellButton.addListener(click(() -> {
            if (tower == null) return;
            if (!confirmingSell) { confirmingSell = true; updateSellText(); }
            else if (listener != null) listener.onSell(tower);
        }));

        addActor(moveLabel);
        moveLabel.setTouchable(Touchable.disabled);
        moveLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        moveLabel.setBounds(8,20,184,50);

        addActor(sellLabel);
        sellLabel.setTouchable(Touchable.disabled);
        sellLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        sellLabel.setBounds(198,20,114,50);

        addActor(sellValue);
        sellValue.setTouchable(Touchable.disabled);
        sellValue.setAlignment(com.badlogic.gdx.utils.Align.center);
        sellValue.setBounds(198,20,114,50);

        dennyTexture = load("icons/icon_denny.png");
        buyDenny = new Image(dennyTexture);
        buyDenny.setSize(18,18);
        buyDenny.setPosition(151,36);
        buyDenny.setTouchable(Touchable.disabled);
        addActor(buyDenny);

        buyCost.setFontScale(.8f);
        buyCost.setPosition(171,34);
        buyCost.setTouchable(Touchable.disabled);
        addActor(buyCost);
        sellDenny = new Image(dennyTexture);
        sellDenny.setSize(18,18);
        sellDenny.setPosition(282,36);
        sellDenny.setTouchable(Touchable.disabled);
        addActor(sellDenny);

        setSize(320,900);
        setPosition(1600,SIDEBAR_Y);
        setVisible(false);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
        refresh();
    }

    private Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(t);
        return t;
    }
    private ImageButton imageButton(String up, String down) {
        ImageButton.ImageButtonStyle s = new ImageButton.ImageButtonStyle();

        s.imageUp = new TextureRegionDrawable(
            new com.badlogic.gdx.graphics.g2d.TextureRegion(load(up))
        );

        s.imageDown = new TextureRegionDrawable(
            new com.badlogic.gdx.graphics.g2d.TextureRegion(load(down))
        );

        s.imageChecked = s.imageUp;
        s.imageOver = s.imageUp;

        return new ImageButton(s);
    }
    private ClickListener click(Runnable r) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                r.run();
            }
        };
    }
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable makeSolid(Color c) {
        com.badlogic.gdx.graphics.Pixmap p = new com.badlogic.gdx.graphics.Pixmap(1,1,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        p.setColor(c); p.fill(); Texture t = new Texture(p); p.dispose();
        textures.add(t);
        return new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(t));
    }
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable makeRounded(Color c) {
        int w = 296;
        int h = 31;
        int r = 11;

        com.badlogic.gdx.graphics.Pixmap p =
            new com.badlogic.gdx.graphics.Pixmap(
                w, h,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
            );

        p.setBlending(com.badlogic.gdx.graphics.Pixmap.Blending.None);
        p.setColor(c);

        p.fillRectangle(r, 0, w - 2 * r, h);
        p.fillRectangle(0, r, w, h - 2 * r);

        p.fillCircle(r, r, r);
        p.fillCircle(w - r - 1, r, r);
        p.fillCircle(r, h - r - 1, r);
        p.fillCircle(w - r - 1, h - r - 1, r);

        Texture texture = new Texture(p);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        p.dispose();
        textures.add(texture);

        return new TextureRegionDrawable(
            new com.badlogic.gdx.graphics.g2d.TextureRegion(texture)
        );
    }

    public void show(Tower selected) {
        clearActions();
        tower = selected; confirmingSell = false; upgradeMode = false; selectedElement = "fire";
        if (!open) {
            setVisible(true); setPosition(1600,SIDEBAR_Y); addAction(Actions.moveTo(1280,SIDEBAR_Y,.32f)); open = true;
        }
        refresh();
    }
    public void hide() {
        clearActions();
        tower = null; open = false;
        addAction(Actions.sequence(Actions.moveTo(1600,SIDEBAR_Y,.25f), Actions.visible(false)));
    }
    private void toggleUpgrade() {
        if (tower == null) return; upgradeMode = !upgradeMode; refresh();
    }
    private void selectElement(String e) {
        if (tower == null || !upgradeMode) return;

        selectedElement = e;
        refresh();
    }
    private void cyclePriority(int dir) {
        if (tower==null) return;
        tower.cycleTargetPriority(dir);
        priorityLabel.setText(title(tower.getTargetPriority().name()));
        if(listener!=null) listener.onPriorityChanged(tower);
    }
    private String title(String s) {
        return s.substring(0,1)+s.substring(1).toLowerCase();
    }
    private String value(Float v) {
        return v == null ? "NULL" : (Math.abs(v-Math.round(v))<.001f ? String.valueOf(Math.round(v)) : String.format(java.util.Locale.US,"%.1f",v));
    }
    private void addStat(String name, String value) {
        Table row = new Table();
        row.setBackground(makeRounded(new Color(.075f,.075f,.075f,1f)));
        Label l = new Label(name, statStyle); Label v = new Label(value, statStyle);
        l.setFontScale(1.05f); v.setFontScale(1.05f);
        row.add(l).left().expandX().padLeft(12); row.add(v).right().padRight(12);
        stats.add(row).width(296).height(31).padBottom(5).row();
    }

    private void refresh() {
        if (tower == null) return;

        nameLabel.setText(tower.getDisplayName());
        elementLabel.setText(tower.getElementName());
        uniqueLabel.setText(tower.getUniqueText());

        towerIcon.setDrawable(
            new TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(tower.getTexture())
            )
        );

        elementLabel.setVisible(true);
        nameLabel.setVisible(true);
        uniqueLabel.setVisible(true);
        towerIcon.setVisible(true);

        elementRow.setVisible(upgradeMode);

        for (int i = 0; i < ELEMENTS.length; i++) {
            boolean selected = ELEMENTS[i].equals(selectedElement);

            elementButtons[i].setColor(
                selected
                    ? Color.WHITE
                    : new Color(0.35f, 0.35f, 0.35f, 1f)
            );
        }

        priorityLabel.setText(
            title(tower.getTargetPriority().name())
        );

        leftPriority.setVisible(!upgradeMode);
        rightPriority.setVisible(!upgradeMode);
        priorityLabel.setVisible(!upgradeMode);
        priorityBg.setVisible(!upgradeMode);

        moveLabel.setText(upgradeMode ? "Buy" : "Move");
        moveLabel.setVisible(true);

        buyDenny.setVisible(upgradeMode);
        buyCost.setVisible(upgradeMode);

        buyCost.setText("NULL");
        upgradeCost.setText("NULL");


        stats.clearChildren();

        if (upgradeMode) {
            addStat("DMG", "NULL > NULL");
            addStat("ATK Speed", "NULL > NULL");
            addStat("Range", "NULL > NULL");

            stats.setY(315f);
        } else {
            for (Tower.StatLine line : tower.getSidebarStats()) {
                String statValue =
                    line.value == null ? "NULL" : line.value.get();

                addStat(line.label, statValue);
            }

            stats.setY(365f);
        }

        stats.top().left();

        if (upgradeMode) {
            elementRow.toFront();
        }

        updateSellText();
    }

    private void updateSellText() {
        sellLabel.setVisible(!confirmingSell); sellValue.setVisible(confirmingSell);
        sellValue.setText("+NULL");
        sellDenny.setVisible(confirmingSell);
    }
    @Override public void dispose() { for(Texture t:textures)t.dispose(); font.dispose(); smallFont.dispose(); }
}
