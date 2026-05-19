package com.steel.silent.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.model.orbit.OrbitState;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.simulation.snapshot.SnapshotPublisher;

public class Universe {

    private final List<SolarSystem> solarSystems = new ArrayList<>();
    private final SnapshotPublisher snapshotPublisher = new SnapshotPublisher();

    private final long epochMillis;
    private final AtomicLong elapsedSimMillis = new AtomicLong(0);

    public Universe() {
        this(0L);
    }

    public Universe(final long epochMillis) {
        this.epochMillis = epochMillis;
    }

    public long update(final long previousTime, final int speed) {
        final long updatedMillis = System.currentTimeMillis();
        final long realDeltaMillis = updatedMillis - previousTime;
        final long simDeltaMillis = Math.round(realDeltaMillis * speed);
        elapsedSimMillis.addAndGet(simDeltaMillis);
        publishSnapshot();
        return updatedMillis;
    }

    public Optional<SimulationSnapshot> latestSnapshot() {
        return snapshotPublisher.latest();
    }

    public void publishSnapshot() {
        snapshotPublisher.publish(buildSnapshot());
    }

    public SimulationSnapshot buildSnapshot() {
        final long elapsedMillis = elapsedSimMillis.get();
        final double simTimeSeconds = elapsedMillis / 1000.0;

        final List<BodyState> states = solarSystems.stream()
                .flatMap(SolarSystem::bodies)
                .map(body -> toBodyState(body, simTimeSeconds))
                .toList();

        return new SimulationSnapshot(epochMillis, elapsedMillis, List.copyOf(states));
    }

    private BodyState toBodyState(final CelestialBody body, final double simTimeSeconds) {
        final OrbitState orbitState = orbitStateFor(body, simTimeSeconds);

        return new BodyState(
                body.id(),
                body.name(),
                body.classification(),
                orbitState.positionMeters(),
                orbitState.velocityMetersPerSecond(),
                body.orientationAt(simTimeSeconds),
                body.radiusMeters(),
                body.color());
    }

    private OrbitState orbitStateFor(final CelestialBody body, final double simTimeSeconds) {
        if (body instanceof Satellite satellite) {
            return satellite.orbit().stateAt(simTimeSeconds);
        }
        return OrbitState.stationary(body.initialPositionMeters());
    }

    public void withSolarSystem(final SolarSystem solarSystem) {
        solarSystems.add(solarSystem);
    }
}
