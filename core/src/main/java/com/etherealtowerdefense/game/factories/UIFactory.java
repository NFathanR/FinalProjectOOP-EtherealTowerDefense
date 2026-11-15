package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIFactory {
    private static final BitmapFont FONT = new BitmapFont();

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
        style.font = FONT;
        style.up = color(new Color(0.2f, 0.2f, 0.4f, 1));
        style.over = color(new Color(0.35f, 0.35f, 0.7f, 1));
        style.down = color(new Color(0.1f, 0.1f, 0.25f, 1));
        return new TextButton(text, style);
    }

    public static Label createTitle(String text) {
        Label.LabelStyle style = new Label.LabelStyle(FONT, Color.WHITE);
        Label label = new Label(text, style);
        label.setFontScale(2.5f);
        return label;
    }
}
