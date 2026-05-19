package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

public class DefaultEntityRenderer implements EntityRenderer {

    private static final int CIRCLE_TEXTURE_SIZE = 512;

    private final SpriteBatch batch;
    private final Sprite sprite;

    public DefaultEntityRenderer(final ShapeRenderer shapeRenderer) {
        this.batch = new SpriteBatch();
        this.sprite = new Sprite(circleTexture());
    }

    @Override
    public void render(final ProjectedBodyState body, final Matrix4 projection) {
        final float x = (float) body.x();
        final float y = (float) body.y();
        final float radius = (float) body.radius();
        final float diameter = Math.max(0.01f, radius * 2f);

        sprite.setColor(Color.valueOf(body.color()));
        sprite.setSize(diameter, diameter);
        sprite.setOriginCenter();
        sprite.setCenter(x, y);

        batch.setProjectionMatrix(projection);
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    private static Texture circleTexture() {
        final Pixmap pixmap = new Pixmap(
                CIRCLE_TEXTURE_SIZE,
                CIRCLE_TEXTURE_SIZE,
                Pixmap.Format.RGBA8888);

        final double center = (CIRCLE_TEXTURE_SIZE - 1) * 0.5;
        final double radius = CIRCLE_TEXTURE_SIZE * 0.5 - 2.0;

        for (int y = 0; y < CIRCLE_TEXTURE_SIZE; y++) {
            for (int x = 0; x < CIRCLE_TEXTURE_SIZE; x++) {
                final double dx = x - center;
                final double dy = y - center;
                final double distance = Math.sqrt(dx * dx + dy * dy);
                final float alpha = (float) Math.max(0.0, Math.min(1.0, radius + 1.0 - distance));
                pixmap.setColor(1f, 1f, 1f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        final Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

}
