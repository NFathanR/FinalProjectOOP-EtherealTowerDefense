package com.etherealtowerdefense.game.ui;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.etherealtowerdefense.game.factories.FontFactory;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import java.util.function.IntConsumer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

//buat ngurus pilihan map ya sesuai nama loh ya -SEN
public class MapSelectionMenu {
    private final Stage stage;
    private final IntConsumer onMapSelected;
    private final Runnable onBack;//buat balik ke menu -SEN
    private final Texture buttonTexture;



    public MapSelectionMenu(IntConsumer onMapSelected, Runnable onBack) {
        this.onMapSelected = onMapSelected;
        this.onBack = onBack;

        stage = new Stage(new FitViewport(1600, 900));
        Table table = new Table(); //buat layout menu
        table.setFillParent(true); //layout memenuhi seluruh area stage
        stage.addActor(table); //menambahkan layout ke stage

        // Judul ajah (SEN)
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = FontFactory.get(48);
        Label title = new Label("PILH MAP OKEH YES", titleStyle);
        table.add(title).padBottom(40); //taro ditengah yes -SEN
        table.row();

        //buat tombol utk pilih map
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = FontFactory.get(30);


        // Membuat background untuk tombol -SEN
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        buttonTexture = new Texture(pixmap);
        pixmap.dispose();

        // Mengatur warna background tombol -SEN
        buttonStyle.up = new TextureRegionDrawable(buttonTexture).tint(new Color(0.25f, 0.30f, 0.38f, 1f));
        buttonStyle.down = new TextureRegionDrawable(buttonTexture).tint(new Color(0.15f, 0.20f, 0.28f, 1f));
        buttonStyle.over = new TextureRegionDrawable(buttonTexture).tint(new Color(0.35f, 0.45f, 0.55f, 1f));


        //tombol map 1
        TextButton map1Button = new TextButton("MAP 1", buttonStyle);
        // Mengirim ID map1 saat tombol diklik -SEN
        map1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onMapSelected.accept(1);
            }
        });
        table.add(map1Button).width(250).height(70).padBottom(20);
        table.row();


        //tombol map 2 -SEN
        TextButton map2Button = new TextButton("MAP 2", buttonStyle);
        map2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onMapSelected.accept(2);
            }
        });
        table.add(map2Button).width(250).height(70).padBottom(20);
        table.row();


        //balik ke menu awal -SEN
        TextButton backButton = new TextButton("BACK", buttonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onBack.run();
            }
        });
        table.add(backButton).width(250).height(70);
        table.row();
    }

    public Stage getStage() {
        return stage;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        buttonTexture.dispose();
    }

}
