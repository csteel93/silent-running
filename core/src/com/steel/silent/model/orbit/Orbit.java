package com.steel.silent.model.orbit;

import com.steel.silent.model.body.CelestialBody;

public interface Orbit {
    CelestialBody primary();
    OrbitState stateAt(double timeSeconds);
    double radiusMeters();
    double periodSeconds();

    default double meanMotionRadPerSecond() {
        return (Math.PI * 2.0) / periodSeconds();
    }
}
