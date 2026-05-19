package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.VisibleObject;

public class EntityTextureRenderer implements EntityRenderer {

    private final VisibleObject body;
    private final SpriteBatch batch;
    private final Sprite sprite;

    public EntityTextureRenderer(final VisibleObject body,
            final Texture texture) {
        this.body = body;
        this.batch = new SpriteBatch();
        this.sprite = new Sprite(texture);
    }

    @Override
    public void render(Matrix4 projection) {
        updateSprite();
        batch.setProjectionMatrix(projection);
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    private void updateSprite() {
        final float diameter = Math.max(0.01f, (float) body.radius() * 2f);
        sprite.setSize(diameter, diameter);
        sprite.setOriginCenter();
        sprite.setCenter((float) body.x(), (float) body.y());
        sprite.setRotation((float) body.aspect() * MathUtils.radiansToDegrees);

    }
}
