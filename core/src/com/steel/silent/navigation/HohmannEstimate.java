package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;

/**
 * Result of a Hohmann transfer estimate between two co-planar circular orbits
 * sharing the same parent body.
 *
 * <p>All velocity values are in world-units per millisecond of simulation time.
 * Transfer time is in milliseconds of simulation time.</p>
 *
 * <p>For outbound transfers (r2 > r1): deltaV1 is positive (prograde burn at
 * departure), deltaV2 is positive (prograde insertion burn at arrival).</p>
 *
 * <p>For inbound transfers (r2 < r1): deltaV1 is negative (retrograde burn),
 * deltaV2 is negative (braking burn). The sign is preserved so the planner
 * can determine burn direction; the visual path is always smooth.</p>
 */
public record HohmannEstimate(
        /** Departure orbit radius around the shared parent (world units). */
        double r1,
        /** Arrival orbit radius around the shared parent (world units). */
        double r2,
        /** Semi-major axis of the transfer ellipse: (r1 + r2) / 2. */
        double semiMajorAxis,
        /** Time to traverse the transfer half-ellipse (milliseconds of sim time). */
        double transferTime,
        /** Delta-V of the departure burn (world-units / ms). Signed: positive = prograde. */
        double deltaV1,
        /** Delta-V of the arrival burn (world-units / ms). Signed: positive = prograde. */
        double deltaV2,
        /** True when the destination is farther from the parent than the source. */
        boolean isOutbound,
        /** The body whose gravity defines the transfer ellipse (the shared parent). */
        CelestialBody sourceParent,
        /** Predicted simulation time at arrival: startTime + transferTime. */
        long arrivalTime) {

    /** Total absolute delta-V budget for this transfer. */
    public double totalDeltaV() {
        return Math.abs(deltaV1) + Math.abs(deltaV2);
    }
}
