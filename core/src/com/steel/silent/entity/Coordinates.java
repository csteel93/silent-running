package com.steel.silent.entity;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Coordinates {

    private final AtomicReference<BigDecimal> xCoord;
    private final AtomicReference<BigDecimal> yCoord;

    public Coordinates(final BigDecimal xCoord, final BigDecimal yCoord) {
        this.xCoord = new AtomicReference<>(xCoord);
        this.yCoord = new AtomicReference<>(yCoord);
    }

    public void update(final BigDecimal newXCoord, final BigDecimal newYCoord) {
        setXCoord(newXCoord);
        setYCoord(newYCoord);
    }

    public void setXCoord(final BigDecimal newXCoord) {
        this.xCoord.set(newXCoord);
    }

    public void setYCoord(final BigDecimal newYCoord) {
        this.yCoord.set(newYCoord);
    }

    public BigDecimal getXCoord() {
        return xCoord.get();
    }

    public BigDecimal getYCoord() {
        return yCoord.get();
    }
}
