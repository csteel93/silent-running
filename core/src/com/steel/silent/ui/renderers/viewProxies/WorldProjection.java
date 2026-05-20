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

    private final double centerX;
    private final double centerY;
    private final double metersPerMapUnit;
    private final ProjectionScale scale;

    private WorldProjection(final double centerX,
            final double centerY,
            final double metersPerMapUnit,
            final ProjectionScale scale) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.metersPerMapUnit = metersPerMapUnit;
        this.scale = scale;
    }

    /**
     * Builds a projection that fits all body positions from {@code snapshot}
     * into a map of the given pixel dimensions.
     */
    public static WorldProjection fromSnapshot(final SimulationSnapshot snapshot,
            final double mapWidth,
            final double mapHeight) {
        return fromSnapshot(snapshot, mapWidth, mapHeight, ProjectionScale.DEFAULT);
    }

    public static WorldProjection fromSnapshot(final SimulationSnapshot snapshot,
            final double mapWidth,
            final double mapHeight,
            final ProjectionScale scale) {
        final double mapRadius = Math.min(mapWidth, mapHeight) * 0.5 * scale.mapFill();
        final double worldRadius = snapshot.bodies().stream()
                .mapToDouble(WorldProjection::distanceFromOrigin)
                .max()
                .orElse(1.0);
        return new WorldProjection(
                mapWidth * 0.5,
                mapHeight * 0.5,
                Math.max(1.0, worldRadius / mapRadius),
                scale);
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
     * radius. The result is clamped by {@link ProjectionScale} so every body remains visible and no single
     * body dominates the view.
     *
     * <p>Intentional exaggeration: {@link ProjectionScale#bodyExaggeration()}
     * is typically above 1×
     * so planets subtend several map units instead of fractions of a pixel.
     * This is a display-only transform; simulation values are unchanged.</p>
     */
    public double bodyRadius(final double radiusMeters) {
        final double scaled = radiusMeters / metersPerMapUnit * scale.bodyExaggeration();
        return Math.max(scale.minBodyRadius(), Math.min(scale.maxBodyRadius(), scaled));
    }

    private static double distanceFromOrigin(final BodyState body) {
        return Math.hypot(body.positionMeters().x(), body.positionMeters().y()) + body.radiusMeters();
    }
}
