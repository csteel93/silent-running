package com.steel.silent.entity;

import java.math.BigDecimal;

public class FocalPoint extends CelestialBody {

    public FocalPoint(final BigDecimal radius, final BigDecimal xCoord, final BigDecimal yCoord) {
        super(new Coordinates(xCoord, yCoord), radius);
    }

}
