package com.steel.silent.simulation.snapshot;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SnapshotPublisher {

    private final AtomicReference<SimulationSnapshot> latest = new AtomicReference<>();

    public void publish(final SimulationSnapshot snapshot) {
        latest.set(snapshot);
    }

    public Optional<SimulationSnapshot> latest() {
        return Optional.ofNullable(latest.get());
    }

}
