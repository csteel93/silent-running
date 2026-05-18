package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.simulation.OrbitalMechanics;
import com.steel.silent.simulation.OrbitalMechanics.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates a planned route against all moving bodies over time.
 *
 * <p>Two levels of blocking are detected by sampling each leg at discrete
 * simulation times and predicting every body's world position at that time:</p>
 * <ul>
 *   <li>{@link BlockType#COLLISION} — the ship's path enters a body's physical
 *       radius plus the collision safety margin. These events reject a route.</li>
 *   <li>{@link BlockType#GRAVITY_WELL} — the ship passes within a body's sphere
 *       of influence (its {@code influenceRadius}) without an explicit flyby plan.
 *       Only {@link Satellite} bodies carry an influence radius; focal points
 *       (stars) are never reported as gravity-well encounters. These events are
 *       candidates for conversion into gravity-assist waypoints.</li>
 * </ul>
 *
 * <p>Bodies listed in {@code ignoredBodies} are skipped entirely — typically
 * the route's source, destination, and any already-used assist bodies.</p>
 *
 * <p>The returned {@link RouteValidation} contains blocking events sorted
 * chronologically so the planner can process them in encounter order.</p>
 */
public final class RouteValidator {

    private static final int SAMPLES_PER_LEG = 48;

    private RouteValidator() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Validate a trajectory against all bodies.
     *
     * @param trajectory    route to validate (may be null → returns clear)
     * @param allBodies     every body in the universe
     * @param ignoredBodies bodies to skip (source, destination, used assists)
     * @param currentSim    current simulation time for position prediction
     * @param shipRadius    physical radius of the ship (added to collision check)
     */
    public static RouteValidation validate(final Trajectory trajectory,
                                           final Collection<? extends CelestialBody> allBodies,
                                           final Set<CelestialBody> ignoredBodies,
                                           final long currentSim,
                                           final double shipRadius) {
        if (trajectory == null || trajectory.getLegs().isEmpty()) {
            return RouteValidation.clear();
        }

        final List<BlockingEvent> events = new ArrayList<>();
        for (final Trajectory.Leg leg : trajectory.getLegs()) {
            // Validate every leg independently because a multi-leg route may be
            // safe before a flyby but unsafe after it, or vice versa.
            sampleLeg(leg, allBodies, ignoredBodies, currentSim, shipRadius, events);
        }

        // The planner wants to respond to the earliest problem first. Sorting
        // gives it temporal order even though events were collected leg by leg.
        Collections.sort(events);

        final boolean hasCollision = events.stream()
                .anyMatch(e -> e.blockType() == BlockType.COLLISION);
        final boolean hasGravityWell = events.stream()
                .anyMatch(e -> e.blockType() == BlockType.GRAVITY_WELL);

        return new RouteValidation(events.isEmpty(), hasCollision, hasGravityWell, List.copyOf(events));
    }

    /**
     * Quick boolean check — skips building the full event list.
     * Used when the planner only needs to know if a route is clear.
     */
    public static boolean isClear(final Trajectory trajectory,
                                   final Collection<? extends CelestialBody> allBodies,
                                   final Set<CelestialBody> ignoredBodies,
                                   final long currentSim,
                                   final double shipRadius) {
        return validate(trajectory, allBodies, ignoredBodies, currentSim, shipRadius).isClear();
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private static void sampleLeg(final Trajectory.Leg leg,
                                   final Collection<? extends CelestialBody> allBodies,
                                   final Set<CelestialBody> ignoredBodies,
                                   final long currentSim,
                                   final double shipRadius,
                                   final List<BlockingEvent> events) {
        // Track which bodies have already been reported on this leg so we emit
        // only the first (earliest) encounter per body per leg.
        final Set<CelestialBody> reportedThisLeg = new HashSet<>();

        final long span = Math.max(1L, leg.arrivalSimTime - leg.departureSimTime);
        final double collisionMargin = TrajectoryConfigurations.collisionSafetyMargin();

        for (int i = 0; i <= SAMPLES_PER_LEG; i++) {
            final double t = (double) i / SAMPLES_PER_LEG;
            final long simTime = leg.departureSimTime + (long) (span * t);
            final Vec2 shipPos = new Vec2(leg.xAt(t), leg.yAt(t));

            for (final CelestialBody body : allBodies) {
                // Skip bodies we've been asked to ignore.
                if (ignoredBodies.contains(body)) continue;
                // Skip the leg's own origin and destination — approach is intentional.
                if (body.equals(leg.origin) || body.equals(leg.destination)) continue;
                // Skip if already reported for this leg.
                if (reportedThisLeg.contains(body)) continue;

                final Vec2 bodyPos = OrbitalMechanics.predict(body, currentSim, simTime);
                final double dist = OrbitalMechanics.dist(shipPos, bodyPos);
                final double bodyRadius = body.radius().doubleValue();

                // --- Collision check ---
                // Physical collision is stricter than gravity-well entry and
                // always makes a route invalid unless the body is intentionally
                // being visited/ignored.
                final double collisionThreshold = bodyRadius + shipRadius + collisionMargin;
                if (dist < collisionThreshold) {
                    final Vec2 bodyVel = bodyVelocityNorm(body, currentSim, simTime);
                    events.add(new BlockingEvent(body, simTime, shipPos, bodyVel,
                            BlockType.COLLISION, dist));
                    reportedThisLeg.add(body);
                    continue;
                }

                // --- Gravity-well check (Satellite bodies only) ---
                if (body instanceof final Satellite satellite) {
                    final double influenceRadius = satellite.getInfluenceRadius().doubleValue();
                    // Only flag if the influence radius is meaningfully larger than the body.
                    if (influenceRadius > bodyRadius * 1.1 && dist < influenceRadius) {
                        // Gravity-well events are not automatically fatal. They
                        // are invitations for Navigator to try inserting a
                        // planned flyby at this body.
                        final Vec2 bodyVel = bodyVelocityNorm(body, currentSim, simTime);
                        events.add(new BlockingEvent(body, simTime, shipPos, bodyVel,
                                BlockType.GRAVITY_WELL, dist));
                        reportedThisLeg.add(body);
                    }
                }
            }
        }
    }

    /** Normalized velocity direction of a body at the given simulation time. */
    private static Vec2 bodyVelocityNorm(final CelestialBody body,
                                          final long currentSim,
                                          final long simTime) {
        final Vec2 raw = OrbitalMechanics.bodyVelocityRaw(body, currentSim, simTime);
        final double len = Math.sqrt(raw.x * raw.x + raw.y * raw.y);
        if (len < 1e-12) return new Vec2(0, 1);
        return new Vec2(raw.x / len, raw.y / len);
    }
}
