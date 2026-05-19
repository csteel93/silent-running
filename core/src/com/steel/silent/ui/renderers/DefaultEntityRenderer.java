package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.VisibleObject;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultEntityRenderer implements EntityRenderer {

    private final VisibleObject body;
    private final ShapeRenderer shapeRenderer;

    @Override
    public void render(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf(body.getColor()));
        shapeRenderer.circle((float)body.x(), (float)body.y(),
            (float)body.radius(), 100);

        shapeRenderer.end();
    }

}
