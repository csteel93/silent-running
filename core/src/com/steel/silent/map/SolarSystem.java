package com.steel.silent.map;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SolarSystem {

    private final FocalPoint focalPoint;
    private final ConcurrentHashMap<CelestialBody, CopyOnWriteArrayList<Satellite>> satellites = new ConcurrentHashMap<>();

    public SolarSystem(final FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
        this.satellites.put(focalPoint, new CopyOnWriteArrayList<>());
    }

    public void withSatellites(final List<Satellite> satellites){
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

    public void render(final ShapeRenderer renderer) {
        focalPoint.render(renderer);
        satellites.keySet().forEach(satellite -> satellite.render(renderer));
    }

    public void update(final long deltaTime) {
        Queue<Satellite> orderedSatellites = new LinkedList<>(satellites.get(focalPoint));

        while (!orderedSatellites.isEmpty()) {
            Satellite satellite = orderedSatellites.poll();
            orderedSatellites.addAll(satellites.get(satellite));

            orbit(satellite, deltaTime);

        }
    }

    private void orbit(final Satellite satellite,
                       final long delta) {

        double updated_x_offset = satellite.getOrbitalRadius().doubleValue() * Math.cos(satellite.getRelativeAngle().get().doubleValue());
        double updated_y_offset = satellite.getOrbitalRadius().doubleValue() * Math.sin(satellite.getRelativeAngle().get().doubleValue());

        double updated_x = updated_x_offset + satellite.getFocalPoint().getXCoord().get().doubleValue();
        double updated_y = updated_y_offset + satellite.getFocalPoint().getYCoord().get().doubleValue();

        // distance of one complete orbit
        double orbitDistance = orbitDistance(satellite.getOrbitalRadius().floatValue());

        // length in time of full orbit
        long orbitTimeMillis = Duration.ofSeconds(satellite.getOrbitalSpeed().longValue()).toMillis();

        // speed travelled
        double angularVelocity = orbitDistance / orbitTimeMillis;

        // distance travelled in radians, angle covered
        double radianDelta = angularVelocity * delta / 100;

        double sin = Math.sin(radianDelta);
        double cos = Math.cos(radianDelta);

        double diff_x = updated_x - satellite.getFocalPoint().getXCoord().get().doubleValue();
        double diff_y = updated_y - satellite.getFocalPoint().getYCoord().get().doubleValue();

        double x_change = (diff_x * cos) - (diff_y * sin);
        double y_change = (diff_x * sin) + (diff_y * cos);

        double new_x = x_change + satellite.getFocalPoint().getXCoord().get().doubleValue();
        double new_y = y_change + satellite.getFocalPoint().getYCoord().get().doubleValue();

        satellite.getXCoord().set(new BigDecimal(new_x));
        satellite.getYCoord().set(new BigDecimal(new_y));
        satellite.getRelativeAngle().getAndAccumulate(BigDecimal.valueOf(radianDelta), BigDecimal::add);
    }

    private float orbitDistance(final float orbitRadius) {
        return (float) (2 * Math.PI * orbitRadius);
    }

}
