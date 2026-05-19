package com.steel.silent.ui.renderers;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

/** Optional debug overlay for passive orbital context. */
public final class DebugOverlayRenderer {

    public static boolean enabled = readEnabledProperty();

    private static final int CIRCLE_STEPS = 48;
    private static final Color OUTER_INFLUENCE_COLOR = new Color(0.9f, 0.55f, 0.05f, 0.75f);
    private static final Color SATELLITE_ORBIT_COLOR = new Color(0.4f, 0.9f, 1.0f, 0.45f);

    private final ShapeRenderer shapeRenderer;

    public DebugOverlayRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void renderInfluenceRadii(final List<ProjectedBodyState> bodies, final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        bodies.stream()
                .filter(this::shouldDrawInfluenceRadius)
                .forEach(body -> drawCircle(
                        (float) body.x(),
                        (float) body.y(),
                        (float) body.getInfluenceRadius(),
                        OUTER_INFLUENCE_COLOR));
        shapeRenderer.end();
    }

    public void renderDebugRings(final List<ProjectedBodyState> bodies, final Matrix4 projection) {
        if (!enabled) {
            return;
        }
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        bodies.stream()
                .filter(this::shouldDrawInfluenceRadius)
                .forEach(body -> drawCircle(
                        (float) body.x(),
                        (float) body.y(),
                        (float) body.getEncounterRadius(),
                        SATELLITE_ORBIT_COLOR));
        shapeRenderer.end();
    }

    private boolean shouldDrawInfluenceRadius(final ProjectedBodyState body) {
        return !"STAR".equals(body.classification());
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
        return "true".equalsIgnoreCase(System.getProperty("silent.debug.overlay", "true"));
    }
}
