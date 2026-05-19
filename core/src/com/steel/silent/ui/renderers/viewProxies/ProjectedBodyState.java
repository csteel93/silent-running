package com.steel.silent.ui.renderers.viewProxies;

import java.util.Map;
import java.util.UUID;

import com.steel.silent.simulation.snapshot.BodyState;

/**
 * A read-only view of a single body's simulation state projected into map
 * space. All coordinate and radius values are in map units, not meters.
 * Callers should never cache instances across render frames.
 */
public class ProjectedBodyState {

    private final BodyState body;
    private final WorldProjection projection;

    public ProjectedBodyState(final BodyState body, final WorldProjection projection) {
        this.body = body;
        this.projection = projection;
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
        return projection.x(body.positionMeters().x());
    }

    /** Map-space y coordinate of this body's centre. */
    public double y() {
        return projection.y(body.positionMeters().y());
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
        return projection.worldDistance(body.influenceRadius());
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
}
