package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.simulation.OrbitalMechanics.Vec2;

/**
 * A detected obstruction on a planned route.
 *
 * <p>The planner receives a list of these sorted by {@link #encounterTime} so
 * it can process blockers in the order the ship would actually encounter them.
 * The first unhandled gravity-well event is always the best candidate for a
 * gravity assist, because inserting an assist there leaves the largest arc to
 * the destination.</p>
 *
 * <p>{@link #encounterVelocity} is the normalized direction the blocking body
 * is moving at encounter time. It is used when computing relative-frame entry
 * velocity for the gravity-assist arc.</p>
 */
public record BlockingEvent(
        CelestialBody body,
        long encounterTime,
        Vec2 encounterPosition,
        Vec2 encounterVelocity,
        BlockType blockType,
        double distanceToBody
) implements Comparable<BlockingEvent> {

    @Override
    public int compareTo(final BlockingEvent other) {
        return Long.compare(this.encounterTime, other.encounterTime);
    }
}
