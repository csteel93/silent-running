package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UniverseRenderer {

    /** Hide moons when the viewport is wider than this many metres. */
    private static final double MOON_GATE_METERS_PER_PIXEL = 1e8;

    private final ShapeRenderer shapeRenderer;
    private final Map<UUID, EntityRenderer> entityRenderers = new HashMap<>();
    private final DebugOverlayRenderer debugOverlay;
    private final BodyLabelRenderer bodyLabelRenderer;
    private boolean labelsVisible = true;

    public UniverseRenderer(final ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.debugOverlay = new DebugOverlayRenderer(shapeRenderer);
        this.bodyLabelRenderer = new BodyLabelRenderer(shapeRenderer);
    }

    public void render(final SimulationSnapshot snapshot,
            final Matrix4 projection,
            final OrthographicCamera camera,
            final WorldProjection worldProjection) {

        final boolean showMoons = worldProjection.metersPerPixel() <= MOON_GATE_METERS_PER_PIXEL;

        final List<ProjectedBodyState> bodies = snapshot.bodies().stream()
                .filter(body -> showMoons || !"MOON".equals(body.classification()))
                .filter(body -> isVisible(body, camera, worldProjection))
                .map(body -> new ProjectedBodyState(body, worldProjection))
                .toList();

        bodies.forEach(body -> {
            final EntityRenderer renderer = entityRenderers.computeIfAbsent(
                    body.id(), ignored -> createRenderer(body));
            renderer.render(body, projection);
        });

        debugOverlay.renderInfluenceRadii(bodies, projection, camera);

        if (labelsVisible) {
            bodyLabelRenderer.render(camera, bodies);
        }

        debugOverlay.renderDebugRings(bodies, projection, camera);
    }

    private boolean isVisible(final BodyState body,
            final OrthographicCamera camera,
            final WorldProjection proj) {
        final float relX = proj.relX(body.positionMeters().x());
        final float relY = proj.relY(body.positionMeters().y());
        // Use the rendered radius (already clamped to ≥3 px) as the cull sphere.
        final float cullRadius = proj.renderRadiusMeters(body.radiusMeters());
        return camera.frustum.sphereInFrustum(relX, relY, 0f, cullRadius);
    }

    private EntityRenderer createRenderer(final ProjectedBodyState body) {
        if ("SHIP".equals(body.classification())) {
            return new ShipRenderer(shapeRenderer);
        }
        if ("STAR".equals(body.classification())) {
            return new EntityTextureRenderer(new Texture(Gdx.files.internal("sun.png")));
        }
        if ("Earth".equals(body.name())) {
            return new EntityTextureRenderer(new Texture(Gdx.files.internal("earth.png")));
        }
        return new CircleBodyRenderer(shapeRenderer);
    }

    public void dispose() {
        bodyLabelRenderer.dispose();
        shapeRenderer.dispose();
    }

    public boolean areLabelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(final boolean labelsVisible) {
        this.labelsVisible = labelsVisible;
    }
}
