package com.steel.silent.entity;

import com.steel.silent.navigation.Trajectory;
import com.steel.silent.simulation.OrbitalMechanics;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A controllable spacecraft.
 *
 * Ships have two life-cycle states:
 * <ul>
 *   <li>{@link State#ORBITING}: bound to a parent body, behaving like a
 *       {@link Satellite} (parent, radius, angle, period).</li>
 *   <li>{@link State#TRANSITING}: following a {@link Trajectory} between
 *       bodies. Position is interpolated along the current leg.</li>
 * </ul>
 *
 * The cruise speed is expressed in world units per millisecond of
 * <em>simulation</em> time (the same time base used to advance bodies), so
 * speed-multiplier changes naturally affect ETAs.
 */
public class Ship implements IdentifiableBody {

    public enum State { ORBITING, TRANSITING }

    @Getter
    private final UUID id = UUID.randomUUID();
    @Getter
    private final Coordinates coordinates;
    private final BigDecimal radius;
    @Getter
    private final Characteristics characteristics = new Characteristics();

    /** World units per ms of simulation time. */
    @Getter
    private final double cruiseSpeed;

    private final AtomicReference<State> state = new AtomicReference<>(State.ORBITING);

    // ORBITING state
    @Getter
    private volatile CelestialBody parentBody;
    @Getter
    private volatile BigDecimal orbitalRadius;
    @Getter
    private volatile BigDecimal orbitalPeriodSeconds; // semantically matches Satellite.orbitalSpeed
    private final AtomicReference<BigDecimal> relativeAngle = new AtomicReference<>(BigDecimal.ZERO);

    // TRANSITING state
    private final AtomicReference<Trajectory> trajectory = new AtomicReference<>(null);

    public Ship(final CelestialBody parent,
                final BigDecimal radius,
                final BigDecimal orbitalRadius,
                final BigDecimal orbitalPeriodSeconds,
                final double cruiseSpeed) {
        this(parent, radius, orbitalRadius, orbitalPeriodSeconds, cruiseSpeed, Math.random() * Math.PI * 2);
    }

    public Ship(final CelestialBody parent,
                final BigDecimal radius,
                final BigDecimal orbitalRadius,
                final BigDecimal orbitalPeriodSeconds,
                final double cruiseSpeed,
                final double initialAngle) {
        this.radius = radius;
        this.cruiseSpeed = cruiseSpeed;
        this.parentBody = parent;
        this.orbitalRadius = orbitalRadius;
        this.orbitalPeriodSeconds = orbitalPeriodSeconds;
        this.relativeAngle.set(BigDecimal.valueOf(initialAngle));
        final double x = parent.getCoordinates().x().doubleValue()
            + orbitalRadius.doubleValue() * Math.cos(initialAngle);
        final double y = parent.getCoordinates().y().doubleValue()
            + orbitalRadius.doubleValue() * Math.sin(initialAngle);
        this.coordinates = new Coordinates(BigDecimal.valueOf(x), BigDecimal.valueOf(y));
        this.characteristics.setClassification("SHIP");
        this.characteristics.setName("Ship-" + id.toString().substring(0, 4));
        this.characteristics.setColor("ffffffff");
    }

    public State getState() {
        return state.get();
    }

    public Trajectory getTrajectory() {
        return trajectory.get();
    }

    /**
     * Assign a new trajectory. The ship transitions to TRANSITING.
     */
    public void commandTrajectory(final Trajectory t) {
        this.trajectory.set(t);
        this.state.set(State.TRANSITING);
    }

    /**
     * Drop into orbit around the given body at the ship's current position.
     */
    public void enterOrbitAt(final CelestialBody body) {
        final double cx = body.getCoordinates().x().doubleValue();
        final double cy = body.getCoordinates().y().doubleValue();
        final double dx = coordinates.x().doubleValue() - cx;
        final double dy = coordinates.y().doubleValue() - cy;
        final double r = Math.sqrt(dx * dx + dy * dy);
        final double angle = Math.atan2(dy, dx);

        this.parentBody = body;
        this.orbitalRadius = BigDecimal.valueOf(Math.max(r, body.radius().doubleValue() * 1.5));
        // Default to a comfortable period proportional to radius if we don't have a destination override.
        if (this.orbitalPeriodSeconds == null) {
            this.orbitalPeriodSeconds = BigDecimal.valueOf((long) (60 + r));
        }
        this.relativeAngle.set(BigDecimal.valueOf(angle));
        this.trajectory.set(null);
        this.state.set(State.ORBITING);
    }

    /**
     * Advance the ship one simulation tick.
     *
     * @param simDelta simulation-time elapsed this tick (ms)
     * @param simTime  current absolute simulation time (ms)
     */
    public void update(final long simDelta, final long simTime) {
        switch (state.get()) {
            case ORBITING -> updateOrbit(simDelta);
            case TRANSITING -> updateTransit(simDelta, simTime);
        }
    }

    private void updateOrbit(final long simDelta) {
        if (parentBody == null || orbitalRadius == null || orbitalPeriodSeconds == null) {
            return;
        }
        final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriodSeconds.longValue()).toMillis();
        if (orbitTimeMillis <= 0) return;
        final double angularVelocity = (2.0 * Math.PI) / orbitTimeMillis;
        final double radianDelta = angularVelocity * simDelta;

        final double newAngle = relativeAngle.get().doubleValue() + radianDelta;
        relativeAngle.set(BigDecimal.valueOf(newAngle));

        final double cx = parentBody.getCoordinates().x().doubleValue();
        final double cy = parentBody.getCoordinates().y().doubleValue();
        final double r = orbitalRadius.doubleValue();
        coordinates.update(
            BigDecimal.valueOf(cx + r * Math.cos(newAngle)),
            BigDecimal.valueOf(cy + r * Math.sin(newAngle)));
    }

    private void updateTransit(final long simDelta, final long simTime) {
        final Trajectory t = trajectory.get();
        if (t == null) {
            state.set(State.ORBITING);
            return;
        }
        if (simTime < t.departureSimTime()) {
            updateOrbit(simDelta);
            return;
        }
        final Trajectory.Sample s = t.sampleAt(simTime);
        coordinates.update(BigDecimal.valueOf(s.x), BigDecimal.valueOf(s.y));
        if (s.arrived) {
            // Trajectory reports the destination body; orbit around it at the
            // arrival radius using a default short period.
            if (s.arrivalBody != null) {
                this.orbitalPeriodSeconds = BigDecimal.valueOf(Math.max(30L,
                    (long) (s.arrivalBody.radius().doubleValue() * 4.0)));
                enterOrbitAt(s.arrivalBody);
            } else {
                state.set(State.ORBITING);
                trajectory.set(null);
            }
        }
    }

    /**
     * Where will this ship be at a future simulation time? Used by the planner
     * when chaining legs (a leg's departure point is the previous leg's
     * arrival point, but we still call into this for symmetry with bodies).
     */
    public OrbitalMechanics.Vec2 predictPosition(final long currentSim, final long targetSim) {
        if (state.get() == State.TRANSITING) {
            final Trajectory t = trajectory.get();
            if (t != null) {
                if (targetSim < t.departureSimTime()) {
                    return predictOrbitPosition(currentSim, targetSim);
                }
                final Trajectory.Sample s = t.sampleAt(targetSim);
                return new OrbitalMechanics.Vec2(s.x, s.y);
            }
        }
        // Orbiting (or no trajectory): predict like a satellite around parent.
        return predictOrbitPosition(currentSim, targetSim);
    }

    public OrbitalMechanics.Vec2 predictOrbitPosition(final long currentSim, final long targetSim) {
        if (parentBody != null && orbitalRadius != null && orbitalPeriodSeconds != null) {
            final OrbitalMechanics.Vec2 parentAt = OrbitalMechanics.predict(parentBody, currentSim, targetSim);
            final long orbitTimeMillis = Duration.ofSeconds(orbitalPeriodSeconds.longValue()).toMillis();
            final double omega = orbitTimeMillis > 0 ? (2.0 * Math.PI) / orbitTimeMillis : 0;
            final double angle = relativeAngle.get().doubleValue() + omega * (targetSim - currentSim);
            final double r = orbitalRadius.doubleValue();
            return new OrbitalMechanics.Vec2(parentAt.x + r * Math.cos(angle), parentAt.y + r * Math.sin(angle));
        }
        return new OrbitalMechanics.Vec2(coordinates.x().doubleValue(), coordinates.y().doubleValue());
    }

    @Override public String name() { return characteristics.getName(); }
    @Override public String classification() { return characteristics.getClassification(); }
    @Override public BigDecimal x() { return coordinates.x(); }
    @Override public BigDecimal y() { return coordinates.y(); }
    @Override public BigDecimal aspect() { return coordinates.o(); }
    @Override public BigDecimal radius() { return radius; }
    @Override public String getColor() { return characteristics.getColor(); }
}
