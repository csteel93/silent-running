package com.steel.silent.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.ui.handler.KeyHandlerFactory;
import com.steel.silent.ui.handler.key.ScrollHandler;
import com.steel.silent.ui.renderers.UniverseRenderer;
import com.steel.silent.ui.renderers.viewProxies.MapScale;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

public class SkyMap {

    private static final float ZOOM_MULTIPLIER = 500f;
    private static final float MIN_ZOOM = 0.002f;
    private static final float MAX_ZOOM = 0.45f;

    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final UserInputProcessor inputProcessor;
    private final UniverseRenderer universeRenderer;
    private final Universe universe;

    // public SkyMap(final float width, final float height, final VisibleUniverse
    // universe) {
    // this(width, height, universe, null, new UserInputConfigurations(3, 5f));
    // }

    // public SkyMap(final float width, final float height, final VisibleUniverse
    // universe, final Ship debugShip) {
    // this(width, height, universe, debugShip, new UserInputConfigurations(3, 5f));
    // }

    // public SkyMap(final float width, final float height, final VisibleUniverse
    // universe, final UserInputConfigurations uiConfig) {
    // this(width, height, universe, null, uiConfig);
    // }

    public SkyMap(final float width, final float height, final Universe universe, final MapScale mapScale) {
        this(width, height, universe, mapScale, new UserInputConfigurations(3, 5f));
    }

    public SkyMap(final float width, final float height, final Universe universe, final MapScale mapScale,
            final UserInputConfigurations uiConfig) {
        final OrthographicCamera ortho = new OrthographicCamera(width, height);
        ortho.setToOrtho(false, width, height);
        this.camera = ortho;
        this.viewport = new ExtendViewport(width * 2, height * 2, this.camera);
        this.inputProcessor = new UserInputProcessor(
                KeyHandlerFactory.getKeyHandlers(camera, viewport, uiConfig),
                new ScrollHandler(camera),
                camera,
                viewport,
                universe);
        this.universe = universe;

        this.universeRenderer = new UniverseRenderer(new ShapeRenderer(), mapScale);
    }

    public void registerInput(final InputMultiplexer multiplexer) {
        multiplexer.addProcessor(inputProcessor);
    }

    public void focusOn(final ProjectedBody body) {
        if (body == null)
            return;
        camera.position.set((float) body.x(), (float) body.y(), 0f);
        camera.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, ((float) body.radius()) / ZOOM_MULTIPLIER));
        camera.update();
    }

    public void render() {
        inputProcessor.handleInput();
        camera.update();
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        universe.latestSnapshot()
                .ifPresent(snapshot -> universeRenderer.render(snapshot, camera.combined, camera));
    }

    public void update(final int width, final int height) {
        viewport.update(width, height, false);
    }

    public void dispose() {
        universeRenderer.dispose();
    }

    public boolean areLabelsVisible() {
        return universeRenderer.areLabelsVisible();
    }

    public void setLabelsVisible(final boolean labelsVisible) {
        universeRenderer.setLabelsVisible(labelsVisible);
    }
}
