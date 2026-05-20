package com.steel.silent.ui.renderers.viewProxies;

import java.util.Map;
import java.util.UUID;

import com.steel.silent.math.Vector;
import com.steel.silent.simulation.snapshot.BodyState;

/**
 * A read-only view of a single body's simulation state projected into map
 * space. All coordinate and radius values are in map units, not meters.
 * Callers should never cache instances across render frames.
 */
public class ProjectedBodyState {

    private final BodyState body;
    private final WorldProjection projection;
    private final Map<UUID, BodyState> bodiesById;

    public ProjectedBodyState(final BodyState body, final WorldProjection projection) {
        this(body, projection, Map.of());
    }

    public ProjectedBodyState(final BodyState body,
            final WorldProjection projection,
            final Map<UUID, BodyState> bodiesById) {
        this.body = body;
        this.projection = projection;
        this.bodiesById = bodiesById;
    }

    public UUID id() {
        return body.bodyId();
    }

    public String name() {
        return body.name();
    }

    public String classification() {
        return body.classification();
    }

    /** Map-space x coordinate of this body's centre. */
    public double x() {
        return projectedPosition().x();
    }

    /** Map-space y coordinate of this body's centre. */
    public double y() {
        return projectedPosition().y();
    }

    /**
     * Visually exaggerated map-space radius (display only; clamped to a
     * minimum so every body remains selectable).
     */
    public double radius() {
        return projection.bodyRadius(body.radiusMeters());
    }

    /**
     * Map-space radius of the body's gravitational influence sphere, projected
     * as a true distance (no body exaggeration applied).
     */
    public double getInfluenceRadius() {
        return projection.influenceRadius(body.influenceRadius(), body.radiusMeters());
    }

    /**
     * Map-space radius of the close-approach encounter zone. Kept at 20 % of
     * the influence sphere so it stays inside and doesn't overlap sibling orbits.
     */
    public double getEncounterRadius() {
        return getInfluenceRadius() * 0.20;
    }

    public double orientationRad() {
        return body.orientationRad();
    }

    public String color() {
        return body.color();
    }

    public BodyState bodyState() {
        return body;
    }

    private Vector projectedPosition() {
        final UUID primaryBodyId = body.primaryBodyId();
        if (primaryBodyId == null) {
            return rawProjectedPosition(body);
        }

        final BodyState primary = bodiesById.get(primaryBodyId);
        if (primary == null || "STAR".equals(primary.classification())) {
            return rawProjectedPosition(body);
        }

        final Vector rawOffset = body.positionMeters().sub(primary.positionMeters());
        final double rawDistanceMeters = rawOffset.len();
        if (rawDistanceMeters == 0.0) {
            return rawProjectedPosition(body);
        }

        final Vector primaryPosition = rawProjectedPosition(primary);
        final Vector direction = rawOffset.nor();
        final double visualDistance = visualDistanceFromPrimary(rawDistanceMeters, primary);

        return primaryPosition.add(direction.scl(visualDistance));
    }

    private double visualDistanceFromPrimary(final double rawDistanceMeters, final BodyState primary) {
        if ("SHIP".equals(body.classification())) {
            return projection.shipOrbitDistance(
                    rawDistanceMeters,
                    primary.radiusMeters(),
                    primary.influenceRadius(),
                    body.radiusMeters());
        }
        return projection.childOrbitDistance(
                rawDistanceMeters,
                primary.radiusMeters(),
                body.radiusMeters());
    }

    private Vector rawProjectedPosition(final BodyState state) {
        return new Vector(
                projection.x(state.positionMeters().x()),
                projection.y(state.positionMeters().y()));
    }
}
