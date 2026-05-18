package com.steel.silent.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.Ship;
import com.steel.silent.navigation.Trajectory;
import com.steel.silent.simulation.OrbitalMechanics;
import com.steel.silent.simulation.Universe;

public class ShipRenderer implements EntityRenderer {

    private static final int   TRAJECTORY_SAMPLES_PER_LEG = 32;
    private static final int   WAITING_ORBIT_SAMPLES       = 32;
    private static final float MAX_HEADING_STEP            = 0.12f;

    /** Colour for normal Hohmann transfer legs. */
    private static final Color NORMAL_LEG_COLOR = new Color(0.4f, 0.8f, 1.0f, 0.6f);
    /** Colour for gravity-assist flyby legs (origin == destination). */
    private static final Color ASSIST_LEG_COLOR = new Color(1.0f, 0.6f, 0.1f, 0.8f);

    private final Ship ship;
    private final ShapeRenderer shapeRenderer;
    private final Universe universe;
    private Float renderedHeading;

    public ShipRenderer(final Ship ship, final ShapeRenderer shapeRenderer, final Universe universe) {
        this.ship = ship;
        this.shapeRenderer = shapeRenderer;
        this.universe = universe;
    }

    @Override
    public void render(final Matrix4 projection) {
        shapeRenderer.setProjectionMatrix(projection);

        // Trajectory: draw a polyline through each leg.
        // Normal transfer legs are cyan; gravity-assist flyby legs are orange.
        final Trajectory trajectory = ship.getTrajectory();
        if (trajectory != null && !trajectory.getLegs().isEmpty()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(NORMAL_LEG_COLOR);
            drawWaitingOrbitSegment(trajectory);
            for (final Trajectory.Leg leg : trajectory.getLegs()) {
                // A gravity-assist arc leg has the same body as both origin and destination.
                final boolean isFlybyArc = leg.origin != null && leg.origin.equals(leg.destination);
                shapeRenderer.setColor(isFlybyArc ? ASSIST_LEG_COLOR : NORMAL_LEG_COLOR);
                float previousX = (float) leg.departureX;
                float previousY = (float) leg.departureY;
                for (int i = 1; i <= TRAJECTORY_SAMPLES_PER_LEG; i++) {
                    final double t = (double) i / TRAJECTORY_SAMPLES_PER_LEG;
                    final float x = (float) leg.xAt(t);
                    final float y = (float) leg.yAt(t);
                    shapeRenderer.line(previousX, previousY, x, y);
                    previousX = x;
                    previousY = y;
                }
            }
            shapeRenderer.end();
        }

        // Ship body: small triangle pointing along motion (or up if orbiting).
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(safeColor(ship.getColor()));
        final float x = ship.x().floatValue();
        final float y = ship.y().floatValue();
        final float r = Math.max(2f, ship.radius().floatValue());
        final float heading = smoothHeading(computeHeading(ship, universe.getSimTime()));
        drawTriangle(x, y, r, heading);
        shapeRenderer.end();
    }

    private void drawWaitingOrbitSegment(final Trajectory trajectory) {
        final long now = universe.getSimTime();
        final long departure = trajectory.departureSimTime();
        if (now >= departure) {
            return;
        }

        float previousX = ship.x().floatValue();
        float previousY = ship.y().floatValue();
        for (int i = 1; i <= WAITING_ORBIT_SAMPLES; i++) {
            final long sampleTime = now + (departure - now) * i / WAITING_ORBIT_SAMPLES;
            final OrbitalMechanics.Vec2 sample = ship.predictOrbitPosition(now, sampleTime);
            final float x = (float) sample.x;
            final float y = (float) sample.y;
            shapeRenderer.line(previousX, previousY, x, y);
            previousX = x;
            previousY = y;
        }
    }

    private float computeHeading(final Ship ship, final long simTime) {
        if (ship.getState() == Ship.State.TRANSITING) {
            final Trajectory t = ship.getTrajectory();
            if (t != null && !t.getLegs().isEmpty()) {
                final Trajectory.Leg first = t.getLegs().get(0);
                if (simTime < first.departureSimTime) {
                    return orbitHeading(ship);
                }

                for (final Trajectory.Leg leg : t.getLegs()) {
                    if (simTime > leg.arrivalSimTime) {
                        continue;
                    }
                    final long span = Math.max(1, leg.arrivalSimTime - leg.departureSimTime);
                    final double progress = Math.max(0.0, Math.min(1.0,
                        (double) (simTime - leg.departureSimTime) / (double) span));
                    final double dx = leg.dxAt(progress);
                    final double dy = leg.dyAt(progress);
                    if (dx != 0 || dy != 0) {
                        return (float) Math.atan2(dy, dx);
                    }
                }
            }
        }
        return (float) (Math.PI / 2.0); // default: pointing up
    }

    private float orbitHeading(final Ship ship) {
        if (ship.getParentBody() == null) {
            return (float) (Math.PI / 2.0);
        }
        final double dx = ship.x().doubleValue() - ship.getParentBody().x().doubleValue();
        final double dy = ship.y().doubleValue() - ship.getParentBody().y().doubleValue();
        if (dx == 0 && dy == 0) {
            return (float) (Math.PI / 2.0);
        }
        return (float) Math.atan2(dx, -dy);
    }

    private float smoothHeading(final float target) {
        if (renderedHeading == null) {
            renderedHeading = target;
            return target;
        }
        final float delta = shortestAngleDelta(renderedHeading, target);
        final float step = Math.max(-MAX_HEADING_STEP, Math.min(MAX_HEADING_STEP, delta));
        renderedHeading = normalizeAngle(renderedHeading + step);
        return renderedHeading;
    }

    private float shortestAngleDelta(final float from, final float to) {
        return normalizeAngle(to - from);
    }

    private float normalizeAngle(final float angle) {
        float out = angle;
        while (out > Math.PI) out -= (float) (Math.PI * 2.0);
        while (out < -Math.PI) out += (float) (Math.PI * 2.0);
        return out;
    }

    private void drawTriangle(final float x, final float y, final float size, final float heading) {
        // Triangle with tip in heading direction, base behind.
        final float tipX = x + (float) Math.cos(heading) * size * 1.6f;
        final float tipY = y + (float) Math.sin(heading) * size * 1.6f;
        final float leftAngle = heading + (float) (Math.PI * 0.85);
        final float rightAngle = heading - (float) (Math.PI * 0.85);
        final float leftX = x + (float) Math.cos(leftAngle) * size;
        final float leftY = y + (float) Math.sin(leftAngle) * size;
        final float rightX = x + (float) Math.cos(rightAngle) * size;
        final float rightY = y + (float) Math.sin(rightAngle) * size;
        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
    }

    private Color safeColor(final String color) {
        if (color == null) return Color.WHITE;
        try {
            return Color.valueOf(color);
        } catch (final Exception e) {
            return Color.WHITE;
        }
    }
}
