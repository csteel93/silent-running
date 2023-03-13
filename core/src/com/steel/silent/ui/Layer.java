package com.steel.silent.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import lombok.Getter;

public abstract class Layer {

    private final Viewport viewport;
    @Getter
    private final Stage stage;

    public Layer(final Viewport viewport) {
        this.viewport = viewport;
        this.stage = new Stage(viewport);
    }

    public void render() {
        stage.draw();
        stage.act();
    }

    public void update(final int width, final int height) {
        viewport.update(width, height, false);
    }

    public void registerInput(final InputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(stage);
    }
}
