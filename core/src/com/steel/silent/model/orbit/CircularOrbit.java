package com.steel.silent.model.orbit;

import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;

import lombok.ToString;

@ToString
public class CircularOrbit implements Orbit {

    private final CelestialBody primary;
    private final double radiusMeters;
    private final double periodSeconds;
    private final double epochAngleRad;

    public CircularOrbit(final CelestialBody primary,
            final double radiusMeters,
            final double periodSeconds) {
        this.primary = primary;
        this.radiusMeters = radiusMeters;
        this.periodSeconds = periodSeconds;
        this.epochAngleRad = randomAngle();
    }

    private static double randomAngle() {
        return Math.random() * Math.PI * 2;
    }

    @Override
    public CelestialBody primary() {
        return primary;
    }

    @Override
    public OrbitState stateAt(final double timeSeconds) {
        final double angularVelocityRadPerSecond = (Math.PI * 2.0) / periodSeconds;
        final double angleRad = epochAngleRad + angularVelocityRadPerSecond * timeSeconds;

        final OrbitState primaryState = stateForPrimary(timeSeconds);

        final Vector localPosition = new Vector(
                Math.cos(angleRad) * radiusMeters,
                Math.sin(angleRad) * radiusMeters);

        final Vector localVelocity = new Vector(
                -Math.sin(angleRad) * radiusMeters * angularVelocityRadPerSecond,
                Math.cos(angleRad) * radiusMeters * angularVelocityRadPerSecond);

        return new OrbitState(
                primaryState.positionMeters().add(localPosition),
                primaryState.velocityMetersPerSecond().add(localVelocity),
                angleRad);
    }

    private OrbitState stateForPrimary(final double timeSeconds) {
        if (primary instanceof Satellite satellite) {
            return satellite.orbit().stateAt(timeSeconds);
        }
        return OrbitState.stationary(primary.initialPositionMeters());
    }

    @Override
    public double radiusMeters() {
        return radiusMeters;
    }

    @Override
    public double periodSeconds() {
        return periodSeconds;
    }
}
