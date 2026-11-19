package com.etherealtowerdefense.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.etherealtowerdefense.game.factories.UIFactory;

public class MainMenu {
    private final Stage stage = new Stage(new FitViewport(1280, 720));

    public MainMenu(Runnable onPlay) {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        TextButton play = UIFactory.createButton("Play");
        TextButton Options = UIFactory.createButton("Options");
        TextButton exit = UIFactory.createButton("Exit");

        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPlay.run();
            }
        });

        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });



        root.add(UIFactory.createTitle("Ethereal Tower Defense")).padBottom(50).row();
        root.add(play).width(250).height(60).pad(10).row();
        root.add(Options).width(250).height(60).pad(10).row();
        root.add(exit).width(250).height(60).pad(10);
    }

    public Stage getStage() { return stage; }

    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
        stage.act(delta);
        stage.draw();
    }

    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    public void dispose() { stage.dispose(); }
}
