package com.steel.silent.model.body;

import com.steel.silent.math.Vector;

public class FocalPoint extends CelestialBody {

    public FocalPoint(final double mu,
            final double radiusMeters,
            final Vector initialPositionMeters,
            final double initialOrientationRadians,
            final double rotationPeriodSeconds) {
            super(mu, radiusMeters, initialPositionMeters, initialOrientationRadians, rotationPeriodSeconds);
    }

}
