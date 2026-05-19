package com.steel.silent.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.model.craft.Ship;
import com.steel.silent.model.orbit.OrbitState;
import com.steel.silent.simulation.monitor.OrbitCompletionLogger;
import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.simulation.snapshot.SnapshotPublisher;

public class Universe {

    private final List<SolarSystem> solarSystems = new ArrayList<>();
    private final List<Ship> ships = new CopyOnWriteArrayList<>();
    private final SnapshotPublisher snapshotPublisher = new SnapshotPublisher();
    private final OrbitCompletionLogger orbitLogger = new OrbitCompletionLogger();

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
        final long elapsedMillis = elapsedSimMillis.addAndGet(simDeltaMillis);
        orbitLogger.tick(solarSystems.stream(), elapsedMillis / 1000.0);
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

        final List<BodyState> bodyStates = solarSystems.stream()
                .flatMap(SolarSystem::bodies)
                .map(body -> toBodyState(body, simTimeSeconds))
                .toList();

        final List<BodyState> shipStates = ships.stream()
                .map(ship -> toBodyState(ship, simTimeSeconds))
                .toList();

        final List<BodyState> states = new ArrayList<>(bodyStates.size() + shipStates.size());
        states.addAll(bodyStates);
        states.addAll(shipStates);

        return new SimulationSnapshot(epochMillis, elapsedMillis, List.copyOf(states));
    }

    private BodyState toBodyState(final CelestialBody body, final double simTimeSeconds) {
        final OrbitState orbitState = orbitStateFor(body, simTimeSeconds);
        return new BodyState(
                body.id(),
                primaryBodyId(body),
                body.name(),
                body.classification(),
                orbitState.positionMeters(),
                orbitState.velocityMetersPerSecond(),
                body.orientationAt(simTimeSeconds),
                body.radiusMeters(),
                body instanceof Satellite ? ((Satellite) body).influenceRadius() : body.radiusMeters(),
                body.color());
    }

    private BodyState toBodyState(final Ship ship, final double simTimeSeconds) {
        final OrbitState orbitState = ship.stateAt(simTimeSeconds);
        return new BodyState(
                ship.id(),
                ship.primary().id(),
                ship.name(),
                ship.classification(),
                orbitState.positionMeters(),
                orbitState.velocityMetersPerSecond(),
                ship.orientationAt(simTimeSeconds),
                ship.radiusMeters(),
                ship.radiusMeters(),
                ship.color());
    }

    private UUID primaryBodyId(final CelestialBody body) {
        if (body instanceof Satellite satellite) {
            return satellite.primary().id();
        }
        return null;
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

    public void withShip(final Ship ship) {
        ships.add(ship);
    }

    public List<Ship> ships() {
        return List.copyOf(ships);
    }
}
