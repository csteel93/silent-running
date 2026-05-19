package com.steel.silent.ui.renderers.viewProxies;

import java.util.UUID;

import com.steel.silent.simulation.snapshot.BodyState;

public class ProjectedBody {
    
    private final BodyState body;
    private final MapScale mapScale;

    public ProjectedBody(
            final BodyState body,
            final MapScale mapScale) {
        this.body = body;
        this.mapScale = mapScale;
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
