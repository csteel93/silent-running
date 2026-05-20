package com.steel.silent.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.ui.handler.KeyHandlerFactory;
import com.steel.silent.ui.renderers.UniverseRenderer;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

import java.util.Optional;
import java.util.UUID;

public class SkyMap {

    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    private final SolarCamera solarCamera;
    private final UserInputProcessor inputProcessor;
    private final UniverseRenderer universeRenderer;
    private final Universe universe;

    public SkyMap(final float width, final float height, final Universe universe) {
        this(width, height, universe, new UserInputConfigurations(20, 5f));
    }

    public SkyMap(final float width, final float height,
                  final Universe universe,
                  final UserInputConfigurations uiConfig) {
        this.camera = new OrthographicCamera(width, height);
        this.viewport = new ScreenViewport(camera);
        this.solarCamera = new SolarCamera(width, height);
        this.inputProcessor = new UserInputProcessor(
                KeyHandlerFactory.getKeyHandlers(camera, viewport, uiConfig, solarCamera),
                solarCamera);
        this.universe = universe;
        this.universeRenderer = new UniverseRenderer(new ShapeRenderer());
    }

    public void registerInput(final InputMultiplexer multiplexer) {
        multiplexer.addProcessor(new GestureDetector(new PinchZoomListener()));
        multiplexer.addProcessor(inputProcessor);
    }

    public void focusOn(final ProjectedBodyState body) {
        if (body == null) return;
        focusOn(body.id());
    }

    public void focusOn(final UUID bodyId) {
        universe.latestSnapshot()
                .or(() -> Optional.of(universe.buildSnapshot()))
                .flatMap(snapshot -> Optional.ofNullable(snapshot.bodiesById().get(bodyId)))
                .ifPresent(this::focusOnBody);
    }

    private void focusOnBody(final BodyState body) {
        solarCamera.focusOn(
                body.positionMeters().x(),
                body.positionMeters().y(),
                body.influenceRadius());
    }

    public void render() {
        inputProcessor.handleInput();
        solarCamera.applyTo(camera);
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);

        universe.latestSnapshot().ifPresent(snapshot -> {
            final WorldProjection projection = solarCamera.buildProjection();
            universeRenderer.render(snapshot, camera.combined, camera, projection);
        });
    }

    public void update(final int width, final int height) {
        viewport.update(width, height, false);
        solarCamera.resize(width, height);
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

    // ── Pinch-to-zoom (mobile) ────────────────────────────────────────────────

    private class PinchZoomListener extends GestureDetector.GestureAdapter {

        private float lastPinchDistance = 0f;

        @Override
        public boolean zoom(final float initialDistance, final float distance) {
            // Incremental factor from previous call to avoid accumulation error.
            if (lastPinchDistance > 0f && distance > 0f) {
                final double factor = lastPinchDistance / distance;
                // Zoom toward the viewport centre (no midpoint available here).
                solarCamera.zoomAtCenter(factor);
            }
            lastPinchDistance = distance;
            return true;
        }

        @Override
        public boolean pinch(final Vector2 ip1, final Vector2 ip2,
                             final Vector2 p1, final Vector2 p2) {
            final float currentDist = p1.dst(p2);
            if (lastPinchDistance > 0f && currentDist > 0f) {
                final double factor = lastPinchDistance / currentDist;
                final float midX = (p1.x + p2.x) * 0.5f;
                final float midY = (p1.y + p2.y) * 0.5f;
                solarCamera.zoomToward(midX, midY, factor);
            }
            lastPinchDistance = currentDist;
            return true;
        }

        @Override
        public void pinchStop() {
            lastPinchDistance = 0f;
        }
    }
}
