package com.steel.silent.entity;

import java.util.concurrent.atomic.AtomicReference;

public class Coordinates {

    private final AtomicReference<Double> x;
    private final AtomicReference<Double> y;
    private final AtomicReference<Double> o;

    public Coordinates(final double x, final double y) {
        this.x = new AtomicReference<>(x);
        this.y = new AtomicReference<>(y);
        this.o = new AtomicReference<>(0d);
    }

    public void update(final double newX, final double newY) {
        setX(newX);
        setY(newY);
    }

    public void setX(final double newX) {
        this.x.set(newX);
    }

    public void setY(final double newY) {
        this.y.set(newY);
    }

    public void setO(final double newO){
        this.o.set(newO);
    }

    public double x() {
        return x.get();
    }

    public double y() {
        return y.get();
    }

    public double o(){
        return o.get();
    }
}
