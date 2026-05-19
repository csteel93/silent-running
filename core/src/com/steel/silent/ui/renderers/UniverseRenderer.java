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

    private final ShapeRenderer shapeRenderer;
    private final WorldProjection worldProjection;
    private final Map<UUID, EntityRenderer> entityRenderers = new HashMap<>();
    private final DebugOverlayRenderer debugOverlay;
    private final BodyLabelRenderer bodyLabelRenderer;
    private boolean labelsVisible = true;

    public UniverseRenderer(final ShapeRenderer shapeRenderer,
            final WorldProjection projection) {
        this.shapeRenderer = shapeRenderer;
        this.worldProjection = projection;
        this.debugOverlay = new DebugOverlayRenderer(shapeRenderer);
        this.bodyLabelRenderer = new BodyLabelRenderer(shapeRenderer);
    }

    public void render(final SimulationSnapshot snapshot,
            final Matrix4 projection,
            final OrthographicCamera camera) {

        final Map<UUID, BodyState> bodiesById = snapshot.bodiesById();
        final List<ProjectedBodyState> bodies = snapshot.bodies().stream()
                .map(body -> new ProjectedBodyState(body, worldProjection, bodiesById))
                .toList();

        bodies.forEach(body -> {
            final EntityRenderer renderer = entityRenderers.computeIfAbsent(
                    body.id(), ignored -> createRenderer(body));
            renderer.render(body, projection);
        });

        debugOverlay.renderInfluenceRadii(bodies, projection);

        if (labelsVisible) {
            bodyLabelRenderer.render(camera, bodies);
        }

        debugOverlay.renderDebugRings(bodies, projection);
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
        return new DefaultEntityRenderer(shapeRenderer);
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
