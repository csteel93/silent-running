package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BodyLabelRenderer {

    private static final float FONT_SCALE = 1.15f;
    private static final float PADDING_X = 7f;
    private static final float PADDING_Y = 4f;
    private static final float BODY_GAP = 12f;
    private static final Color BACKGROUND = new Color(0.03f, 0.04f, 0.05f, 0.72f);
    private static final Color LEADER = new Color(0.72f, 0.78f, 0.84f, 0.62f);
    private static final Color FOREGROUND = new Color(0.88f, 0.92f, 0.96f, 1f);

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Matrix4 screenProjection = new Matrix4();
    private final Vector3 screenPosition = new Vector3();

    public BodyLabelRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(FOREGROUND);
    }

    public void render(final OrthographicCamera camera, final List<ProjectedBodyState> bodies) {
        font.getData().setScale(FONT_SCALE);
        screenProjection.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(screenProjection);
        shapeRenderer.setProjectionMatrix(screenProjection);
        final List<LabelBounds> labels = new ArrayList<>();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        bodies.stream().filter(this::shouldLabel)
                .forEach(body -> labels.add(labelBounds(camera, body)));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BACKGROUND);
        labels.forEach(label -> shapeRenderer.rect(label.x, label.y, label.width, label.height));
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(LEADER);
        labels.forEach(label -> shapeRenderer.line(label.centerX, label.y, label.bodyX, label.bodyY));
        shapeRenderer.end();

        batch.begin();
        labels.forEach(label -> {
            layout.setText(font, label.text);
            font.draw(batch, layout, label.textX, label.textY);
        });
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private LabelBounds labelBounds(final OrthographicCamera camera, final ProjectedBodyState body) {
        layout.setText(font, body.name());

        screenPosition.set(body.x(), body.y(), 0f);
        camera.project(screenPosition);

        final float paddingX = PADDING_X;
        final float paddingY = PADDING_Y;
        // body.radiusPixels() is already clamped to ≥3px by WorldProjection.
        final float screenRadius = body.radiusPixels();
        final float width = layout.width + paddingX * 2f;
        final float height = layout.height + paddingY * 2f;
        final float x = screenPosition.x - width * 0.5f;
        final float y = screenPosition.y + screenRadius + BODY_GAP;
        return new LabelBounds(body.name(), x, y, width, height, x + paddingX, y + paddingY + layout.height,
                x + width * 0.5f, screenPosition.x, screenPosition.y);
    }

    private boolean shouldLabel(final ProjectedBodyState body) {
        return "PLANET".equals(body.classification())
                || "MOON".equals(body.classification())
                || "SHIP".equals(body.classification());
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    private static class LabelBounds {
        private final String text;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float textX;
        private final float textY;
        private final float centerX;
        private final float bodyX;
        private final float bodyY;

        private LabelBounds(final String text,
                final float x,
                final float y,
                final float width,
                final float height,
                final float textX,
                final float textY,
                final float centerX,
                final float bodyX,
                final float bodyY) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textX = textX;
            this.textY = textY;
            this.centerX = centerX;
            this.bodyX = bodyX;
            this.bodyY = bodyY;
        }
    }
}
