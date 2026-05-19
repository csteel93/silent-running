package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.Ship;

public class ShipRenderer implements EntityRenderer {

    private static final float FIXED_ORBIT_HEADING = (float) (Math.PI / 2.0);

    private final Ship ship;
    private final ShapeRenderer shapeRenderer;

    public ShipRenderer(final Ship ship, final ShapeRenderer shapeRenderer) {
        this.ship = ship;
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void render(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(safeColor(ship.getColor()));
        final double x = ship.x();
        final double y = ship.y();
        final double r = Math.max(1f, ship.radius());
        drawTriangle((float) x, (float) y, (float) r, FIXED_ORBIT_HEADING);
        shapeRenderer.end();
    }

    private void drawTriangle(final float x, final float y, final float size, final float heading) {
        final float tipX = x + (float) Math.cos(heading) * size * 1.6f;
        final float tipY = y + (float) Math.sin(heading) * size * 1.6f;
        final float leftAngle = heading + (float) (Math.PI * 0.85);
        final float rightAngle = heading - (float) (Math.PI * 0.85);
        final float leftX = x + (float) Math.cos(leftAngle) * size;
        final float leftY = y + (float) Math.sin(leftAngle) * size;
        final float rightX = x + (float) Math.cos(rightAngle) * size;
        final float rightY = y + (float) Math.sin(rightAngle) * size;
        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
    }

    private Color safeColor(final String color) {
        if (color == null)
            return Color.WHITE;
        try {
            return Color.valueOf(color);
        } catch (final Exception e) {
            return Color.WHITE;
        }
    }
}
