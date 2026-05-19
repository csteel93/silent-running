package com.steel.silent.simulation.monitor;

import com.steel.silent.model.body.Satellite;
import com.steel.silent.simulation.SolarSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Logs to stdout each time a watched satellite completes a full orbit.
 * Watching is selective: only bodies registered via {@link #watch} are tracked.
 */
public class OrbitCompletionLogger {

    private final Map<UUID, Long> completedOrbitsByBodyId = new HashMap<>();

    /**
     * Returns true if this satellite should be logged. Override or configure
     * if you want different selection criteria.
     */
    protected boolean shouldWatch(final Satellite satellite) {
        return ("Moon".equals(satellite.name()) && "Earth".equals(satellite.primary().name()))
                || ("Earth".equals(satellite.name()) && "STAR".equals(satellite.primary().classification()));
    }

    /** Call once per simulation tick with the current simulation time in seconds. */
    public void tick(final Stream<SolarSystem> solarSystems, final double simTimeSeconds) {
        solarSystems
                .flatMap(SolarSystem::bodies)
                .filter(body -> body instanceof Satellite)
                .map(body -> (Satellite) body)
                .filter(this::shouldWatch)
                .forEach(satellite -> logIfCompleted(satellite, simTimeSeconds));
    }

    private void logIfCompleted(final Satellite satellite, final double simTimeSeconds) {
        final double angleRadians = satellite.orbit().stateAt(simTimeSeconds).angleRadians();
        final long completedCount = (long) Math.floor(angleRadians / (Math.PI * 2.0));
        final long previousCount = completedOrbitsByBodyId.computeIfAbsent(
                satellite.id(), ignored -> completedCount);

        for (long n = previousCount + 1; n <= completedCount; n++) {
            System.out.printf(
                    "%s completed orbit #%d around %s at %.2f simulated days%n",
                    satellite.name(),
                    n,
                    satellite.primary().name(),
                    simTimeSeconds / 86_400.0);
        }

        completedOrbitsByBodyId.put(satellite.id(), completedCount);
    }
}
