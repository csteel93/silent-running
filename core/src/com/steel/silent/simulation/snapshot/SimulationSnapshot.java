package com.steel.silent.simulation.snapshot;

import java.util.List;

public record SimulationSnapshot(
    long epochMillis,
    long elapsedSimMillis,
    List<BodyState> bodies
) {
    public long absoluteSimMillis() {
        return epochMillis + elapsedSimMillis;
    }
}