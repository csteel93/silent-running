package com.steel.silent.entity;

import com.steel.silent.simulation.OrbitalMechanics;
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
    private final CelestialBody parentBody;
    @Getter
    private final BigDecimal orbitalRadius;
    @Getter
    private final BigDecimal orbitalPeriodSeconds;
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
        this.parentBody = parent;
        this.orbitalRadius = orbitalRadius;
        this.orbitalPeriodSeconds = orbitalPeriodSeconds;
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
        final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriodSeconds.longValue()).toMillis();
        if (orbitTimeMillis <= 0) return;
        final double angularVelocity = (2.0 * Math.PI) / orbitTimeMillis;
        final double radianDelta = angularVelocity * simDelta;

        final double newAngle = relativeAngle.get().doubleValue() + radianDelta;
        relativeAngle.set(BigDecimal.valueOf(newAngle));

        final double cx = parentBody.getCoordinates().x().doubleValue();
        final double cy = parentBody.getCoordinates().y().doubleValue();
        final double r = orbitalRadius.doubleValue();
        coordinates.update(
            BigDecimal.valueOf(cx + r * Math.cos(newAngle)),
            BigDecimal.valueOf(cy + r * Math.sin(newAngle)));
    }

    public OrbitalMechanics.Vec2 predictPosition(final long currentSim, final long targetSim) {
        return predictOrbitPosition(currentSim, targetSim);
    }

    public OrbitalMechanics.Vec2 predictOrbitPosition(final long currentSim, final long targetSim) {
        final OrbitalMechanics.Vec2 parentAt = OrbitalMechanics.predict(parentBody, currentSim, targetSim);
        final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriodSeconds.longValue()).toMillis();
        final double omega = orbitTimeMillis > 0 ? (2.0 * Math.PI) / orbitTimeMillis : 0;
        final double angle = relativeAngle.get().doubleValue() + omega * (targetSim - currentSim);
        final double r = orbitalRadius.doubleValue();
        return new OrbitalMechanics.Vec2(parentAt.x + r * Math.cos(angle), parentAt.y + r * Math.sin(angle));
    }

    @Override public String name() { return characteristics.getName(); }
    @Override public String classification() { return characteristics.getClassification(); }
    @Override public BigDecimal x() { return coordinates.x(); }
    @Override public BigDecimal y() { return coordinates.y(); }
    @Override public BigDecimal aspect() { return coordinates.o(); }
    @Override public BigDecimal radius() { return radius; }
    @Override public String getColor() { return characteristics.getColor(); }
}
