package com.steel.silent.navigation;

import java.util.List;

/**
 * Result of validating a planned route against all moving bodies.
 *
 * <p>Blocking events are sorted chronologically (earliest encounter first),
 * so the planner can extend the route by addressing them in order.</p>
 *
 * <p>A route is {@link #isClear} only when no blocking events of any kind
 * were detected along the entire trajectory.</p>
 */
public record RouteValidation(
        boolean isClear,
        boolean hasCollision,
        boolean hasGravityWellEncounters,
        List<BlockingEvent> blockingEvents) {

    /** Convenience factory for a route that passed all checks. */
    public static RouteValidation clear() {
        return new RouteValidation(true, false, false, List.of());
    }
}
