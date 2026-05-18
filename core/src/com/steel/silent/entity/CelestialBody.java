package com.steel.silent.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(exclude = "coordinates")
@RequiredArgsConstructor
public class CelestialBody implements IdentifiableBody {

    private static final double DEFAULT_DENSITY = 1.0;
    private static final double INFLUENCE_SCALE = 2.0;
    private static final double MIN_INFLUENCE_RADIUS_MULTIPLIER = 3.0;
    private static final double MAX_RENDERED_INFLUENCE_RADIUS = 300.0;
    private static final double ARTIFICIAL_ORBIT_RADIUS_MULTIPLIER = 1;
    private static final double ARTIFICIAL_ORBIT_CLEARANCE = 5.0;

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    private final BigDecimal radius;
    @Getter
    private final BigDecimal rotationalSpeed;
    @Getter
    protected Characteristics characteristics = new Characteristics();

    /**
     * Visual gravitational parameter (μ) for circular orbit timing.
     *
     * This is a tunable simulation value, expressed in world-units^3/ms^2.
     * Use {@link com.steel.silent.simulation.OrbitalMechanics#muFromOrbit} to
     * derive a consistent value from a known orbit.
     */
    @Getter @Setter
    private double visualMu = 1.0 / (28.0 * 28.0);

    public double getEstimatedMass() {
        return estimateMass(radius);
    }

    public BigDecimal influenceRadius() {
        final double radiusValue = radius.doubleValue();
        final double influence = Math.max(
            radiusValue * MIN_INFLUENCE_RADIUS_MULTIPLIER,
            Math.sqrt(Math.max(0.0, getEstimatedMass())) * INFLUENCE_SCALE);
        return BigDecimal.valueOf(influence);
    }

    public BigDecimal renderedInfluenceRadius() {
        if ("MOON".equals(classification())){
            return artificialSatelliteOrbitRadius();
        }
        return BigDecimal.valueOf(Math.min(
            influenceRadius().doubleValue(),
            MAX_RENDERED_INFLUENCE_RADIUS));
    }

    public BigDecimal artificialSatelliteOrbitRadius() {
        final double satelliteRadius = radius.doubleValue() * ARTIFICIAL_ORBIT_RADIUS_MULTIPLIER
                + ARTIFICIAL_ORBIT_CLEARANCE;
        return BigDecimal.valueOf(satelliteRadius);
    }

    private static double estimateMass(final BigDecimal radius) {
        final double radiusValue = radius.doubleValue();
        return (4.0 / 3.0) * Math.PI * radiusValue * radiusValue * radiusValue * DEFAULT_DENSITY;
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
    public BigDecimal x() {
        return coordinates.x();
    }

    @Override
    public BigDecimal y() {
        return coordinates.y();
    }

    @Override
    public BigDecimal aspect() {
        return coordinates.o();
    }

    @Override
    public BigDecimal radius() {
        return radius;
    }

    @Override
    public String getColor() {
        return characteristics.getColor();
    }
}
