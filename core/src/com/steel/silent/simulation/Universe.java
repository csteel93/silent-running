package com.steel.silent.simulation;

import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.entity.Ship;
import com.steel.silent.map.SolarSystem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Data
public class Universe {

    private final List<SolarSystem> solarSystems = new ArrayList<>();
    private final List<Ship> ships = new CopyOnWriteArrayList<>();
    private final AtomicLong simulationTime = new AtomicLong(0);

    public long update(final long previousTime, final int speed) {
        final long updateTime = System.currentTimeMillis();
        final long deltaTime = updateTime - previousTime;
        final long simDelta = deltaTime * speed;

        // Advance the global simulation clock so position prediction and ships use
        // the same time base as orbital motion.
        final long simTime = simulationTime.addAndGet(simDelta);

        solarSystems.forEach(solarSystem -> solarSystem.update(deltaTime, speed));
        ships.forEach(ship -> ship.update(simDelta, simTime));

        return updateTime;
    }

    public long getSimTime() {
        return simulationTime.get();
    }

    public Stream<IdentifiableBody> getState() {
        return Stream.concat(
            solarSystems.stream().flatMap(SolarSystem::getStateOfSolarSystem),
            ships.stream().map(s -> (IdentifiableBody) s));
    }
}
