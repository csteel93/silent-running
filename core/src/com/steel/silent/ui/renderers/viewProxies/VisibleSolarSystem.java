package com.steel.silent.ui.renderers.viewProxies;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.steel.silent.map.SolarSystem;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VisibleSolarSystem {

    private final SolarSystem solarSystem;
    private final VisibleBody focalPoint;
    private final ConcurrentHashMap<VisibleBody, CopyOnWriteArrayList<VisibleSatellite>> satellites;
    private final MapScale mapScale;

    public static VisibleSolarSystem fromSolarSystem(final SolarSystem solarSystem,
            final MapScale mapScale) {
        return new VisibleSolarSystem(solarSystem,
                VisibleBody.fromCelestialBody(solarSystem.getFocalPoint(), mapScale),
                getVisibleSatellites(solarSystem, mapScale), mapScale);
    }

    public Stream<VisibleObject> getStateOfSolarSystem() {
        final Queue<VisibleObject> satelliteStream = new ArrayDeque<>();
        boolean traversing = true;
        List<VisibleBody> temp = Collections.singletonList(focalPoint);
        while (traversing) {
            satelliteStream.addAll(temp);
            temp = temp.stream()
                    .flatMap(body -> satellites.get(body).stream())
                    .collect(Collectors.toList());
            if (temp.isEmpty()) {
                traversing = false;
            }
        }
        return satelliteStream.stream();
    }

    private static ConcurrentHashMap<VisibleBody, CopyOnWriteArrayList<VisibleSatellite>> getVisibleSatellites(
            final SolarSystem solarSystem, final MapScale mapScale) {
        return solarSystem.getSatellites()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> VisibleBody.fromCelestialBody(entry.getKey(), mapScale),
                        entry -> entry.getValue().stream()
                                .map(satellite -> VisibleSatellite.fromSatellite(satellite, mapScale))
                                .collect(Collectors.toCollection(CopyOnWriteArrayList::new)),
                        (a, b) -> a,
                        ConcurrentHashMap::new));
    }
}
