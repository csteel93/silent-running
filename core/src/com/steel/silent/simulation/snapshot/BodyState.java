package com.steel.silent.simulation.snapshot;

import java.util.UUID;

import com.steel.silent.math.Vector;

public record BodyState(
        UUID bodyId,
        String name,
        String classification,
        Vector positionMeters,
        Vector velocityMetersPerSecond,
        double orientationRad,
        double radiusMeters,
        String color) {
}
