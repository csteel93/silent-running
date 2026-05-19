package com.steel.silent.entity;

import lombok.Getter;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A spacecraft in a stable circular orbit around a parent body.
 */
public class Ship implements IdentifiableBody {

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    private final double radius;
    @Getter
    private final Characteristics characteristics = new Characteristics();
    @Getter
    private final AtomicReference<CelestialBody> localBody;
    @Getter
    private final AtomicReference<Double> orbitalRadius;
    @Getter
    private final AtomicReference<Double> orbitalPeriod;
    @Getter
    private final AtomicReference<Double> relativeAngle = new AtomicReference<>(0d);

    public Ship(final CelestialBody parent,
            final double radius,
            final double orbitalRadius,
            final double orbitalPeriodSeconds) {
        this(parent, radius, orbitalRadius, orbitalPeriodSeconds, Math.random() * Math.PI * 2);
    }

    public Ship(final CelestialBody parent,
            final double radius,
            final double orbitalRadius,
            final double orbitalPeriodSeconds,
            final double initialAngle) {
        this.radius = radius;
        this.localBody = new AtomicReference<>(parent);
        this.orbitalRadius = new AtomicReference<>(orbitalRadius);
        this.orbitalPeriod = new AtomicReference<>(orbitalPeriodSeconds);
        this.relativeAngle.set(initialAngle);
        final double x = parent.getCoordinates().x()
                + orbitalRadius * Math.cos(initialAngle);
        final double y = parent.getCoordinates().y()
                + orbitalRadius * Math.sin(initialAngle);
        this.coordinates = new Coordinates(x, y);
        this.characteristics.setClassification("SHIP");
        this.characteristics.setName("Ship-" + id.toString().substring(0, 4));
        this.characteristics.setColor("ffffffff");
    }

    public void update(final long simDelta, final long simTime) {
        updateOrbit(simDelta);
    }

    private void updateOrbit(final long simDelta) {
        final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriod.get().longValue()).toMillis();
        if (orbitTimeMillis <= 0)
            return;
        final double angularVelocity = (2.0 * Math.PI) / orbitTimeMillis;
        final double radianDelta = angularVelocity * simDelta;

        final double newAngle = relativeAngle.get().doubleValue() + radianDelta;
        relativeAngle.set(newAngle);

        final double cx = localBody.get().getCoordinates().x();
        final double cy = localBody.get().getCoordinates().y();
        final double r = orbitalRadius.get().doubleValue();
        coordinates.update(cx + r * Math.cos(newAngle),
                cy + r * Math.sin(newAngle));
    }

    @Override
    public String name() {
        return characteristics.getName();
    }

    @Override
    public String classification() {
        return characteristics.getClassification();
    }

    @Override
    public double x() {
        return coordinates.x();
    }

    @Override
    public double y() {
        return coordinates.y();
    }

    @Override
    public double aspect() {
        return coordinates.o();
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public String getColor() {
        return characteristics.getColor();
    }

}
