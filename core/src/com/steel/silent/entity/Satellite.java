package com.steel.silent.entity;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

import com.steel.silent.navigation.OrbitalMechanics;
import com.steel.silent.ui.renderers.Vec2d;

public class Satellite extends CelestialBody {

    @Getter
    private final CelestialBody focalPoint;
    // orbital radius meters
    @Getter
    private final double orbitalRadius;
    // orbital period in seconds
    @Getter
    private final double orbitalPeriod;

    @Getter
    private final double influenceRadius;

    @Getter
    private final AtomicReference<Double> relativeAngle;

    @Getter
    private final double angularVelocity;

    /**
     * The initial orbital angle at simulation epoch (t = 0 seconds).
     * Used for deterministic position / velocity prediction at any absolute
     * simulation time without accumulating floating-point drift from the live
     * sim loop.  {@link #relativeAngle} is kept as the live render angle and
     * is updated every tick by {@link com.steel.silent.map.SolarSystem}.
     */
    private final double epochAngle;

    public Satellite(final CelestialBody focalPoint,
            final double radius,
            final double orbitalRadius,
            final double orbitalPeriod,
            final double mu) {
        this(focalPoint, radius, orbitalRadius, orbitalPeriod, mu, 0d, randomAngle());
    }

    public Satellite(final CelestialBody focalPoint,
            final double radius,
            final double orbitalRadius,
            final double orbitalPeriod,
            final double mu,
            final double rotationSpeed,
            final double angle) {
        super(initializeCoordinates(focalPoint, orbitalRadius, angle), radius, mu, rotationSpeed);
        this.focalPoint = focalPoint;
        this.orbitalRadius = orbitalRadius;
        this.orbitalPeriod = orbitalPeriod;
        this.relativeAngle = new AtomicReference<>(angle);
        this.epochAngle = angle;
        this.influenceRadius = orbitalRadius * Math.pow(mu / focalPoint.getMu(), 2.0 / 5.0);
        this.angularVelocity = Math.PI * 2.0 / orbitalPeriod;
    }

    /**
     * Orbital angle at {@code simTimeSeconds} seconds of absolute simulation time.
     * Uses the epoch angle so predictions are correct regardless of how long the
     * live sim loop has been running.
     */
    public double getOrbitAngleRad(final double simTimeSeconds) {
        return OrbitalMechanics.normalizeAngle(epochAngle + angularVelocity * simTimeSeconds);
    }

    @Override
    public Vec2d getWorldPositionMeters(double simTimeSeconds) {
        double angle = epochAngle + angularVelocity * simTimeSeconds;
        double localX = Math.cos(angle) * orbitalRadius;
        double localY = Math.sin(angle) * orbitalRadius;
        Vec2d parentWorldPosition = focalPoint.getWorldPositionMeters(simTimeSeconds);
        return new Vec2d(
                parentWorldPosition.x() + localX,
                parentWorldPosition.y() + localY);
    }

    @Override
    public Vec2d getWorldVelocityMetersPerSecond(double simTimeSeconds) {
        double angle = epochAngle + angularVelocity * simTimeSeconds;
        double localVx = -Math.sin(angle) * orbitalRadius * angularVelocity;
        double localVy =  Math.cos(angle) * orbitalRadius * angularVelocity;
        Vec2d parentVelocity = focalPoint.getWorldVelocityMetersPerSecond(simTimeSeconds);
        return parentVelocity.add(new Vec2d(localVx, localVy));
    }

    private static double randomAngle() {
        return Math.random() * Math.PI * 2;
    }

    private static Coordinates initializeCoordinates(final CelestialBody focalPoint,
            final double orbitalRadius,
            final double angle) {
        final double xOffset = orbitalRadius * Math.cos(angle);
        final double yOffset = orbitalRadius * Math.sin(angle);
        final double xCoord = xOffset + focalPoint.getCoordinates().x();
        final double yCoord = yOffset + focalPoint.getCoordinates().y();
        return new Coordinates(xCoord, yCoord);
    }

}
