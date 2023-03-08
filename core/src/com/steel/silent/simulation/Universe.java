package com.steel.silent.simulation;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.steel.silent.map.SolarSystem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Universe {

    private final List<SolarSystem> solarSystems = new ArrayList<>();

    public long update(final long previousTime){
        final long updateTime = System.currentTimeMillis();
        final long deltaTime = updateTime - previousTime;

        solarSystems.forEach(solarSystem -> solarSystem.update(deltaTime));

        return updateTime;
    }

    public void render(final ShapeRenderer shapeRenderer) {
        solarSystems.forEach(solarSystem -> solarSystem.render(shapeRenderer));
    }

}
