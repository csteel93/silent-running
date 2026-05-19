package com.steel.silent.ui.renderers.viewProxies;

import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;

/**
 * Converts SI-unit simulation positions and radii into map-space coordinates
 * for rendering. Orbital distances are scaled uniformly to fit the visible
 * map area; body radii are independently exaggerated for legibility since
 * physically honest sizes are sub-pixel at solar-system scale.
 *
 * <p>All methods that accept meters return map-space units. The map origin
 * (0, 0) in simulation space maps to ({@code centerX}, {@code centerY}).</p>
 */
public class WorldProjection {

    /** Fraction of the map radius used by the furthest body's orbit. */
    private static final double MAP_FILL = 0.80;

    /**
     * Minimum visual body radius in map units. Keeps sub-km moons (Phobos,
     * Deimos) visible as selectable dots in the solar overview even when
     * their true projected size is well below 1 pixel.
     */
    private static final double MIN_BODY_RADIUS = 2.0;

    /**
     * Maximum visual body radius in map units. Prevents the Sun from
     * dominating the view when body exaggeration is large.
     */
    private static final double MAX_BODY_RADIUS = 40.0;

    private final double centerX;
    private final double centerY;
    private final double metersPerMapUnit;
    private final double bodyExaggeration;

    private WorldProjection(final double centerX,
            final double centerY,
            final double metersPerMapUnit,
            final double bodyExaggeration) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.metersPerMapUnit = metersPerMapUnit;
        this.bodyExaggeration = bodyExaggeration;
    }

    /**
     * Builds a projection that fits all body positions from {@code snapshot}
     * into a map of the given pixel dimensions.
     *
     * @param bodyExaggeration multiplier applied to physical body radii.
     *   Values around 200 make inner planets visible without zoom;
     *   the result is clamped to [{@value #MIN_BODY_RADIUS}, {@value #MAX_BODY_RADIUS}].
     */
    public static WorldProjection fromSnapshot(final SimulationSnapshot snapshot,
            final double mapWidth,
            final double mapHeight,
            final double bodyExaggeration) {
        final double mapRadius = Math.min(mapWidth, mapHeight) * 0.5 * MAP_FILL;
        final double worldRadius = snapshot.bodies().stream()
                .mapToDouble(WorldProjection::distanceFromOrigin)
                .max()
                .orElse(1.0);
        return new WorldProjection(
                mapWidth * 0.5,
                mapHeight * 0.5,
                Math.max(1.0, worldRadius / mapRadius),
                bodyExaggeration);
    }

    /** Projects the x-component of a simulation position (meters) to map units. */
    public double x(final double positionMeters) {
        return centerX + positionMeters / metersPerMapUnit;
    }

    /** Projects the y-component of a simulation position (meters) to map units. */
    public double y(final double positionMeters) {
        return centerY + positionMeters / metersPerMapUnit;
    }

    /**
     * Projects a distance (meters) to map units without adding the map center
     * offset. Use for orbit radii, influence rings, and gap calculations.
     */
    public double worldDistance(final double meters) {
        return meters / metersPerMapUnit;
    }

    /**
     * Projects a body radius (meters) to a visually exaggerated map-unit
     * radius. The result is clamped to [{@value #MIN_BODY_RADIUS},
     * {@value #MAX_BODY_RADIUS}] so every body remains visible and no single
     * body dominates the view.
     *
     * <p>Intentional exaggeration: {@code bodyExaggeration} is typically 200×
     * so planets subtend several map units instead of fractions of a pixel.
     * This is a display-only transform; simulation values are unchanged.</p>
     */
    public double bodyRadius(final double radiusMeters) {
        final double scaled = radiusMeters / metersPerMapUnit * bodyExaggeration;
        return Math.max(MIN_BODY_RADIUS, Math.min(MAX_BODY_RADIUS, scaled));
    }

    private static double distanceFromOrigin(final BodyState body) {
        return Math.hypot(body.positionMeters().x(), body.positionMeters().y()) + body.radiusMeters();
    }
}
