package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.IdentifiableBody;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultEntityRenderer implements EntityRenderer {

    private final IdentifiableBody identifiableBody;
    private final ShapeRenderer shapeRenderer;

    @Override
    public void render(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf(identifiableBody.getColor()));
        shapeRenderer.circle(identifiableBody.x().floatValue(), identifiableBody.y().floatValue(),
            identifiableBody.radius().floatValue(), 100);
        shapeRenderer.end();
    }

}
