package com.steel.silent.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.steel.silent.debug.SimulationSnapshotWriter;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.ui.handler.KeyHandlerFactory;
import com.steel.silent.ui.handler.key.ScrollHandler;
import com.steel.silent.ui.renderers.UniverseRenderer;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SkyMap {

    private static final float ZOOM_MULTIPLIER = 500f;
    private static final float MIN_ZOOM = 0.002f;
    private static final float MAX_ZOOM = 0.45f;

    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final UserInputProcessor inputProcessor;
    private final UniverseRenderer universeRenderer;
    private final Universe universe;
    private final WorldProjection worldProjection;
    private final SimulationSnapshotWriter snapshotWriter = new SimulationSnapshotWriter();

    public SkyMap(final float width, final float height, final Universe universe,
            final WorldProjection worldProjection) {
        this(width, height, universe, worldProjection, new UserInputConfigurations(3, 5f));
    }

    public SkyMap(final float width, final float height, final Universe universe,
            final WorldProjection worldProjection,
            final UserInputConfigurations uiConfig) {
        final OrthographicCamera ortho = new OrthographicCamera(width, height);
        ortho.setToOrtho(false, width, height);
        this.camera = ortho;
        this.viewport = new ExtendViewport(width, height, this.camera);
        this.inputProcessor = new UserInputProcessor(
                KeyHandlerFactory.getKeyHandlers(camera, viewport, uiConfig),
                new ScrollHandler(camera),
                camera,
                viewport,
                universe);
        this.universe = universe;
        this.worldProjection = worldProjection;
        this.universeRenderer = new UniverseRenderer(new ShapeRenderer(), worldProjection);
    }

    public void registerInput(final InputMultiplexer multiplexer) {
        multiplexer.addProcessor(inputProcessor);
    }

    public void focusOn(final ProjectedBodyState body) {
        if (body == null) return;
        focusOn(body.id());
    }

    public void focusOn(final UUID bodyId) {
        universe.latestSnapshot()
                .or(() -> Optional.of(universe.buildSnapshot()))
                .flatMap(snapshot -> projectedBody(snapshot, bodyId))
                .ifPresent(this::focusOnProjectedBody);
    }

    private Optional<ProjectedBodyState> projectedBody(final SimulationSnapshot snapshot, final UUID bodyId) {
        final Map<UUID, BodyState> bodiesById = snapshot.bodiesById();
        return Optional.ofNullable(snapshot.bodiesById().get(bodyId))
                .map(body -> new ProjectedBodyState(body, worldProjection, bodiesById));
    }

    private void focusOnProjectedBody(final ProjectedBodyState body) {
        camera.position.set((float) body.x(), (float) body.y(), 0f);
        camera.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, (float) body.radius() / ZOOM_MULTIPLIER));
        camera.update();
    }

    public void render() {
        inputProcessor.handleInput();
        camera.update();
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        universe.latestSnapshot()
                .ifPresent(snapshot -> {
                    universeRenderer.render(snapshot, camera.combined, camera);
                    snapshotWriter.writeIfDue(snapshot, System.currentTimeMillis(), worldProjection);
                });
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
