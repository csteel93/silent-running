package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;

import java.util.Collection;
import java.util.Set;

/**
 * @deprecated Replaced by {@link RouteValidator}, which additionally detects
 *             gravity-well encounters and returns structured {@link BlockingEvent}
 *             objects sorted by encounter time.
 *
 *             <p>This class is kept only to avoid breaking any external callers.
 *             All internal navigation code now uses {@link RouteValidator} directly.</p>
 */
@Deprecated
public final class CollisionChecker {

    private CollisionChecker() {}

    /**
     * @deprecated Use {@link RouteValidator#isClear} instead.
     */
    @Deprecated
    public static boolean isClearOfBodies(final Trajectory trajectory,
                                          final Collection<? extends CelestialBody> bodies,
                                          final long currentSim,
                                          final double shipRadius) {
        return RouteValidator.isClear(trajectory, bodies, Set.of(), currentSim, shipRadius);
    }

    /**
     * @deprecated Use {@link RouteValidator#validate} to get structured blocking events.
     */
    @Deprecated
    public static CelestialBody firstCollision(final Trajectory trajectory,
                                               final Collection<? extends CelestialBody> bodies,
                                               final long currentSim,
                                               final double shipRadius) {
        final RouteValidation result = RouteValidator.validate(
                trajectory, bodies, Set.of(), currentSim, shipRadius);
        return result.blockingEvents().stream()
                .filter(e -> e.blockType() == BlockType.COLLISION)
                .map(BlockingEvent::body)
                .findFirst()
                .orElse(null);
    }
}
