package com.steel.silent.ui.renderers;

import java.util.List;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

/** Optional debug overlay for passive orbital context. */
public final class DebugOverlayRenderer {

    public static boolean enabled = readEnabledProperty();

    private static final int MIN_CIRCLE_STEPS = 96;
    private static final int MAX_CIRCLE_STEPS = 8_192;
    private static final float CIRCLE_STEPS_PER_SCREEN_UNIT = 1.5f;
    private static final Color OUTER_INFLUENCE_COLOR = new Color(0.9f, 0.55f, 0.05f, 0.75f);
    private static final Color SATELLITE_ORBIT_COLOR = new Color(0.4f, 0.9f, 1.0f, 0.45f);

    private final ShapeRenderer shapeRenderer;
    // Reused per drawCircle call to avoid per-frame allocation.
    private final Matrix4 localProjection = new Matrix4();

    public DebugOverlayRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void renderInfluenceRadii(final List<ProjectedBodyState> bodies,
            final Matrix4 projection,
            final OrthographicCamera camera) {
        bodies.stream()
                .filter(this::shouldDrawInfluenceRadius)
                .forEach(body -> drawCircle(
                        (float) body.x(),
                        (float) body.y(),
                        (float) body.getInfluenceRadius(),
                        OUTER_INFLUENCE_COLOR,
                        camera.zoom,
                        projection));
    }

    public void renderDebugRings(final List<ProjectedBodyState> bodies,
            final Matrix4 projection,
            final OrthographicCamera camera) {
        if (!enabled) {
            return;
        }
        bodies.stream()
                .filter(this::shouldDrawInfluenceRadius)
                .forEach(body -> drawCircle(
                        (float) body.x(),
                        (float) body.y(),
                        (float) body.getEncounterRadius(),
                        SATELLITE_ORBIT_COLOR,
                        camera.zoom,
                        projection));
    }

    private boolean shouldDrawInfluenceRadius(final ProjectedBodyState body) {
        return !"STAR".equals(body.classification()) && !"SHIP".equals(body.classification());
    }

    private void drawCircle(final float cx,
            final float cy,
            final float r,
            final Color color,
            final float cameraZoom,
            final Matrix4 projection) {
        if (r <= 0)
            return;
        final int steps = circleSteps(r, cameraZoom);
        // Translate the projection to the body centre so all vertex coordinates are
        // relative to origin. This avoids catastrophic float cancellation when r is
        // tiny relative to cx/cy (e.g. Phobos/Deimos at solar-system scale) — small
        // floats near zero have full precision, unlike (534f + 1e-4f) which loses the
        // offset entirely. The GPU applies the translation with adequate precision.
        localProjection.set(projection).translate(cx, cy, 0);
        shapeRenderer.setProjectionMatrix(localProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        float prevX = r;
        float prevY = 0;
        for (int i = 1; i <= steps; i++) {
            final double angle = (Math.PI * 2.0 * i) / steps;
            final float nx = (float) (r * Math.cos(angle));
            final float ny = (float) (r * Math.sin(angle));
            shapeRenderer.line(prevX, prevY, nx, ny);
            prevX = nx;
            prevY = ny;
        }
        shapeRenderer.end();
    }

    private int circleSteps(final float radius, final float cameraZoom) {
        final float screenRadius = radius / Math.max(0.0001f, cameraZoom);
        return Math.max(
                MIN_CIRCLE_STEPS,
                Math.min(MAX_CIRCLE_STEPS, (int) Math.ceil(screenRadius * CIRCLE_STEPS_PER_SCREEN_UNIT)));
    }

    private static boolean readEnabledProperty() {
        return "true".equalsIgnoreCase(System.getProperty("silent.debug.overlay", "true"));
    }
}
