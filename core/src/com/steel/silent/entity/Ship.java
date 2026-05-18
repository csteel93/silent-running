package com.steel.silent.entity;

import lombok.Getter;

import java.math.BigDecimal;
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
    private final BigDecimal radius;
    @Getter
    private final Characteristics characteristics = new Characteristics();
    @Getter
    private final AtomicReference<CelestialBody> localBody;
    @Getter
    private final AtomicReference<BigDecimal> orbitalRadius;
    @Getter
    private final AtomicReference<BigDecimal> orbitalPeriod;
    @Getter
    private final AtomicReference<BigDecimal> relativeAngle = new AtomicReference<>(BigDecimal.ZERO);

    public Ship(final CelestialBody parent,
                final BigDecimal radius,
                final BigDecimal orbitalRadius,
                final BigDecimal orbitalPeriodSeconds) {
        this(parent, radius, orbitalRadius, orbitalPeriodSeconds, Math.random() * Math.PI * 2);
    }

    public Ship(final CelestialBody parent,
                final BigDecimal radius,
                final BigDecimal orbitalRadius,
                final BigDecimal orbitalPeriodSeconds,
                final double initialAngle) {
        this.radius = radius;
        this.localBody = new AtomicReference<>(parent);
        this.orbitalRadius = new AtomicReference<>(orbitalRadius);
        this.orbitalPeriod = new AtomicReference<>(orbitalPeriodSeconds);
        this.relativeAngle.set(BigDecimal.valueOf(initialAngle));
        final double x = parent.getCoordinates().x().doubleValue()
            + orbitalRadius.doubleValue() * Math.cos(initialAngle);
        final double y = parent.getCoordinates().y().doubleValue()
            + orbitalRadius.doubleValue() * Math.sin(initialAngle);
        this.coordinates = new Coordinates(BigDecimal.valueOf(x), BigDecimal.valueOf(y));
        this.characteristics.setClassification("SHIP");
        this.characteristics.setName("Ship-" + id.toString().substring(0, 4));
        this.characteristics.setColor("ffffffff");
    }

    public void update(final long simDelta, final long simTime) {
        updateOrbit(simDelta);
    }

    private void updateOrbit(final long simDelta) {
        final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriod.get().longValue()).toMillis();
        if (orbitTimeMillis <= 0) return;
        final double angularVelocity = (2.0 * Math.PI) / orbitTimeMillis;
        final double radianDelta = angularVelocity * simDelta;

        final double newAngle = relativeAngle.get().doubleValue() + radianDelta;
        relativeAngle.set(BigDecimal.valueOf(newAngle));

        final double cx = localBody.get().getCoordinates().x().doubleValue();
        final double cy = localBody.get().getCoordinates().y().doubleValue();
        final double r = orbitalRadius.get().doubleValue();
        coordinates.update(
            BigDecimal.valueOf(cx + r * Math.cos(newAngle)),
            BigDecimal.valueOf(cy + r * Math.sin(newAngle)));
    }

    @Override public String name() { return characteristics.getName(); }
    @Override public String classification() { return characteristics.getClassification(); }
    @Override public BigDecimal x() { return coordinates.x(); }
    @Override public BigDecimal y() { return coordinates.y(); }
    @Override public BigDecimal aspect() { return coordinates.o(); }
    @Override public BigDecimal radius() { return radius; }
    @Override public String getColor() { return characteristics.getColor(); }
}
