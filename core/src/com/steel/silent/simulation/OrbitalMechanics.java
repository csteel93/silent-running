package com.steel.silent.simulation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint; // retained for predict() dispatch
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;


/**
 * Deterministic position prediction.
 *
 * The simulation advances every body by a "simulation time" that equals
 * {@code wallDelta * speed} per tick (see {@link Universe#update}). Because
 * orbits are perfect circles with constant angular velocity, given any body's
 * current angle at simulation time {@code currentSim} we can compute its
 * position exactly at any future simulation time {@code targetSim}.
 *
 * The angular velocity used here mirrors the formula used in
 * {@link com.steel.silent.map.SolarSystem#orbit}: it is the value that, when
 * multiplied by elapsed simulation time, yields the rotation applied around
 * the focal point.
 */
public final class OrbitalMechanics {

    private OrbitalMechanics() {}

    /** Position of {@code body} at {@code targetSim} given the simulation is currently at {@code currentSim}. */
    public static Vec2 predict(final IdentifiableBody body, final long currentSim, final long targetSim) {
        if (body instanceof Satellite satellite) {
            return predictSatellite(satellite, currentSim, targetSim);
        }
        if (body instanceof Ship ship) {
            return predictShip(ship, currentSim, targetSim);
        }
        // FocalPoint and anything else: stationary in this model.
        return new Vec2(body.x().doubleValue(), body.y().doubleValue());
    }

    /** Convenience for a CelestialBody. */
    public static Vec2 predictBody(final CelestialBody body, final long currentSim, final long targetSim) {
        return predict(body, currentSim, targetSim);
    }

    private static Vec2 predictSatellite(final Satellite satellite, final long currentSim, final long targetSim) {
        final Vec2 focalAt = predict(satellite.getFocalPoint(), currentSim, targetSim);
        final double angle = futureAngle(satellite, currentSim, targetSim);
        final double r = satellite.getOrbitalRadius().doubleValue();
        return new Vec2(focalAt.x + r * Math.cos(angle), focalAt.y + r * Math.sin(angle));
    }

    private static Vec2 predictShip(final Ship ship, final long currentSim, final long targetSim) {
        return ship.predictPosition(currentSim, targetSim);
    }

    /** Angle of a satellite around its focal point at a future simulation time. */
    public static double futureAngle(final Satellite satellite, final long currentSim, final long targetSim) {
        final double now = satellite.getRelativeAngle().get().doubleValue();
        final double omega = angularVelocity(satellite);
        return now + omega * (targetSim - currentSim);
    }

    /** Radians per ms of simulation time, consistent with SolarSystem.orbit(). */
    public static double angularVelocity(final Satellite satellite) {
        return angularVelocity(satellite.getOrbitalRadius().doubleValue(), satellite.getOrbitalSpeed().longValue());
    }

    /** Same formula used by SolarSystem.orbit when computing radianDelta. */
    public static double angularVelocity(final double orbitalRadius, final long orbitalSpeed) {
        if (orbitalSpeed <= 0) {
            return 0.0;
        }
        return (2.0 * Math.PI) / orbitalSpeed;
    }

    /**
     * Derive a visual gravitational parameter (μ) from a known circular orbit.
     *
     * From Kepler's third law:  T² = (4π²/μ) · a³  →  μ = 4π²a³ / T²
     *
     * The result is expressed in (world-units)³/(ms-sim)² so it is consistent
     * with positions and simulation times already in use.
     *
     * @param orbitalRadius   orbital radius of the reference satellite (world units)
     * @param orbitalPeriodMs orbital period in milliseconds of simulation time
     */
    public static double muFromOrbit(final double orbitalRadius, final long orbitalPeriodMs) {
        if (orbitalPeriodMs <= 0) {
            return 1.0 / (28.0 * 28.0); // legacy fallback
        }
        final double omega = (2.0 * Math.PI) / orbitalPeriodMs;
        return omega * omega * orbitalRadius * orbitalRadius * orbitalRadius;
    }

    /**
     * Raw velocity vector of a body at a given simulation time.
     *
     * Returns world-units per millisecond of simulation time (not normalized).
     * Uses a small symmetric finite-difference around {@code simTime}.
     */
    public static Vec2 bodyVelocityRaw(final CelestialBody body, final long currentSim, final long simTime) {
        final long dt = 100L;
        final Vec2 before = predict(body, currentSim, simTime - dt / 2);
        final Vec2 after  = predict(body, currentSim, simTime + dt / 2);
        return new Vec2((after.x - before.x) / dt, (after.y - before.y) / dt);
    }

    /** Squared Euclidean distance between two points. */
    public static double distSq(final Vec2 a, final Vec2 b) {
        final double dx = a.x - b.x;
        final double dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    public static double dist(final Vec2 a, final Vec2 b) {
        return Math.sqrt(distSq(a, b));
    }

    /** Lightweight value pair for predicted positions. */
    public static final class Vec2 {
        public final double x;
        public final double y;
        public Vec2(final double x, final double y) {
            this.x = x;
            this.y = y;
        }
        @Override public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}
