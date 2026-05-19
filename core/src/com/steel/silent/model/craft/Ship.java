package com.steel.silent.model.craft;

import java.util.UUID;

import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Characteristics;
import com.steel.silent.model.orbit.CircularOrbit;
import com.steel.silent.model.orbit.Orbit;
import com.steel.silent.model.orbit.OrbitState;

/**
 * A spacecraft whose simulation state is derived from an orbit, then published
 * through Universe snapshots for rendering.
 */
public class Ship {

    private final UUID id = UUID.randomUUID();
    private final Characteristics characteristics = new Characteristics();
    private final Orbit orbit;
    private final double radiusMeters;

    public Ship(final CelestialBody parent,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double orbitalPeriodSeconds) {
        this(parent, radiusMeters, orbitalRadiusMeters, orbitalPeriodSeconds, Math.random() * Math.PI * 2.0);
    }

    public Ship(final CelestialBody parent,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double orbitalPeriodSeconds,
            final double initialAngleRad) {
        this(new CircularOrbit(parent, orbitalRadiusMeters, orbitalPeriodSeconds, initialAngleRad), radiusMeters);
    }

    public Ship(final Orbit orbit, final double radiusMeters) {
        this.orbit = orbit;
        this.radiusMeters = radiusMeters;
        characteristics.setClassification("SHIP");
        characteristics.setName("Ship-" + id.toString().substring(0, 4));
        characteristics.setColor("ffffffff");
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return characteristics.getName();
    }

    public String classification() {
        return characteristics.getClassification();
    }

    public String color() {
        return characteristics.getColor();
    }

    public Characteristics characteristics() {
        return characteristics;
    }

    public Orbit orbit() {
        return orbit;
    }

    public CelestialBody primary() {
        return orbit.primary();
    }

    public double radiusMeters() {
        return radiusMeters;
    }

    public OrbitState stateAt(final double simTimeSeconds) {
        return orbit.stateAt(simTimeSeconds);
    }

    public double orientationAt(final double simTimeSeconds) {
        final OrbitState state = stateAt(simTimeSeconds);
        return Math.atan2(state.velocityMetersPerSecond().y(), state.velocityMetersPerSecond().x());
    }
}
