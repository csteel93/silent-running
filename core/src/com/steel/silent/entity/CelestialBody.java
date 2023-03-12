package com.steel.silent.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(exclude = "coordinates")
@RequiredArgsConstructor
public class CelestialBody {

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    private final BigDecimal radius;
    @Getter
    @Setter
    protected Characteristics characteristics = new Characteristics();

    public void render(final ShapeRenderer renderer) {
        renderer.setColor(Color.valueOf(characteristics.getColor()));
        renderer.circle(coordinates.getXCoord().floatValue(), coordinates.getYCoord().floatValue(), radius.floatValue(), 100);
    }

    @Data
    @EqualsAndHashCode
    public static class Characteristics {

        private String classification;
        private String name;
        private String color;
    }
}
