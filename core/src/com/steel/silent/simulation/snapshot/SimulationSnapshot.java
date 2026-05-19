package com.steel.silent.simulation.snapshot;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public record SimulationSnapshot(
    long epochMillis,
    long elapsedSimMillis,
    List<BodyState> bodies
) {
    public long absoluteSimMillis() {
        return epochMillis + elapsedSimMillis;
    }

    public Map<UUID, BodyState> bodiesById() {
        return bodies.stream().collect(Collectors.toMap(BodyState::bodyId, Function.identity()));
    }
}
