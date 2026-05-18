package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.simulation.Universe;

/**
 * Optional debug overlay for passive orbital context.
 */
public final class DebugOverlayRenderer {

    public static boolean enabled = readEnabledProperty();

    private static final int CIRCLE_STEPS = 48;
    private static final Color OUTER_INFLUENCE_COLOR = new Color(0.8f, 0.5f, 0.1f, 0.35f);
    private static final Color SATELLITE_ORBIT_COLOR = new Color(0.4f, 0.9f, 1.0f, 0.45f);

    private final Universe universe;
    private final ShapeRenderer shapeRenderer;

    public DebugOverlayRenderer(final Universe universe, final ShapeRenderer shapeRenderer) {
        this.universe = universe;
        this.shapeRenderer = shapeRenderer;
    }

    public void render(final Matrix4 projection) {
        if (!enabled) return;
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        universe.getState().forEach(body -> {
            if (body instanceof final CelestialBody celestialBody) {
                drawCircle(
                    (float) celestialBody.x().doubleValue(),
                    (float) celestialBody.y().doubleValue(),
                    (float) celestialBody.artificialSatelliteOrbitRadius().doubleValue(),
                    SATELLITE_ORBIT_COLOR);
                drawCircle(
                    (float) celestialBody.x().doubleValue(),
                    (float) celestialBody.y().doubleValue(),
                    (float) celestialBody.renderedInfluenceRadius().doubleValue(),
                    OUTER_INFLUENCE_COLOR);
            }
        });
        shapeRenderer.end();
    }

    private void drawCircle(final float cx, final float cy, final float r, final Color color) {
        if (r <= 0) return;
        shapeRenderer.setColor(color);
        float prevX = cx + r;
        float prevY = cy;
        for (int i = 1; i <= CIRCLE_STEPS; i++) {
            final double angle = (Math.PI * 2.0 * i) / CIRCLE_STEPS;
            final float nx = cx + (float) (r * Math.cos(angle));
            final float ny = cy + (float) (r * Math.sin(angle));
            shapeRenderer.line(prevX, prevY, nx, ny);
            prevX = nx;
            prevY = ny;
        }
    }

    private static boolean readEnabledProperty() {
        return "true".equalsIgnoreCase(System.getProperty("silent.debug.overlay", "false"));
    }
}
