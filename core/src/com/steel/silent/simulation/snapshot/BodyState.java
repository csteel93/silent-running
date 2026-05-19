package com.steel.silent.simulation.snapshot;

import java.util.UUID;

import com.steel.silent.math.Vector;

public record BodyState(
        UUID bodyId,
        UUID primaryBodyId,
        String name,
        String classification,
        Vector positionMeters,
        Vector velocityMetersPerSecond,
        double orientationRad,
        double radiusMeters,
        double influenceRadius,
        String color) {
}
