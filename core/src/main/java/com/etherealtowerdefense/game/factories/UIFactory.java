package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.etherealtowerdefense.game.ui.FontFactory;

public final class UIFactory {
    private static final BitmapFont FONT = FontFactory.create(30f);
    private static final Texture WHITE_TEXTURE = createWhiteTexture();
    private static final TextureRegionDrawable WHITE = new TextureRegionDrawable(new TextureRegion(WHITE_TEXTURE));

    private UIFactory() { }

    private static Texture createWhiteTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Drawable color(Color color) {
        return WHITE.tint(color);
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

    public static void dispose() {
        FONT.dispose();
        WHITE_TEXTURE.dispose();
    }
}
