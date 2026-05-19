package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.steel.silent.math.Vector;
import com.steel.silent.navigation.HohmannDrawData;
import com.steel.silent.navigation.LaunchWindow;
import com.steel.silent.navigation.OrbitalMechanics;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.ui.renderers.viewProxies.MapScale;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.List;
import java.util.ArrayList;

public class UniverseRenderer {

    private final ShapeRenderer shapeRenderer;
    private final MapScale mapScale;
    private final Map<UUID, EntityRenderer> entityRenderers = new HashMap<>();
    // private final Map<String, Function<VisibleObject, EntityRenderer>>
    // rendererSupplier;
    // private final List<RendererRule> predicateRenderers = new ArrayList<>();
    private final DebugOverlayRenderer debugOverlay;
    // private final WhiteCircleRenderer whiteCircleRenderer;
    private final BodyLabelRenderer bodyLabelRenderer;
    private boolean labelsVisible = true;

    // private VisibleSatellite earth;
    // private VisibleSatellite mars;

    public UniverseRenderer(final ShapeRenderer shapeRenderer,
            final MapScale mapScale) {
        this.shapeRenderer = shapeRenderer;
        this.mapScale = mapScale;
        this.debugOverlay = new DebugOverlayRenderer( shapeRenderer);
        // rendererSupplier = new HashMap<String, Function<VisibleObject,
        // EntityRenderer>>() {
        // {
        // put("STAR", body -> new EntityTextureRenderer(body, new
        // Texture(Gdx.files.internal("sun.png"))));
        // put("SHIP", body -> new ShipRenderer((Ship) body, shapeRenderer));
        // }
        // };
        // registerPredicateRenderer(obj -> "Earth".equals(obj.name()),
        // obj -> new EntityTextureRenderer(obj, new
        // Texture(Gdx.files.internal("earth.png"))));

        // whiteCircleRenderer = new WhiteCircleRenderer(shapeRenderer);
        this.bodyLabelRenderer = new BodyLabelRenderer(shapeRenderer);
        // loadRenderers(universe);
        // this.earth = universe.getState()
        // .filter(body -> body instanceof VisibleSatellite)
        // .map(body -> (VisibleSatellite) body)
        // .filter(body -> "Earth".equals(body.name()))
        // .findFirst()
        // .get();
        // this.mars = universe.getState()
        // .filter(body -> body instanceof VisibleSatellite)
        // .map(body -> (VisibleSatellite) body)
        // .filter(body -> "Mars".equals(body.name()))
        // .findFirst()
        // .get();
    }

    public void render(final SimulationSnapshot snapshot,
            final Matrix4 projection,
            final OrthographicCamera camera) {

        final Map<UUID, BodyState> bodiesById = snapshot.bodiesById();
        final List<ProjectedBody> bodies = snapshot.bodies().stream()
                .map(body -> new ProjectedBody(
                        body,
                        mapScale,
                        bodiesById))
                .toList();

        bodies.forEach(body -> {
            final EntityRenderer renderer = entityRenderers.computeIfAbsent(body.id(),
                    ignored -> createRenderer(body));
            renderer.render(body, projection);
        });

        if (labelsVisible) {
            bodyLabelRenderer.render(camera, bodies);
        }

        debugOverlay.render(bodies, projection);
    }

