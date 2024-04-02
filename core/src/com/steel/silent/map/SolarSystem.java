package com.steel.silent.map;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.entity.Satellite;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolarSystem {

    private final FocalPoint focalPoint;
    private final ConcurrentHashMap<CelestialBody, CopyOnWriteArrayList<Satellite>> satellites = new ConcurrentHashMap<>();

    public SolarSystem(final FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
        this.satellites.put(focalPoint, new CopyOnWriteArrayList<>());
    }

    public void withSatellites(final List<Satellite> satellites) {
        satellites.forEach(this::withSatellite);
    }

    public void withSatellite(final Satellite satellite) {
        insertSatellite(satellite);
        appendFocalPoint(satellite);
    }

    private void insertSatellite(final Satellite satellite) {
        final List<Satellite> children = satellites.keySet().stream()
            .filter(celestialBody -> celestialBody instanceof Satellite)
            .map(celestialBody -> (Satellite) celestialBody)
            .filter(celestialBody -> celestialBody.getFocalPoint().getId().equals(satellite.getId()))
            .collect(Collectors.toList());
        satellites.put(satellite, new CopyOnWriteArrayList<>(children));
    }

    private void appendFocalPoint(final Satellite satellite) {
        Optional.ofNullable(satellites.get(satellite.getFocalPoint()))
            .ifPresent(children -> {
                if (!children.contains(satellite)) {
                    children.add(satellite);
                }
            });
    }

    public Stream<IdentifiableBody> getStateOfSolarSystem() {
        final Queue<IdentifiableBody> satelliteStream = new ArrayDeque<>();
        boolean traversing = true;
        List<CelestialBody> temp = Collections.singletonList(focalPoint);
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

    public void update(final long deltaTime, final int speed) {
        final Queue<Satellite> orderedSatellites = new LinkedList<>(satellites.get(focalPoint));
        spin(focalPoint, deltaTime, speed);
        while (!orderedSatellites.isEmpty()) {
            final Satellite satellite = orderedSatellites.poll();
            orderedSatellites.addAll(satellites.get(satellite));
            orbit(satellite, deltaTime, speed);
        }
    }

    private void orbit(final Satellite satellite, final long delta, final int speed) {

        final double updated_x_offset = satellite.getOrbitalRadius().doubleValue() * Math.cos(satellite.getRelativeAngle().get().doubleValue());
        final double updated_y_offset = satellite.getOrbitalRadius().doubleValue() * Math.sin(satellite.getRelativeAngle().get().doubleValue());

        final double updated_x = updated_x_offset + satellite.getFocalPoint().getCoordinates().x().doubleValue();
        final double updated_y = updated_y_offset + satellite.getFocalPoint().getCoordinates().y().doubleValue();

        // distance of one complete orbit
        final double orbitDistance = orbitDistance(satellite.getOrbitalRadius().floatValue());

        // length in time of full orbit
        final long orbitTimeMillis = Duration.ofSeconds(satellite.getOrbitalSpeed().longValue()).toMillis();

        // speed travelled
        final double angularVelocity = orbitDistance / orbitTimeMillis;

        // distance travelled in radians, angle covered
        final double radianDelta = angularVelocity * delta * speed;

        final double sin = Math.sin(radianDelta);
        final double cos = Math.cos(radianDelta);

        final double diff_x = updated_x - satellite.getFocalPoint().getCoordinates().x().doubleValue();
        final double diff_y = updated_y - satellite.getFocalPoint().getCoordinates().y().doubleValue();

        final double x_change = (diff_x * cos) - (diff_y * sin);
        final double y_change = (diff_x * sin) + (diff_y * cos);

        final double new_x = x_change + satellite.getFocalPoint().getCoordinates().x().doubleValue();
        final double new_y = y_change + satellite.getFocalPoint().getCoordinates().y().doubleValue();

        satellite.getCoordinates().update(new BigDecimal(new_x), new BigDecimal(new_y));
        satellite.getRelativeAngle().getAndAccumulate(BigDecimal.valueOf(radianDelta), BigDecimal::add);

        spin(satellite, delta, speed);
    }

    private void spin(final CelestialBody celestialBody, final long delta, final int speed) {
        if (!celestialBody.getRotationalSpeed().equals(BigDecimal.ZERO)) {
            final double circumference = 2 * Math.PI * celestialBody.radius().doubleValue();
            final long rotationTimeMillis = celestialBody.getRotationalSpeed().longValue();
            final double rotationVelocity = circumference / rotationTimeMillis;
            final double rotationDelta = rotationVelocity * delta * speed;
            final double newOrientation = rotationDelta + celestialBody.getCoordinates().o().doubleValue();
            celestialBody.getCoordinates().setO(BigDecimal.valueOf(newOrientation));
        }
    }

    private float orbitDistance(final float orbitRadius) {
        return (float) (2 * Math.PI * orbitRadius);
    }
}
