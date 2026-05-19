package com.steel.silent.model.body;

import com.steel.silent.math.Vector;
import com.steel.silent.model.orbit.Orbit;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Satellite extends CelestialBody {

    private final Orbit orbit;
    private final double influenceRadius;

    public Satellite(
            final Orbit orbit,
            final double mu,
            final double radiusMeters,
            final Vector initialPositionMeters,
            final double initialOrientationRadians,
            final double rotationPeriodSeconds) {
        super(mu, radiusMeters, initialPositionMeters, initialOrientationRadians, rotationPeriodSeconds);
        this.orbit = orbit;
        this.influenceRadius = calculateInfluenceRadius(orbit, mu);
    }

    private double calculateInfluenceRadius(final Orbit orbit, final double mu) {
        return orbit.radiusMeters() * Math.pow(mu / orbit.primary().mu(), 2.0 / 5.0);
    }

    public Orbit orbit() {
        return orbit;
    }

    public CelestialBody primary() {
        return orbit.primary();
    }

    public double influenceRadius(){
        return influenceRadius;
    }

}
