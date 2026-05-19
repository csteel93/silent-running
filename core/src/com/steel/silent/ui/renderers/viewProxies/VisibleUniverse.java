package com.steel.silent.ui.renderers.viewProxies;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.steel.silent.simulation.Universe;

public class VisibleUniverse {

    private final Universe universe;
    private final List<VisibleSolarSystem> solarSystems;
    private final List<VisibleObject> visibleObjects;
    private final double timeScale;

    public VisibleUniverse(final Universe universe,
            final List<VisibleSolarSystem> solarSystems,
            final double timeScale) {
        this.universe = universe;
        this.solarSystems = solarSystems;
        this.visibleObjects = solarSystems.stream()
                .flatMap(VisibleSolarSystem::getStateOfSolarSystem)
                .collect(Collectors.toList());
        this.timeScale = timeScale;
    }

    public long getSimTime(){
        return universe.getSimTime();
    }

   public Stream<VisibleObject> getState() {
        return visibleObjects.stream();
    }

    public static VisibleUniverse fromUniverse(final Universe universe, final double mapWidth,
            final double mapHeight,
            final double bodyScale, final double timeScale) {
        final MapScale mapScale = MapScale.fromUniverse(universe, mapWidth, mapHeight, bodyScale);

        return new VisibleUniverse(universe, getVisibleSolarSystems(universe, mapScale), timeScale);
    }

    private static List<VisibleSolarSystem> getVisibleSolarSystems(final Universe universe, final MapScale mapScale) {
        return universe.getSolarSystems()
                .stream()
                .map(solarSystem -> VisibleSolarSystem.fromSolarSystem(solarSystem, mapScale))
                .collect(Collectors.toList());
    }

}
