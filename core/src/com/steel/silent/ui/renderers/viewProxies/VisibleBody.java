package com.steel.silent.ui.renderers.viewProxies;

import com.steel.silent.entity.CelestialBody;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class VisibleBody implements VisibleObject {

    protected final CelestialBody body;
    protected final MapScale mapScale;

    public VisibleBody(final CelestialBody body, final MapScale mapScale) {
        this.body = body;
        this.mapScale = mapScale;
    }

    public static VisibleBody fromCelestialBody(final CelestialBody body, final MapScale mapScale) {
        return new VisibleBody(body, mapScale);
    }

    @Override
    public String name() {
        return body.name();
    }

    @Override
    public String classification() {
        return body.classification();
    }

    @Override
    public double x() {
        return mapScale.x(body.x());
    }

    @Override
    public double y() {
        return mapScale.y(body.y());
    }

    @Override
    public double aspect() {
        return body.aspect();
    }

    @Override
    public double radius() {
        return mapScale.radius(body.radius());
    }

    @Override
    public String getColor() {
        return body.getColor();
    }

}
