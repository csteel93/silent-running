package com.steel.silent.entity;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Coordinates {

    private final AtomicReference<BigDecimal> x;
    private final AtomicReference<BigDecimal> y;
    private final AtomicReference<BigDecimal> o;

    public Coordinates(final BigDecimal x, final BigDecimal y) {
        this.x = new AtomicReference<>(x);
        this.y = new AtomicReference<>(y);
        this.o = new AtomicReference<>(BigDecimal.ZERO);
    }

    public void update(final BigDecimal newX, final BigDecimal newY) {
        setX(newX);
        setY(newY);
    }

    public void setX(final BigDecimal newX) {
        this.x.set(newX);
    }

    public void setY(final BigDecimal newY) {
        this.y.set(newY);
    }

    public void setO(final BigDecimal newO){
        this.o.set(newO);
    }

    public BigDecimal x() {
        return x.get();
    }

    public BigDecimal y() {
        return y.get();
    }

    public BigDecimal o(){
        return o.get();
    }
}
