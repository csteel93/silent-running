package com.steel.silent.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@EqualsAndHashCode(exclude = "coordinates")
@RequiredArgsConstructor
public class CelestialBody implements IdentifiableBody {

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    // body radius meters
    @Getter
    private final double radius;
    // meters cubed per seconds squared
    @Getter
    private final double mu;
    // full rotation in seconds
    @Getter
    private final double rotationalSpeed;

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
    public double x() {
        return coordinates.x();
    }

    @Override
    public double y() {
        return coordinates.y();
    }

    @Override
    public double aspect() {
        return coordinates.o();
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public String getColor() {
        return characteristics.getColor();
    }
}
