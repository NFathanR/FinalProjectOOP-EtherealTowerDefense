package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIFactory {

    private static final int BUTTON_FONT_SIZE = 22;
    private static final int TITLE_FONT_SIZE = 40;

    private static TextureRegionDrawable color(Color c) {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(c);
        p.fill();
        TextureRegionDrawable d = new TextureRegionDrawable(new Texture(p));
        p.dispose();
        return d;
    }

    public static TextButton createButton(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = FontFactory.get(BUTTON_FONT_SIZE);
        style.up = color(new Color(0.48f, 0.52f, 0.58f, 1));
        style.over = color(new Color(0.66f, 0.70f, 0.76f, 1));
        style.down = color(new Color(0.30f, 0.34f, 0.40f, 1));
        return new TextButton(text, style);
    }

    public static Label createTitle(String text) {
        Label.LabelStyle style = new Label.LabelStyle(FontFactory.get(TITLE_FONT_SIZE), Color.WHITE);
        return new Label(text, style);
    }
}
