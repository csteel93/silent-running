package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.Universe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class UniverseRenderer {

    private static final int ORBIT_GUIDE_SEGMENTS = 128;
    private static final float OUTER_INFLUENCE_ALPHA = 0.36f;
    private static final float SATELLITE_ORBIT_ALPHA = 0.62f;

    private final Universe universe;
    private final ShapeRenderer shapeRenderer;
    private final Map<IdentifiableBody, EntityRenderer> entityRenderers = new HashMap<>();
    private final Map<String, Function<IdentifiableBody, EntityRenderer>> rendererSupplier;
    private final DebugOverlayRenderer debugOverlay;

    public UniverseRenderer(final Universe universe, final ShapeRenderer shapeRenderer) {
        this.universe = universe;
        this.shapeRenderer = shapeRenderer;
        this.debugOverlay = new DebugOverlayRenderer(universe, shapeRenderer);
        rendererSupplier = new HashMap<String, Function<IdentifiableBody, EntityRenderer>>() {{
            put("STAR", body -> new EntityTextureRenderer(body, new Texture(Gdx.files.internal("sun.png"))));
            put("SHIP", body -> new ShipRenderer((Ship) body, shapeRenderer));
        }};
        loadRenderers(universe);
    }

    public void render(final Matrix4 projection) {
        // Late-bind renderers for bodies added after construction (e.g. ships).
        universe.getState().forEach(body -> {
            if (!entityRenderers.containsKey(body)) {
                Optional.ofNullable(rendererSupplier.get(body.classification()))
                    .ifPresent(func -> entityRenderers.put(body, func.apply(body)));
            }
        });
        renderInfluenceGuides(projection);
        universe.getState()
            .forEach(body -> entityRenderers
                .getOrDefault(body, new DefaultEntityRenderer(body, shapeRenderer))
                .render(projection));

        // Render debug overlay last so it draws on top.
        debugOverlay.render(projection);
    }

    private void renderInfluenceGuides(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        universe.getState().forEach(body -> {
            if (body instanceof final CelestialBody celestialBody) {
                drawInfluenceGuides(celestialBody);
            }
        });
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawInfluenceGuides(final CelestialBody body) {
        final Color base = safeColor(body.getColor());
        final float x = body.x().floatValue();
        final float y = body.y().floatValue();
        final float satelliteOrbitRadius = body.artificialSatelliteOrbitRadius().floatValue();
        final float outerInfluenceRadius = body.renderedInfluenceRadius().floatValue();

        shapeRenderer.setColor(base.r, base.g, base.b, SATELLITE_ORBIT_ALPHA);
        shapeRenderer.circle(x, y, satelliteOrbitRadius, ORBIT_GUIDE_SEGMENTS);

        shapeRenderer.setColor(base.r, base.g, base.b, OUTER_INFLUENCE_ALPHA);
        shapeRenderer.circle(x, y, outerInfluenceRadius, ORBIT_GUIDE_SEGMENTS);
    }

    private Color safeColor(final String color) {
        if (color == null) return Color.WHITE;
        try {
            return Color.valueOf(color);
        } catch (final Exception e) {
            return Color.WHITE;
        }
    }

    private void loadRenderers(final Universe universe) {
        universe.getState().forEach(body -> {
            Optional.ofNullable(rendererSupplier.get(body.classification()))
                    .ifPresent(func -> entityRenderers.put(body, func.apply(body)));
        });
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
