package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.navigation.HohmannDebugInfo;
import com.steel.silent.navigation.LaunchWindow;
import com.steel.silent.ui.renderers.viewProxies.VisibleBody;
import com.steel.silent.ui.renderers.viewProxies.VisibleObject;
import com.steel.silent.ui.renderers.viewProxies.VisibleSatellite;
import com.steel.silent.ui.renderers.viewProxies.VisibleUniverse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.List;
import java.util.ArrayList;

public class UniverseRenderer {

    private static final int ORBIT_GUIDE_SEGMENTS = 128;
    private static final float OUTER_INFLUENCE_ALPHA = 0.36f;
    private static final float SATELLITE_ORBIT_ALPHA = 0.62f;

    private final VisibleUniverse universe;
    private final ShapeRenderer shapeRenderer;
    private final Map<VisibleObject, EntityRenderer> entityRenderers = new HashMap<>();
    private final Map<String, Function<VisibleObject, EntityRenderer>> rendererSupplier;
    private final List<RendererRule> predicateRenderers = new ArrayList<>();
    private final DebugOverlayRenderer debugOverlay;
    private final WhiteCircleRenderer whiteCircleRenderer;
    private final BodyLabelRenderer bodyLabelRenderer;
    private boolean labelsVisible = true;

    private VisibleSatellite earth;
    private VisibleSatellite mars;

    public UniverseRenderer(final VisibleUniverse universe, final ShapeRenderer shapeRenderer) {
        this.universe = universe;
        this.shapeRenderer = shapeRenderer;
        this.debugOverlay = new DebugOverlayRenderer(universe, shapeRenderer);
        rendererSupplier = new HashMap<String, Function<VisibleObject, EntityRenderer>>() {
            {
                put("STAR", body -> new EntityTextureRenderer(body, new Texture(Gdx.files.internal("sun.png"))));
                put("SHIP", body -> new ShipRenderer((Ship) body, shapeRenderer));
            }
        };
        registerPredicateRenderer(obj -> "Earth".equals(obj.name()), obj -> new EntityTextureRenderer(obj, new Texture(Gdx.files.internal("earth.png"))));
        
        whiteCircleRenderer = new WhiteCircleRenderer(shapeRenderer);
        bodyLabelRenderer = new BodyLabelRenderer(shapeRenderer);
        loadRenderers(universe);
        this.earth = universe.getState()
                .filter(body -> body instanceof VisibleSatellite)
                .map(body -> (VisibleSatellite) body)
                .filter(body -> "Earth".equals(body.name()))
                .findFirst()
                .get();
        this.mars = universe.getState()
                .filter(body -> body instanceof VisibleSatellite)
                .map(body -> (VisibleSatellite) body)
                .filter(body -> "Mars".equals(body.name()))
                .findFirst()
                .get();
    }

    private static record RendererRule(Predicate<VisibleObject> predicate,
                                       Function<VisibleObject, EntityRenderer> supplier) {
    }

    /**
     * Register a predicate-based renderer. The first matching predicate wins.
     */
    public void registerPredicateRenderer(final Predicate<VisibleObject> predicate,
                                          final Function<VisibleObject, EntityRenderer> supplier) {
        predicateRenderers.add(new RendererRule(predicate, supplier));
    }

    public void render(final Matrix4 projection, final OrthographicCamera camera) {
        // Late-bind renderers for bodies added after construction (e.g. ships).
        universe.getState().forEach(this::ensureRenderer);
        // renderInfluenceGuides(projection);
        universe.getState()
                .forEach(body -> entityRenderers.get(body).render(projection));
        if (labelsVisible) {
            bodyLabelRenderer.render(camera, universe.getState());
        }

        // Render debug overlay last so it draws on top.
        debugOverlay.render(projection);

        // LaunchWindow launchWindow = OrbitalMechanics.findLaunchWindow(earth, mars, universe.getSimTime());
        // HohmannDebugInfo debugInfo = OrbitalMechanics.buildHohmannDebugInfo(launchWindow, universe.getSimTime());
        // drawHohmannDebug(projection, debugInfo, launchWindow);
    }

