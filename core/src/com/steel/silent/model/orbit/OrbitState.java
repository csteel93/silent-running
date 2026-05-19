package com.steel.silent.model.orbit;

import com.steel.silent.math.Vector;

public record OrbitState(
        Vector positionMeters,
        Vector velocityMetersPerSecond,
        double angleRadians) {

    public double angularVelocityRadPerSecond() {
        return angularVelocityRadPerSecondRelativeTo(OrbitState.stationary(Vector.ZERO()));
    }

    public double angularVelocityRadPerSecondRelativeTo(final OrbitState originState) {
        final Vector relativePosition = positionMeters.sub(originState.positionMeters());
        final Vector relativeVelocity = velocityMetersPerSecond.sub(originState.velocityMetersPerSecond());
        final double radiusSquared = relativePosition.len2();
        if (radiusSquared == 0.0) {
            return 0.0;
        }
        return relativePosition.cross(relativeVelocity) / radiusSquared;
    }

    public static OrbitState stationary(final Vector positionMeters) {
        return new OrbitState(
                positionMeters,
                Vector.ZERO(),
                0.0);
    }
}
