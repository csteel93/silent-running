package com.steel.silent.ui.renderers;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

/**
 * Optional debug overlay for passive orbital context.
//  */
public final class DebugOverlayRenderer {

    public static boolean enabled = readEnabledProperty();

    private static final int CIRCLE_STEPS = 48;
    private static final Color OUTER_INFLUENCE_COLOR = new Color(0.8f, 0.5f, 0.1f, 0.35f);
    private static final Color SATELLITE_ORBIT_COLOR = new Color(0.4f, 0.9f, 1.0f, 0.45f);

    private final ShapeRenderer shapeRenderer;

    public DebugOverlayRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void render(final List<ProjectedBody> bodies, final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        bodies.forEach(body -> {
            if (body instanceof final ProjectedBody projectedBody) {
                drawCircle(
                        (float) projectedBody.x(),
                        (float) projectedBody.y(),
                        (float) projectedBody.getEncounterRadius(),
                        SATELLITE_ORBIT_COLOR);
                drawCircle(
                        (float) projectedBody.x(),
                        (float) projectedBody.y(),
                        (float) projectedBody.getInfluenceRadius(),
                        OUTER_INFLUENCE_COLOR);
            }
        });
        shapeRenderer.end();
    }

    private void drawCircle(final float cx, final float cy, final float r, final Color color) {
        if (r <= 0)
            return;
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
