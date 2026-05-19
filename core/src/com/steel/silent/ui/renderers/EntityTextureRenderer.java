package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

public class EntityTextureRenderer implements EntityRenderer {

    private final SpriteBatch batch;
    private final Sprite sprite;

    public EntityTextureRenderer(final Texture texture) {
        this.batch = new SpriteBatch();
        this.sprite = new Sprite(texture);
    }

    @Override
    public void render(final ProjectedBody body, Matrix4 projection) {
        final float x = (float) body.x();
        final float y = (float) body.y();
        final float diameter = Math.max(0.01f, (float) body.radius() * 2f);
        sprite.setSize(diameter, diameter);
        sprite.setOriginCenter();
        sprite.setCenter(x, y);
        sprite.setRotation((float) body.orientationRad() * MathUtils.radiansToDegrees);
        batch.setProjectionMatrix(projection);
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }
}
