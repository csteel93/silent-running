package com.steel.silent.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CelestialBody {

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    protected AtomicReference<BigDecimal> xCoord;
    @Getter
    protected AtomicReference<BigDecimal> yCoord;

    protected BigDecimal radius;

    @Getter
    @Setter
    protected Characteristics characteristics = new Characteristics();

    public void render(final ShapeRenderer renderer) {
        renderer.setColor(Color.valueOf(characteristics.getColor()));
        renderer.circle(xCoord.get().floatValue(), yCoord.get().floatValue(), radius.floatValue(), 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CelestialBody that = (CelestialBody) o;
        return id.equals(that.id) && radius.equals(that.radius) && characteristics.equals(that.characteristics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, radius, characteristics);
    }

    @Data
    @EqualsAndHashCode
    public static class Characteristics {

        private String classification;
        private String name;
        private String color;

    }
}
