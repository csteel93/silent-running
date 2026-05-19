package com.steel.silent.simulation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.FocalPoint;
import com.steel.silent.model.body.Satellite;

import lombok.Getter;

public class SolarSystem {

    @Getter
    private final FocalPoint primary;
    private final Map<CelestialBody, CopyOnWriteArrayList<Satellite>> satellitesByPrimary = new ConcurrentHashMap<>();

    public SolarSystem(final FocalPoint primary) {
        this.primary = primary;
        this.satellitesByPrimary.put(primary, new CopyOnWriteArrayList<>());
    }

    public Stream<CelestialBody> bodies() {
        final Queue<CelestialBody> queue = new ArrayDeque<>();
        final List<CelestialBody> ordered = new ArrayList<>();
        queue.add(primary);
        while (!queue.isEmpty()) {
            final CelestialBody body = queue.poll();
            ordered.add(body);
            queue.addAll(satellitesByPrimary.getOrDefault(body, new CopyOnWriteArrayList<>()));
        }
        return ordered.stream();
    }

    public void withSatellites(final List<Satellite> satellites) {
        satellites.forEach(this::withSatellite);
    }

    public void withSatellite(final Satellite satellite) {
        insertSatellite(satellite);
        appendFocalPoint(satellite);
    }

    private void insertSatellite(final Satellite satellite) {
        final List<Satellite> children = satellitesByPrimary.keySet().stream()
                .filter(celestialBody -> celestialBody instanceof Satellite)
                .map(celestialBody -> (Satellite) celestialBody)
                .filter(celestialBody -> celestialBody.primary().id().equals(satellite.id()))
                .collect(Collectors.toList());
        satellitesByPrimary.put(satellite, new CopyOnWriteArrayList<>(children));
    }

    private void appendFocalPoint(final Satellite satellite) {
        Optional.ofNullable(satellitesByPrimary.get(satellite.primary()))
                .ifPresent(children -> {
                    if (!children.contains(satellite)) {
                        children.add(satellite);
                    }
                });
    }

}
