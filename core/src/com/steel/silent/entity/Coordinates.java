package com.steel.silent.entity;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Coordinates {

    private AtomicReference<BigDecimal> xCoord;
    private AtomicReference<BigDecimal> yCoord;

    public Coordinates(final BigDecimal xCoord, final BigDecimal yCoord){
        this.xCoord.set(xCoord);
        this.yCoord.set(yCoord);
    }

    public void setXCoord(final BigDecimal newXCoord){
        this.xCoord.set(newXCoord);
    }

    public void setYCoord(final BigDecimal newYCoord){
        this.yCoord.set(newYCoord);
    }

    public BigDecimal getXCoord(){
        return xCoord.get();
    }

    public BigDecimal getYCoord(){
        return yCoord.get();
    }
}