    public void drawHohmannDebug(final Matrix4 projection, HohmannDebugInfo debug, LaunchWindow launchWindow) {

        System.out.println("currentTime: " + universe.getSimTime());
        System.out.println("launchTime: " + launchWindow.launchTime);
        System.out.println("arrivalTime: " + launchWindow.arrivalTime);
        System.out.println("transferTime: " + launchWindow.transferTime);
        System.out.println("launch-current delta: " + (launchWindow.launchTime - universe.getSimTime()));
        System.out.println("arrival-current delta: " + (launchWindow.arrivalTime - universe.getSimTime()));

        // Current positions
        // whiteCircleRenderer.render(projection, debug.sourceCurrentPosition,
        //         launchWindow.source.getOrbitalRadius().floatValue());
        // whiteCircleRenderer.render(projection, debug.destinationCurrentPosition,
        //         launchWindow.destination.getOrbitalRadius().floatValue());

        // System.out.println("source current position: "
        //         + debug.sourceCurrentPosition.x + " " + debug.sourceCurrentPosition.y);
        // System.out.println("destination current position: "
        //         + debug.destinationCurrentPosition.x + " " + debug.destinationCurrentPosition.y);
        // drawCircle(debug.sourceCurrentPosition, 8f);
        // drawCircle(debug.destinationCurrentPosition, 8f);

        // whiteCircleRenderer.render(projection, debug.sourceLaunchPosition,
        //         launchWindow.source.radius().floatValue());
        // whiteCircleRenderer.render(projection, debug.destinationArrivalPosition,
        //         launchWindow.destination.radius().floatValue());

        // System.out.println("source launch position: "
        //         + debug.sourceLaunchPosition.x + " " + debug.sourceLaunchPosition.y);
        // System.out.println("destination arrival position: "
        //         + debug.destinationArrivalPosition.x + " " + debug.destinationArrivalPosition.y);
        // // Launch and arrival markers
        // drawCircle(debug.sourceLaunchPosition, 10f);
        // drawCircle(debug.destinationArrivalPosition, 10f);

        // // Expected Hohmann arrival point
        // drawCircle(debug.expectedArrivalPosition, 14f);

        // // Transfer curve
        // for (int i = 0; i < debug.transferCurvePoints.size - 1; i++) {
        // Vector2 a = debug.transferCurvePoints.get(i);
        // Vector2 b = debug.transferCurvePoints.get(i + 1);

        // drawLine(a, b);
        // }

        // // Parent-to-arrival radial line, useful for debugging angles
        // drawLine(
        // debug.parentArrivalPosition,
        // debug.expectedArrivalPosition);

        // // Velocity/tangent arrows
        // drawArrow(
        // debug.sourceLaunchPosition,
        // new Vector2(debug.sourceLaunchVelocity).nor().scl(80f));

        // drawArrow(
        // debug.destinationArrivalPosition,
        // new Vector2(debug.destinationArrivalVelocity).nor().scl(80f));
    }

    private void renderInfluenceGuides(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        universe.getState().forEach(body -> {
            if (body instanceof final VisibleBody celestialBody) {
                drawInfluenceGuides(celestialBody);
            }
        });
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawInfluenceGuides(final VisibleBody body) {
        // final Color base = safeColor(body.getColor());
        // final float x = (float)body.x();
        // final float y = body.y().floatValue();
        // final float satelliteOrbitRadius = body.artificialSatelliteOrbitRadius().floatValue();
        // final float outerInfluenceRadius = body.renderedInfluenceRadius().floatValue();

        // shapeRenderer.setColor(base.r, base.g, base.b, SATELLITE_ORBIT_ALPHA);
        // shapeRenderer.circle(x, y, satelliteOrbitRadius, ORBIT_GUIDE_SEGMENTS);

        // shapeRenderer.setColor(base.r, base.g, base.b, OUTER_INFLUENCE_ALPHA);
        // shapeRenderer.circle(x, y, outerInfluenceRadius, ORBIT_GUIDE_SEGMENTS);
    }

    private Color safeColor(final String color) {
        if (color == null)
            return Color.WHITE;
        try {
            return Color.valueOf(color);
        } catch (final Exception e) {
            return Color.WHITE;
        }
    }

    private void loadRenderers(final VisibleUniverse universe) {
        universe.getState().forEach(this::ensureRenderer);
    }

    private void ensureRenderer(final VisibleObject body) {
        if (entityRenderers.containsKey(body)) {
            return;
        }
        EntityRenderer renderer = null;

        for (final RendererRule rule : predicateRenderers) {

            if (rule.predicate().test(body)) {
                renderer = rule.supplier().apply(body);
                break;
            }
        }

        if (renderer == null) {
            renderer = Optional.ofNullable(rendererSupplier.get(body.classification()))
                    .map(func -> func.apply(body))
                    .orElseGet(() -> new DefaultEntityRenderer(body, shapeRenderer));
        }
        entityRenderers.put(body, renderer);
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
