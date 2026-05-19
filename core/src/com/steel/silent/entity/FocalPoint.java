package com.steel.silent.entity;

public class FocalPoint extends CelestialBody {

    public FocalPoint(final double radius, final double xCoord, final double yCoord, final double mu, final double rotationSpeed) {
        super(new Coordinates(xCoord, yCoord), radius, mu, rotationSpeed);
    }
}
