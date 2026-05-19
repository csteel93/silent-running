package com.steel.silent.entity;

import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;

public class FocalPoint extends CelestialBody {

    public FocalPoint(final double mu,
            final double radiusMeters,
            final Vector initialPositionMeters,
            final double initialOrientationRadians,
            final double rotationPeriodSeconds) {
            super(mu, radiusMeters, initialPositionMeters, initialOrientationRadians, rotationPeriodSeconds);
    }

}
