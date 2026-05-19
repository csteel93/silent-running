package com.steel.silent.entity;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

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
        this.influenceRadius = orbitalRadius * Math.pow(mu / focalPoint.getMu(), 2.0 / 5.0);
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
