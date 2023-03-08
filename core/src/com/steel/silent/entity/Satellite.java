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
    private AtomicReference<BigDecimal> relativeAngle;

    public Satellite(final CelestialBody focalPoint, final BigDecimal radius, final BigDecimal orbitalRadius, final BigDecimal orbitalSpeed) {
        this.focalPoint = focalPoint;
        this.orbitalRadius = orbitalRadius;
        this.orbitalSpeed = orbitalSpeed;
        this.radius = radius;
        initializeCoordinates(focalPoint, orbitalRadius);
    }

    private void initializeCoordinates(final CelestialBody focalPoint, final BigDecimal orbitalRadius) {
        final double randomAngle = Math.random() * Math.PI * 2;
        final BigDecimal xOffset = orbitalRadius.multiply(BigDecimal.valueOf(Math.cos(randomAngle)));
        final BigDecimal yOffset = orbitalRadius.multiply(BigDecimal.valueOf(Math.sin(randomAngle)));
        this.xCoord = new AtomicReference<>(xOffset.add(focalPoint.getXCoord().get()));
        this.yCoord = new AtomicReference<>(yOffset.add(focalPoint.getYCoord().get()));
        this.relativeAngle = new AtomicReference<>(BigDecimal.valueOf(randomAngle));
    }

}
