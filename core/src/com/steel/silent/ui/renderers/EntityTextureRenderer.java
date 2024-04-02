package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.IdentifiableBody;

public class EntityTextureRenderer implements EntityRenderer {

    private final IdentifiableBody body;
    private final SpriteBatch batch;
    private final Sprite sprite;

    public EntityTextureRenderer(final IdentifiableBody body,
                                 final Texture texture) {
        this.body = body;
        this.batch = new SpriteBatch();
        this.sprite = new Sprite(texture);
        sprite.setSize(body.radius().intValue() * 2, body.radius().intValue() * 2);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        sprite.setCenter(body.x().floatValue(), body.y().floatValue());
    }

    @Override
    public void render(Matrix4 projection) {
        batch.begin();
        batch.setProjectionMatrix(projection);
        sprite.setRotation(body.aspect().floatValue());
        sprite.draw(batch);
        batch.end();
    }
}
