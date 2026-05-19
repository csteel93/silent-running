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
    private static final double MIN_BODY_RADIUS = 0.01;
    private static final double MAX_BODY_RADIUS = 480000000.0;
    private static final double MIN_CHILD_ORBIT_GAP = 8.0;

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
    //         final double mapWidth,
    //         final double mapHeight,
    //         final double bodyScale) {
    //     final double mapRadius = Math.min(mapWidth, mapHeight) * MAP_FILL;
    //     final double worldRadius = snapshot.bodies().stream()
    //             .mapToDouble(MapScale::distanceFromOrigin)
    //             .max()
    //             .orElse(1.0);
    //     return new MapScale(
    //             mapWidth * 0.5,
    //             mapHeight * 0.5,
    //             Math.max(1.0, worldRadius / mapRadius),
    //             bodyScale);
    // }

    public static MapScale fromUniverse(final Universe universe,
            final double mapWidth,
            final double mapHeight,
            final double bodyScale) {
        final double mapRadius = Math.min(mapWidth, mapHeight) * MAP_FILL;
        final double worldRadius = universe.buildSnapshot().bodies().stream()
                .mapToDouble(MapScale::distanceFromOrigin)
                .max()
                .orElse(1.0);
        return new MapScale(
                mapWidth * 0.5,
                mapHeight * 0.5,
                Math.max(1.0, worldRadius / mapRadius),
                bodyScale);
    }

    public Vector getCoordinates(final Vector coords) {
        return new Vector(x(coords.x()), y(coords.y()));
    }

    public double x(final double meters) {
        return centerX + meters / metersPerMapUnit;
    }

    public double y(final double meters) {
        return centerY + meters / metersPerMapUnit;
    }

    // public double x(final Satellite satellite) {
    // return childCoordinate(satellite, true);
    // }

    // public double y(final Satellite satellite) {
    // return childCoordinate(satellite, false);
    // }

    public double radius(final double meters) {
        final double scaledRadius = meters / metersPerMapUnit * bodyScale;
        return Math.max(MIN_BODY_RADIUS, Math.min(MAX_BODY_RADIUS, scaledRadius));
    }

    private static double distanceFromOrigin(final BodyState body) {
        return Math.hypot(body.positionMeters().x(), body.positionMeters().y()) + body.radiusMeters();
    }

    // private double childCoordinate(final Satellite satellite, final boolean
    // xAxis) {
    // if ("STAR".equals(satellite.getFocalPoint().classification())) {
    // return xAxis ? x(satellite.initialPositionX()) :
    // y(satellite.initialPositionY());
    // }

    // final double parentX = satellite.getFocalPoint().initialPositionX();
    // final double parentY = satellite.getFocalPoint().initialPositionY();
    // final double dx = satellite.initialPositionX() - parentX;
    // final double dy = satellite.initialPositionY() - parentY;
    // final double realDistance = Math.hypot(dx, dy);
    // if (realDistance <= 0.0) {
    // return xAxis ? x(satellite.initialPositionX()) :
    // y(satellite.initialPositionY());
    // }

    // final double scaledDistance = realDistance / metersPerMapUnit;
    // final double minDistance = radius(satellite.getFocalPoint().radiusMeters())
    // + radius(satellite.radiusMeters())
    // + MIN_CHILD_ORBIT_GAP;
    // final double visualDistance = Math.max(scaledDistance, minDistance);
    // final double parentMapX = x(parentX);
    // final double parentMapY = y(parentY);
    // final double offset = (xAxis ? dx : dy) / realDistance * visualDistance;
    // return (xAxis ? parentMapX : parentMapY) + offset;
    // }
}
