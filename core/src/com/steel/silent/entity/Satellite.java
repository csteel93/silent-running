package com.steel.silent.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Satellite extends CelestialBody {

    @Getter
    private final CelestialBody focalPoint;
    @Getter
    private final BigDecimal orbitalRadius;
    @Getter
    private final BigDecimal influenceRadius;
    @Getter
    private final BigDecimal orbitalSpeed;
    @Getter
    private final AtomicReference<BigDecimal> relativeAngle;

    /**
     * Tunable gravity-assist strength for this body.
     *
     * Used in the turn-angle formula:
     *   turnAngle = gravityAssistStrength / (periapsisDistance * relativeSpeed²)
     *
     * Higher values produce larger flyby deflections. Calibrate so that a
     * typical approach speed gives a meaningful (but not extreme) bend angle.
     * See {@link com.steel.silent.navigation.TrajectoryConfigurations} for
     * the min/max turn angle clamps.
     */
    @Getter @Setter
    private double gravityAssistStrength = 3.0;

    /**
     * Minimum safe passing distance above the body's surface during a flyby.
     * The actual periapsis used in arc geometry is: radius + flybySafetyMargin.
     */
    @Getter @Setter
    private double flybySafetyMargin = 8.0;

    public Satellite(final CelestialBody focalPoint,
                     final BigDecimal radius,
                     final BigDecimal orbitalRadius,
                     final BigDecimal orbitalSpeed) {
        this(focalPoint, radius, orbitalRadius, orbitalRadius.add(BigDecimal.ONE), orbitalSpeed, BigDecimal.ZERO, randomAngle());
    }

    public Satellite(final CelestialBody focalPoint,
                     final BigDecimal radius,
                     final BigDecimal orbitalRadius,
                     final BigDecimal influenceRadius,
                     final BigDecimal orbitalSpeed,
                     final BigDecimal rotationSpeed,
                     final double angle) {
        super(initializeCoordinates(focalPoint, orbitalRadius, angle), radius, rotationSpeed);
        this.focalPoint = focalPoint;
        this.orbitalRadius = orbitalRadius;
        this.influenceRadius = influenceRadius;
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
        final BigDecimal xCoord = xOffset.add(focalPoint.getCoordinates().x());
        final BigDecimal yCoord = yOffset.add(focalPoint.getCoordinates().y());
        return new Coordinates(xCoord, yCoord);
    }
}
