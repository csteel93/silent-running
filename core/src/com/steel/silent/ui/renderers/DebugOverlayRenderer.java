package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.navigation.HohmannEstimate;
import com.steel.silent.navigation.Trajectory;
import com.steel.silent.navigation.TrajectoryPlanner;
import com.steel.silent.simulation.OrbitalMechanics;
import com.steel.silent.simulation.Universe;

/**
 * Optional debug overlay that renders route-planning internals.
 *
 * <p>Enabled by setting the static flag {@link #enabled} to {@code true}
 * (or via system property {@code silent.debug.overlay=true} at startup).
 * The overlay renders on top of the normal scene.</p>
 *
 * <h2>What is drawn</h2>
 * <ul>
 *   <li>Sphere-of-influence rings for every {@link Satellite} in the universe.</li>
 *   <li>The active trajectory re-drawn with per-leg colour coding:
 *     <ul>
 *       <li>Cyan — normal Hohmann transfer leg.</li>
 *       <li>Orange — gravity-assist flyby leg.</li>
 *     </ul>
 *   </li>
 *   <li>Hohmann estimate info for the ship's current trajectory
 *       (printed to stdout when the trajectory changes).</li>
 *   <li>Predicted destination arrival position (small circle).</li>
 * </ul>
 */
public final class DebugOverlayRenderer {

    /** Toggle debug rendering at runtime: {@code DebugOverlayRenderer.enabled = true;}. */
    public static boolean enabled = readEnabledProperty();

    private static final int    CIRCLE_STEPS          = 48;
    private static final int    TRAJECTORY_SAMPLES    = 32;
    private static final Color  SOI_COLOR             = new Color(0.8f, 0.5f, 0.1f, 0.25f);
    private static final Color  ASSIST_LEG_COLOR      = new Color(1.0f, 0.6f, 0.1f, 0.85f);
    private static final Color  NORMAL_LEG_COLOR      = new Color(0.3f, 0.8f, 1.0f, 0.70f);
    private static final Color  ARRIVAL_POINT_COLOR   = new Color(0.2f, 1.0f, 0.4f, 0.9f);

    private final Universe       universe;
    private final ShapeRenderer  shapeRenderer;

    // Tracks the last trajectory to detect changes and log estimates.
    private Trajectory lastLoggedTrajectory;

    public DebugOverlayRenderer(final Universe universe, final ShapeRenderer shapeRenderer) {
        this.universe     = universe;
        this.shapeRenderer = shapeRenderer;
    }

    public void render(final Matrix4 projection) {
        if (!enabled) return;
        shapeRenderer.setProjectionMatrix(projection);

        // --- 1. Sphere-of-influence rings for all satellites ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        universe.getState().forEach(body -> {
            if (body instanceof final Satellite sat) {
                drawCircle(
                    (float) sat.x().doubleValue(),
                    (float) sat.y().doubleValue(),
                    (float) sat.getInfluenceRadius().doubleValue(),
                    SOI_COLOR);
            }
        });
        shapeRenderer.end();

        // --- 2. Per-leg colour-coded trajectory + predicted arrival position ---
        universe.getShips().forEach(ship -> renderShipDebug(ship, projection));
    }

    private void renderShipDebug(final Ship ship, final Matrix4 projection) {
        final Trajectory traj = ship.getTrajectory();
        if (traj == null || traj.getLegs().isEmpty()) return;

        // Log Hohmann estimate when the trajectory changes.
        if (traj != lastLoggedTrajectory) {
            lastLoggedTrajectory = traj;
            logHohmannEstimate(ship, traj);
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (final Trajectory.Leg leg : traj.getLegs()) {
            // Identify flyby legs by checking if origin == destination (assist arc) or
            // if the leg's fuel cost is below the chord-cost threshold for a flyby.
            final boolean isFlybyLeg = leg.origin != null
                    && leg.origin.equals(leg.destination);
            final Color legColor = isFlybyLeg ? ASSIST_LEG_COLOR : NORMAL_LEG_COLOR;
            shapeRenderer.setColor(legColor);

            float prevX = (float) leg.departureX;
            float prevY = (float) leg.departureY;
            for (int i = 1; i <= TRAJECTORY_SAMPLES; i++) {
                final double t = (double) i / TRAJECTORY_SAMPLES;
                final float x = (float) leg.xAt(t);
                final float y = (float) leg.yAt(t);
                shapeRenderer.line(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // --- Predicted arrival position ---
        final Trajectory.Leg last = traj.getLegs().get(traj.getLegs().size() - 1);
        if (last.destination != null) {
            final long arrivalTime = last.arrivalSimTime;
            final OrbitalMechanics.Vec2 predicted =
                    OrbitalMechanics.predict(last.destination, universe.getSimTime(), arrivalTime);
            shapeRenderer.setColor(ARRIVAL_POINT_COLOR);
            drawCross((float) predicted.x, (float) predicted.y, 6.0f);
        }

        shapeRenderer.end();
    }

    // -------------------------------------------------------------------------
    // Drawing primitives
    // -------------------------------------------------------------------------

    private void drawCircle(final float cx, final float cy, final float r, final Color color) {
        if (r <= 0) return;
        shapeRenderer.setColor(color);
        float prevX = cx + r;
        float prevY = cy;
        for (int i = 1; i <= CIRCLE_STEPS; i++) {
            final double angle = (Math.PI * 2.0 * i) / CIRCLE_STEPS;
            final float nx = cx + (float) (r * Math.cos(angle));
            final float ny = cy + (float) (r * Math.sin(angle));
            shapeRenderer.line(prevX, prevY, nx, ny);
            prevX = nx;
            prevY = ny;
        }
    }

    private void drawCross(final float x, final float y, final float size) {
        shapeRenderer.line(x - size, y, x + size, y);
        shapeRenderer.line(x, y - size, x, y + size);
    }

    // -------------------------------------------------------------------------
    // Logging
    // -------------------------------------------------------------------------

    private static void logHohmannEstimate(final Ship ship, final Trajectory traj) {
        if (traj.getLegs().isEmpty()) return;
        final Trajectory.Leg first = traj.getLegs().get(0);
        if (first.origin == null || first.destination == null) return;

        final HohmannEstimate est = TrajectoryPlanner.estimateHohmann(
                first.origin, first.destination,
                first.departureSimTime, first.departureSimTime);

        if (est != null) {
            System.out.printf(
                "[debug] Hohmann %s→%s | r1=%.0f r2=%.0f a=%.0f | "
                + "T=%.0fms | ΔV1=%.4f ΔV2=%.4f | %s%n",
                first.origin.name(), first.destination.name(),
                est.r1(), est.r2(), est.semiMajorAxis(),
                est.transferTime(),
                est.deltaV1(), est.deltaV2(),
                est.isOutbound() ? "outbound" : "inbound");
        }

        System.out.printf("[debug] route: %d legs | ETA sim+%dms%n",
                traj.getLegs().size(),
                traj.arrivalSimTime() - traj.departureSimTime());
    }

    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------

    private static boolean readEnabledProperty() {
        return "true".equalsIgnoreCase(System.getProperty("silent.debug.overlay", "false"));
    }
}
