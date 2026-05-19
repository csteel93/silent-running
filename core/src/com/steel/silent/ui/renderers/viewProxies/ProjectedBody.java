package com.steel.silent.ui.renderers.viewProxies;

import java.util.Map;
import java.util.UUID;

import com.steel.silent.math.Vector;
import com.steel.silent.simulation.snapshot.BodyState;

public class ProjectedBody {

    private final BodyState body;
    private final MapScale mapScale;
    private final Map<UUID, BodyState> bodiesById;

    public ProjectedBody(
            final BodyState body,
            final MapScale mapScale) {
        this(body, mapScale, Map.of());
    }

    public ProjectedBody(
            final BodyState body,
            final MapScale mapScale,
            final Map<UUID, BodyState> bodiesById) {
        this.body = body;
        this.mapScale = mapScale;
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

    public double x() {
        return mapScale.x(body.positionMeters().x());
    }

    public double y() {
        return mapScale.y(body.positionMeters().y());
    }

    public double radius() {
        return mapScale.radius(body.radiusMeters());
    }

    public double getInfluenceRadius() {
        return mapScale.radius(body.influenceRadius());
    }

    public double getEncounterRadius() {
        return Math.min(radius() * 50.0, getInfluenceRadius() * 0.20);
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

    // private Vector projectedPosition() {
    // // return mapScale.getCoordinates(body.positionMeters());
    // final UUID primaryBodyId = body.primaryBodyId();
    // if (primaryBodyId == null) {
    // return mapScale.getCoordinates(body.positionMeters());
    // }

    // final BodyState primary = bodiesById.get(primaryBodyId);
    // if (primary == null) {
    // return mapScale.getCoordinates(body.positionMeters());
    // }

    // final Vector rawOffset = body.positionMeters().sub(primary.positionMeters());
    // final double rawDistanceMeters = rawOffset.len();
    // if (rawDistanceMeters == 0.0) {
    // return mapScale.getCoordinates(body.positionMeters());
    // }

    // final Vector primaryPosition =
    // mapScale.getCoordinates(primary.positionMeters());
    // final Vector direction = rawOffset.nor();
    // final double projectedDistance = mapScale.childOrbitDistance(
    // rawDistanceMeters,
    // primary.radiusMeters(),
    // body.radiusMeters());

    // return primaryPosition.add(direction.scl(projectedDistance));
    // }
}
