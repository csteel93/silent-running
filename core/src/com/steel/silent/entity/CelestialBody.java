package com.steel.silent.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(exclude = "coordinates")
@RequiredArgsConstructor
public class CelestialBody implements IdentifiableBody {

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    private final BigDecimal radius;
    @Getter
    private final BigDecimal rotationalSpeed;
    @Getter
    protected Characteristics characteristics = new Characteristics();

    @Override
    public String name() {
        return characteristics.getName();
    }

    @Override
    public String classification() {
        return characteristics.getClassification();
    }

    @Override
    public BigDecimal x() {
        return coordinates.x();
    }

    @Override
    public BigDecimal y() {
        return coordinates.y();
    }

    @Override
    public BigDecimal aspect() {
        return coordinates.o();
    }

    @Override
    public BigDecimal radius() {
        return radius;
    }

    @Override
    public String getColor() {
        return characteristics.getColor();
    }
}
