package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

/**
 * Renders a body as a filled circle using {@link ShapeRenderer}, with a
 * segment count that scales with screen radius (min 8, max 64).
 *
 * <p>Replacing the old raster-texture approach eliminates the pixelated
 * appearance at extreme zoom levels, since the circle is always drawn at
 * full GPU resolution.</p>
 */
public class CircleBodyRenderer implements EntityRenderer {

    private static final int MIN_SEGMENTS = 8;
    private static final int MAX_SEGMENTS = 64;

    private final ShapeRenderer shapeRenderer;

    public CircleBodyRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void render(final ProjectedBodyState body, final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(safeColor(body.color()));
        shapeRenderer.circle(body.x(), body.y(), body.radius(), segments(body.radiusPixels()));
        shapeRenderer.end();
    }

    private static int segments(final float screenRadiusPx) {
        return Math.max(MIN_SEGMENTS, Math.min(MAX_SEGMENTS, (int) (screenRadiusPx * 2.5f)));
    }

    private static Color safeColor(final String hex) {
        if (hex == null) return Color.WHITE;
        try {
            return Color.valueOf(hex);
        } catch (final RuntimeException e) {
            return Color.WHITE;
        }
    }
}
