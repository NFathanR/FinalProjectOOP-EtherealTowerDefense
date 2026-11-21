package com.etherealtowerdefense.game.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;


public class FontFactory {

    // Letakkan file ini di folder assets (mis. assets/fonts/Roboto-Regular.ttf), atau ubah path-nya.
    private static final String FONT_PATH = "fonts/Roboto-Regular.ttf";

    private static final Map<Integer, BitmapFont> CACHE = new HashMap<>();
    private static FreeTypeFontGenerator generator;

    private FontFactory() { }

    public static BitmapFont get(int size) {
        BitmapFont font = CACHE.get(size);
        if (font == null) {
            if (generator == null) {
                generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
            }
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;                              // ukuran akhir di layar
            parameter.minFilter = Texture.TextureFilter.Linear; // halus saat jendela diubah ukurannya
            parameter.magFilter = Texture.TextureFilter.Linear;
            font = generator.generateFont(parameter);
            CACHE.put(size, font);
        }
        return font;
    }

    public static void dispose() {
        for (BitmapFont font : CACHE.values()) {
            font.dispose();
        }
        CACHE.clear();
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
    }
}
