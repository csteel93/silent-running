package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

public class ShipRenderer implements EntityRenderer {

    private final ShapeRenderer shapeRenderer;

    public ShipRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void render(final ProjectedBody body, final Matrix4 projection) {
        final float x = (float) body.x();
        final float y = (float) body.y();
        final float size = Math.max(4f, (float) body.radius());
        final float heading = (float) body.orientationRad();

        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(safeColor(body.color()));
        drawTriangle(x, y, size, heading);
        shapeRenderer.end();
    }

    private void drawTriangle(final float x, final float y, final float size, final float heading) {
        final float tipX = x + (float) Math.cos(heading) * size * 1.8f;
        final float tipY = y + (float) Math.sin(heading) * size * 1.8f;
        final float leftAngle = heading + (float) (Math.PI * 0.82);
        final float rightAngle = heading - (float) (Math.PI * 0.82);
        final float leftX = x + (float) Math.cos(leftAngle) * size;
        final float leftY = y + (float) Math.sin(leftAngle) * size;
        final float rightX = x + (float) Math.cos(rightAngle) * size;
        final float rightY = y + (float) Math.sin(rightAngle) * size;
        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
    }

    private Color safeColor(final String color) {
        if (color == null) {
            return Color.WHITE;
        }
        try {
            return Color.valueOf(color);
        } catch (final RuntimeException e) {
            return Color.WHITE;
        }
    }
}
