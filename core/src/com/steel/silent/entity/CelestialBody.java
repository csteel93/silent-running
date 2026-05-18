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
     * Visual gravitational parameter (μ) used for Hohmann transfer calculations.
     *
     * This is not a real gravitational constant — it is a tunable simulation value
     * calibrated so that Hohmann transfer times look believable at the simulation's
     * time and distance scale.
     *
     * Use {@link com.steel.silent.simulation.OrbitalMechanics#muFromOrbit} to derive
     * a consistent value from a known child satellite's orbital parameters.
     *
     * Default: 1 / (28^2) ≈ 0.00128, which matches the legacy hohmannTimeScale=28
     * compression factor so existing timing is preserved when not explicitly set.
     */
    @Getter @Setter
    private double visualMu = 1.0 / (28.0 * 28.0);

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
