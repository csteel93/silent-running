package com.steel.silent.model.body;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

import com.steel.silent.math.Angles;
import com.steel.silent.math.Vector;

@ToString
@EqualsAndHashCode
public class CelestialBody {

    private final UUID id = UUID.randomUUID();
    private final Characteristics characteristics = new Characteristics();
    private final double mu;
    private final double radiusMeters;
    private final Vector initialPositionMeters;
    private final double initialOrientationRadians;
    private final double rotationPeriodSeconds;

    public CelestialBody(
            final double mu,
            final double radiusMeters,
            final Vector initialPositionMeters,
            final double initialOrientationRadians,
            final double rotationPeriodSeconds) {
        this.mu = mu;
        this.radiusMeters = radiusMeters;
        this.initialPositionMeters = initialPositionMeters;
        this.initialOrientationRadians = initialOrientationRadians;
        this.rotationPeriodSeconds = rotationPeriodSeconds;
    }

    public double orientationAt(final double simTimeSeconds) {
        if (rotationPeriodSeconds <= 0.0) {
            return initialOrientationRadians;
        }
        final double angularVelocityRadPerSecond = (Math.PI * 2.0) / rotationPeriodSeconds;
        return Angles.normalizeRadians(initialOrientationRadians + angularVelocityRadPerSecond * simTimeSeconds);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return characteristics.getName();
    }

    public String classification() {
        return characteristics.getClassification();
    }

    public Characteristics characteristics() {
        return characteristics;
    }

    public double mu() {
        return mu;
    }

    public Vector initialPositionMeters() {
        return initialPositionMeters;
    }

    public double rotationPeriodSeconds() {
        return rotationPeriodSeconds;
    }

    public double radiusMeters() {
        return radiusMeters;
    }

    public String color() {
        return characteristics.getColor();
    }
}
