package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultEntityRenderer implements EntityRenderer {

    private final ShapeRenderer shapeRenderer;

    @Override
    public void render(final ProjectedBody body, final Matrix4 projection) {
        final float x = (float) body.x();
        final float y = (float) body.y();
        final float radius = (float) body.radius();
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf(body.color()));
        shapeRenderer.circle(x, y, radius, 100);
        shapeRenderer.end();
    }

}
