package com.steel.silent.entity;

import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class FocalPoint extends CelestialBody{

    public FocalPoint(final BigDecimal radius, final BigDecimal xCoord, final BigDecimal yCoord){
        this.radius = radius;
        this.xCoord = new AtomicReference<>(xCoord);
        this.yCoord = new AtomicReference<>(yCoord);
    }

}
