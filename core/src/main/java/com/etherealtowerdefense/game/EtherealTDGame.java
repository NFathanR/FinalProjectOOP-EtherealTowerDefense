package com.etherealtowerdefense.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.etherealtowerdefense.game.map.GameMap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EtherealTDGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private GameMap gameMap;
    private ShapeRenderer shapeRenderer;
    private static final int TILE_SIZE = 40;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        gameMap = new GameMap();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); //muali proses gambar kotaknya
        for(int y = 0; y < 18; y++) {
            for(int x = 0; x < 32; x++) {

                if(gameMap.getTile(x, y) == 1) {
                    //path nya
                    shapeRenderer.setColor(0.65f, 0.45f, 0.25f, 1f);

                } else {
                    //rumput
                    shapeRenderer.setColor(0.2f, 0.5f, 0.3f, 1f);//set warna hijau

                }
                //gambar kotak
                shapeRenderer.rect(x * TILE_SIZE, (17 - y) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }


        shapeRenderer.end(); //selesai gambar
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        shapeRenderer.dispose();
    }
}