    private EntityRenderer createRenderer(final ProjectedBody body) {
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

    // private static record RendererRule(Predicate<VisibleObject> predicate,
    // Function<VisibleObject, EntityRenderer> supplier) {
    // }

    // /**
    // * Register a predicate-based renderer. The first matching predicate wins.
    // */
    // public void registerPredicateRenderer(final Predicate<VisibleObject>
    // predicate,
    // final Function<VisibleObject, EntityRenderer> supplier) {
    // predicateRenderers.add(new RendererRule(predicate, supplier));
    // }

    // public void render(final Matrix4 projection, final OrthographicCamera camera)
    // {
    // // Late-bind renderers for bodies added after construction (e.g. ships).
    // universe.getState().forEach(this::ensureRenderer);
    // // renderInfluenceGuides(projection);
    // universe.getState()
    // .forEach(body -> entityRenderers.get(body).render(projection));
    // if (labelsVisible) {
    // bodyLabelRenderer.render(camera, universe.getState());
    // }

    // // Render debug overlay last so it draws on top.
    // debugOverlay.render(projection);

    // double simTimeSeconds = universe.getSimTime() / 1000.0;

    // LaunchWindow window =
    // OrbitalMechanics.findLaunchWindowTwoPass(earth.getSatellite(),
    // mars.getSatellite(),
    // simTimeSeconds);

    // RenderProjection renderProjection = new
    // RenderProjection(universe.getMapScale());

    // HohmannDrawData drawData = HohmannDrawData.buildHohmannDrawData(window);

    // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    // shapeRenderer.setColor(0.3f, 0.3f, 0.6f, 0.7f);

    // drawOrbitRing(shapeRenderer, earth, simTimeSeconds, renderProjection);
    // drawOrbitRing(shapeRenderer, mars, simTimeSeconds, renderProjection);

    // shapeRenderer.end();

    // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    // shapeRenderer.setColor(Color.YELLOW);
    // drawRoute(shapeRenderer, drawData.transferPointsMeters(), renderProjection);

    // shapeRenderer.end();

    // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    // shapeRenderer.setColor(Color.GREEN);

    // drawMarker(
    // shapeRenderer,
    // drawData.sourceLaunchPosMeters(),
    // 6f,
    // renderProjection);

    // shapeRenderer.setColor(Color.CYAN);
    // drawMarker(
    // shapeRenderer,
    // drawData.destinationArrivalPosMeters(),
    // 8f,
    // renderProjection);

    // shapeRenderer.setColor(Color.ORANGE);
    // drawMarker(
    // shapeRenderer,
    // drawData.expectedArrivalPosMeters(),
    // 12f,
    // renderProjection);

    // shapeRenderer.end();
    // // LaunchWindow launchWindow = OrbitalMechanics.findLaunchWindow(earth, mars,
    // // universe.getSimTime());
    // // HohmannDebugInfo debugInfo =
    // // OrbitalMechanics.buildHohmannDebugInfo(launchWindow,
    // universe.getSimTime());
    // // drawHohmannDebug(projection, debugInfo, launchWindow);
    // }

    // public void drawMarker(
    // ShapeRenderer shapeRenderer,
    // Vector worldMeters,
    // float markerRadiusRenderUnits,
    // RenderProjection projection) {
    // Vector2 p = projection.toRenderPosition(worldMeters);

    // shapeRenderer.circle(
    // p.x,
    // p.y,
    // markerRadiusRenderUnits,
    // 32);
    // }

    // public void drawRoute(
    // ShapeRenderer shapeRenderer,
    // List<Vector> routePointsMeters,
    // RenderProjection projection) {
    // if (routePointsMeters == null || routePointsMeters.size() < 2) {
    // return;
    // }

    // for (int i = 0; i < routePointsMeters.size() - 1; i++) {
    // Vector2 a = projection.toRenderPosition(routePointsMeters.get(i));

    // Vector2 b = projection.toRenderPosition(routePointsMeters.get(i + 1));

    // shapeRenderer.line(a.x, a.y, b.x, b.y);
    // }
    // }

    // public void drawOrbitRing(
    // ShapeRenderer shapeRenderer,
    // VisibleSatellite body,
    // double simTimeSeconds,
    // RenderProjection projection) {

    // Vector parentWorldPosition =
    // body.getSatellite().getFocalPoint().getWorldPositionMeters(simTimeSeconds);

    // Vector2 parentRenderPosition =
    // projection.toRenderPosition(parentWorldPosition);

    // float renderOrbitRadius =
    // projection.toRenderDistance(body.getSatellite().getOrbitalRadius());

    // shapeRenderer.circle(
    // parentRenderPosition.x,
    // parentRenderPosition.y,
    // renderOrbitRadius,
    // 256);
    // }

    // public void drawHohmannDebug(final Matrix4 projection, HohmannDrawData debug,
    // LaunchWindow launchWindow) {

    // System.out.println("currentTime: " + universe.getSimTime());
    // System.out.println("launchTime: " + launchWindow.launchTime);
    // System.out.println("arrivalTime: " + launchWindow.arrivalTime);
    // System.out.println("transferTime: " + launchWindow.transferTime);
    // System.out.println("launch-current delta: " + (launchWindow.launchTime -
    // universe.getSimTime()));
    // System.out.println("arrival-current delta: " + (launchWindow.arrivalTime -
    // universe.getSimTime()));

    // // shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // // drawBody(shapeRenderer, sun, simTimeSeconds, projection,
    // cameraCenterMeters);
    // // drawBody(shapeRenderer, earth, simTimeSeconds, projection,
    // // cameraCenterMeters);
    // // drawBody(shapeRenderer, mars, simTimeSeconds, projection,
    // // cameraCenterMeters);

    // // shapeRenderer.end();

    // // Current positions
    // // whiteCircleRenderer.render(projection, debug.sourceCurrentPosition,
    // // launchWindow.source.getOrbitalRadius().floatValue());
    // // whiteCircleRenderer.render(projection, debug.destinationCurrentPosition,
    // // launchWindow.destination.getOrbitalRadius().floatValue());

    // // System.out.println("source current position: "
    // // + debug.sourceCurrentPosition.x + " " + debug.sourceCurrentPosition.y);
    // // System.out.println("destination current position: "
    // // + debug.destinationCurrentPosition.x + " " +
    // // debug.destinationCurrentPosition.y);
    // // drawCircle(debug.sourceCurrentPosition, 8f);
    // // drawCircle(debug.destinationCurrentPosition, 8f);

    // // whiteCircleRenderer.render(projection, debug.sourceLaunchPosition,
    // // launchWindow.source.radius().floatValue());
    // // whiteCircleRenderer.render(projection, debug.destinationArrivalPosition,
    // // launchWindow.destination.radius().floatValue());

    // // System.out.println("source launch position: "
    // // + debug.sourceLaunchPosition.x + " " + debug.sourceLaunchPosition.y);
    // // System.out.println("destination arrival position: "
    // // + debug.destinationArrivalPosition.x + " " +
    // // debug.destinationArrivalPosition.y);
    // // // Launch and arrival markers
    // // drawCircle(debug.sourceLaunchPosition, 10f);
    // // drawCircle(debug.destinationArrivalPosition, 10f);

    // // // Expected Hohmann arrival point
    // // drawCircle(debug.expectedArrivalPosition, 14f);

    // // // Transfer curve
    // // for (int i = 0; i < debug.transferCurvePoints.size - 1; i++) {
    // // Vector2 a = debug.transferCurvePoints.get(i);
    // // Vector2 b = debug.transferCurvePoints.get(i + 1);

    // // drawLine(a, b);
    // // }

    // // // Parent-to-arrival radial line, useful for debugging angles
    // // drawLine(
    // // debug.parentArrivalPosition,
    // // debug.expectedArrivalPosition);

    // // // Velocity/tangent arrows
    // // drawArrow(
    // // debug.sourceLaunchPosition,
    // // new Vector2(debug.sourceLaunchVelocity).nor().scl(80f));

    // // drawArrow(
    // // debug.destinationArrivalPosition,
    // // new Vector2(debug.destinationArrivalVelocity).nor().scl(80f));
    // }

    // private void loadRenderers(final VisibleUniverse universe) {
    // universe.getState().forEach(this::ensureRenderer);
    // }

    // private void ensureRenderer(final VisibleObject body) {
    // if (entityRenderers.containsKey(body)) {
    // return;
    // }
    // EntityRenderer renderer = null;

    // for (final RendererRule rule : predicateRenderers) {

    // if (rule.predicate().test(body)) {
    // renderer = rule.supplier().apply(body);
    // break;
    // }
    // }

    // if (renderer == null) {
    // renderer = Optional.ofNullable(rendererSupplier.get(body.classification()))
    // .map(func -> func.apply(body))
    // .orElseGet(() -> new DefaultEntityRenderer(body, shapeRenderer));
    // }
    // entityRenderers.put(body, renderer);
    // }

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
