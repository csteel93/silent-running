package com.steel.silent.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.steel.silent.simulation.Universe;
import com.steel.silent.ui.handler.KeyHandlerFactory;
import com.steel.silent.ui.handler.key.ScrollHandler;
import com.steel.silent.ui.renderers.UniverseRenderer;

public class SkyMap {

    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final UserInputProcessor inputProcessor;
    private final UniverseRenderer universeRenderer;

    public SkyMap(final float width, final float height, final Universe universe) {
        this(width, height, universe, new UserInputConfigurations(3, 2));
    }

    public SkyMap(final float width, final float height, final Universe universe, final UserInputConfigurations uiConfig) {
        final OrthographicCamera ortho = new OrthographicCamera(width, height);
        ortho.setToOrtho(false, width, height);
        this.camera = ortho;
        this.viewport = new ExtendViewport(width * 2, height * 2, this.camera);
        this.inputProcessor = new UserInputProcessor(
            KeyHandlerFactory.getKeyHandlers(camera, viewport, uiConfig),
            new ScrollHandler(camera));
        this.universeRenderer = new UniverseRenderer(universe, new ShapeRenderer());
    }

    public void registerInput(final InputMultiplexer multiplexer) {
        multiplexer.addProcessor(inputProcessor);
    }

    public void render() {
        inputProcessor.handleInput();
        camera.update();
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        universeRenderer.render(camera.combined);
    }

    public void update(final int width, final int height) {
        viewport.update(width, height, false);
    }

    public void dispose() {
        universeRenderer.dispose();
    }
}
