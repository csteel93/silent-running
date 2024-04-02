package com.steel.silent.simulation;

import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.map.SolarSystem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
public class Universe {

    private final List<SolarSystem> solarSystems = new ArrayList<>();

    public long update(final long previousTime, final int speed) {
        final long updateTime = System.currentTimeMillis();
        final long deltaTime = updateTime - previousTime;

        solarSystems.forEach(solarSystem -> solarSystem.update(deltaTime, speed));

        return updateTime;
    }

    public Stream<IdentifiableBody> getState() {
        return solarSystems.stream().flatMap(SolarSystem::getStateOfSolarSystem);
    }
}
