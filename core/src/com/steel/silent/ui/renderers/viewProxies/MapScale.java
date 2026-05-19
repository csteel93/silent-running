package com.steel.silent.ui.renderers.viewProxies;

import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.math.Vector;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;

import lombok.Getter;

public class MapScale {

    private static final double MAP_FILL = 0.80;
    private static final double MIN_BODY_RADIUS = 0.001;
    private static final double MAX_BODY_RADIUS = 40.0;

    // private static final double MIN_BODY_RADIUS = 1.0;
    // private static final double MAX_BODY_RADIUS = 20.0;
    private static final double MIN_CHILD_ORBIT_GAP = 0.08;

    private final double centerX;
    private final double centerY;
    @Getter
    private final double metersPerMapUnit;
    private final double bodyScale;

    private MapScale(final double centerX,
            final double centerY,
            final double metersPerMapUnit,
            final double bodyScale) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.metersPerMapUnit = metersPerMapUnit;
        this.bodyScale = bodyScale;
    }

    // public static MapScale fromSnapshot(final SimulationSnapshot snapshot,
    // final double mapWidth,
    // final double mapHeight,
    // final double bodyScale) {
    // final double mapRadius = Math.min(mapWidth, mapHeight) * MAP_FILL;
    // final double worldRadius = snapshot.bodies().stream()
    // .mapToDouble(MapScale::distanceFromOrigin)
    // .max()
    // .orElse(1.0);
    // return new MapScale(
    // mapWidth * 0.5,
    // mapHeight * 0.5,
    // Math.max(1.0, worldRadius / mapRadius),
    // bodyScale);
    // }

    public static MapScale fromUniverse(final Universe universe,
            final double mapWidth,
            final double mapHeight,
            final double bodyScale) {
        final double mapRadius = Math.min(mapWidth, mapHeight) * 0.5 * MAP_FILL;
        final double worldRadius = universe.buildSnapshot().bodies().stream()
                .mapToDouble(MapScale::distanceFromOrigin)
                .max()
                .orElse(1.0);
        System.out.println("map radius:  " + mapRadius);

        System.out.println("world radius:  " + worldRadius);
        return new MapScale(
                mapWidth * 0.5,
                mapHeight * 0.5,
                Math.max(1.0, worldRadius / mapRadius),
                bodyScale);
    }

    public Vector getCoordinates(final Vector coords) {
        return new Vector(x(coords.x()), y(coords.y()));
    }

    public double distance(final double meters) {
        return meters / metersPerMapUnit;
    }

    public double x(final double meters) {
        return centerX + meters / metersPerMapUnit;
    }

    public double y(final double meters) {
        return centerY + meters / metersPerMapUnit;
    }

    public double radius(final double meters) {
        final double scaledRadius = meters / metersPerMapUnit * bodyScale;
        return Math.max(MIN_BODY_RADIUS, Math.min(MAX_BODY_RADIUS, scaledRadius));
    }

    // public double childOrbitDistance(final double rawDistanceMeters,
    //         final double parentRadiusMeters,
    //         final double childRadiusMeters) {
    //     final double scaledDistance = distance(rawDistanceMeters);
    //     final double minDistance = radius(parentRadiusMeters)
    //             + radius(childRadiusMeters)
    //             + MIN_CHILD_ORBIT_GAP;
    //     return Math.max(scaledDistance, minDistance);
    // }

    private static double distanceFromOrigin(final BodyState body) {
        return Math.hypot(body.positionMeters().x(), body.positionMeters().y()) + body.radiusMeters();
    }
}
