package com.steel.silent.entity;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Satellite extends CelestialBody {

    @Getter
    private final CelestialBody focalPoint;
    @Getter
    private final BigDecimal orbitalRadius;
    @Getter
    private final BigDecimal orbitalSpeed;
    @Getter
    private final AtomicReference<BigDecimal> relativeAngle;

    public Satellite(final CelestialBody focalPoint,
                     final BigDecimal radius,
                     final BigDecimal orbitalRadius,
                     final BigDecimal orbitalSpeed) {
        this(focalPoint, radius, orbitalRadius, orbitalSpeed, randomAngle());
    }

    public Satellite(final CelestialBody focalPoint,
                     final BigDecimal radius,
                     final BigDecimal orbitalRadius,
                     final BigDecimal orbitalSpeed,
                     final double angle) {
        super(initializeCoordinates(focalPoint, orbitalRadius, angle), radius);
        this.focalPoint = focalPoint;
        this.orbitalRadius = orbitalRadius;
        this.orbitalSpeed = orbitalSpeed;
        this.relativeAngle = new AtomicReference<>(BigDecimal.valueOf(angle));
    }

    private static double randomAngle() {
        return Math.random() * Math.PI * 2;
    }

    private static Coordinates initializeCoordinates(final CelestialBody focalPoint,
                                                     final BigDecimal orbitalRadius,
                                                     final double angle) {
        final BigDecimal xOffset = orbitalRadius.multiply(BigDecimal.valueOf(Math.cos(angle)));
        final BigDecimal yOffset = orbitalRadius.multiply(BigDecimal.valueOf(Math.sin(angle)));
        final BigDecimal xCoord = xOffset.add(focalPoint.getCoordinates().getXCoord());
        final BigDecimal yCoord = yOffset.add(focalPoint.getCoordinates().getYCoord());
        return new Coordinates(xCoord, yCoord);
    }

}
