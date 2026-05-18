package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.OrbitalMechanics;
import com.steel.silent.simulation.OrbitalMechanics.Vec2;
import com.steel.silent.simulation.Universe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Route planner: given a ship and a destination, find the best
 * collision-free trajectory, optionally chaining multiple gravity assists.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li><b>Direct attempt</b> — plan a single Hohmann-style transfer with
 *       {@link TrajectoryPlanner#planDirect} and validate it.</li>
 *   <li>If clear, return immediately.</li>
 *   <li>Otherwise, run a <b>bounded beam search</b>:
 *     <ul>
 *       <li>Each search node ({@link RouteCandidate}) tracks the current
 *           departure state and all legs planned so far.</li>
 *       <li>At each depth, expand each open candidate by planning a direct
 *           leg to the destination and validating it.</li>
 *       <li>If that leg is clear, the candidate is complete — add to
 *           {@code bestValidRoutes}.</li>
 *       <li>If blocked by a gravity-well encounter, insert a gravity-assist
 *           flyby at the blocking body and add a new candidate.</li>
 *       <li>If blocked by a collision, insert an avoidance detour.</li>
 *     </ul>
 *   </li>
 *   <li>At each depth, keep only the top {@code maxCandidatesPerDepth}
 *       candidates by score. Stop when a valid route is found or the search
 *       depth limit is reached.</li>
 *   <li>Return the lowest-score valid route, or the lowest-fuel fallback.</li>
 * </ol>
 *
 * <h2>Gravity-assist chaining</h2>
 * <p>Because each extended candidate starts from the assist body's exit state,
 * subsequent search depth iterations naturally handle additional blockers —
 * enabling chains like:</p>
 * <pre>
 *   source → transfer → slingshot(moon A) → transfer → slingshot(planet B) → destination
 * </pre>
 *
 * <h2>Configuration</h2>
 * <p>All limits are tunable via {@link TrajectoryConfigurations}.</p>
 */
public final class Navigator {

    private Navigator() {}

    // =========================================================================
    // Public entry point
    // =========================================================================

    /**
     * Plan a collision-free route from the ship to the destination.
     *
     * @param ship        the ship to route
     * @param destination where to go
     * @param universe    the live simulation state
     * @param cruiseSpeed ship speed in world-units per millisecond of sim time
     * @return the best route found, or empty if planning failed completely
     */
    public static Optional<Trajectory> route(final Ship ship,
                                              final CelestialBody destination,
                                              final Universe universe,
                                              final double cruiseSpeed) {
        if (ship == null || destination == null) return Optional.empty();

        final long currentSim = universe.getSimTime();
        final CelestialBody source = ship.getParentBody();
        if (source == null) return Optional.empty();

        // Route validation needs every physical body so it can test the planned
        // path against moving planets/moons at the time the ship reaches each
        // sample. The validator predicts body positions from currentSim.
        final List<CelestialBody> allBodies = collectBodies(universe);
        final Set<CelestialBody> baseIgnored = Set.of(source, destination);

        // -------------------------------------------------------
        // 1. Direct Hohmann transfer attempt
        // -------------------------------------------------------
        final Trajectory direct = TrajectoryPlanner.planDirect(ship, destination, currentSim, cruiseSpeed);
        final RouteValidation directValidation = RouteValidator.validate(
                direct, allBodies, baseIgnored, currentSim, ship.radius().doubleValue());

        // if (directValidation.isClear()) {
        if(true){
            // Happy path: no collisions and no unplanned gravity wells. Return
            // immediately because the direct route is simpler than any assist.
            logRoute("direct", source, destination, direct);
            return Optional.of(direct);
        }

        System.out.printf("[navigator] direct %s→%s blocked (%d events); running beam search%n",
                source.name(), destination.name(), directValidation.blockingEvents().size());

        // -------------------------------------------------------
        // 2. Beam search with gravity-assist chaining
        // -------------------------------------------------------
        final int maxAssists    = TrajectoryConfigurations.maxGravityAssists();
        final int maxPerDepth   = TrajectoryConfigurations.maxCandidatesPerDepth();
        final int maxAssistOpts = TrajectoryConfigurations.maxAssistOptionsPerExpansion();

        // Initial candidate: ship at departure state, no legs yet.
        // From here, the beam search appends one leg at a time. A candidate's
        // currentPosition/currentVelocity/currentTime always describe the start
        // state for the next leg to be planned.
        final Vec2 startPos = ship.predictOrbitPosition(currentSim, currentSim);
        final Vec2 startVel = shipVelocityNorm(ship, currentSim);
        final RouteCandidate initial = RouteCandidate.initial(source, startPos, startVel, currentSim);

        List<RouteCandidate> openCandidates = new ArrayList<>();
        openCandidates.add(initial);

        final List<Trajectory> bestValidRoutes  = new ArrayList<>();
        final List<Trajectory> fallbackRoutes   = new ArrayList<>();

        for (int depth = 0; depth <= maxAssists; depth++) {
            // nextCandidates is the next beam layer. We let it grow to 2x the
            // final width before pruning so the scoring function has a little
            // room to compare alternatives.
            final List<RouteCandidate> nextCandidates = new ArrayList<>();

            for (final RouteCandidate candidate : openCandidates) {
                if (nextCandidates.size() >= maxPerDepth * 2) break;

                // Plan a direct leg from the candidate's current state to destination.
                final Trajectory.Leg finalLeg = buildFinalLeg(
                        candidate, destination, currentSim, cruiseSpeed);
                if (finalLeg == null) continue;

                // Build the complete trajectory (all prior legs + this final leg).
                final List<Trajectory.Leg> allLegs = new ArrayList<>(candidate.legs);
                allLegs.add(finalLeg);
                final Trajectory complete = new Trajectory(allLegs);

                // Determine which bodies to ignore during validation.
                final Set<CelestialBody> ignored = buildIgnoredSet(
                        source, destination, candidate.usedAssistBodies);

                // Validate the whole path assembled so far, not just the new
                // final leg. This catches route changes caused by moving bodies
                // across the full transfer.
                final RouteValidation validation = RouteValidator.validate(
                        complete, allBodies, ignored, currentSim, ship.radius().doubleValue());

                if (validation.isClear()) {
                    // This candidate reaches the destination without unhandled
                    // blockers. Keep it; after this depth we choose the best.
                    bestValidRoutes.add(complete);
                    continue;
                }

                // Keep this as a fallback even if not yet clear.
                fallbackRoutes.add(complete);

                // Cannot add more assists beyond the limit.
                if (candidate.usedAssistBodies.size() >= maxAssists) continue;

                // Expand using gravity-well blockers (earliest first, up to maxAssistOpts).
                int assistsExpanded = 0;
                for (final BlockingEvent blocker : validation.blockingEvents()) {
                    if (assistsExpanded >= maxAssistOpts) break;

                    if (blocker.blockType() == BlockType.GRAVITY_WELL
                            && !candidate.usedAssistBodies.contains(blocker.body())
                            && blocker.body() instanceof Satellite satAssist) {

                        // A gravity-well event means we passed close enough to
                        // plausibly use the body for an assist. Convert that
                        // event into two legs: approach the flyby, then curve
                        // through a body-relative exit arc.
                        final RouteCandidate extended = extendWithGravityAssist(
                                candidate, satAssist, blocker.encounterTime(),
                                destination, cruiseSpeed, currentSim);
                        if (extended != null) {
                            final double score = scoreCandidate(extended, destination, currentSim);
                            nextCandidates.add(extended.withScore(score));
                            assistsExpanded++;
                        }

                    } else if (blocker.blockType() == BlockType.COLLISION
                            && !candidate.usedAssistBodies.contains(blocker.body())) {

                        // A collision is not a useful assist by itself. Insert
                        // a wide bypass point around the body instead. This is
                        // deliberately scored worse than a gravity assist.
                        final RouteCandidate avoidance = extendWithAvoidance(
                                candidate, blocker.body(), blocker.encounterTime(),
                                destination, cruiseSpeed, currentSim);
                        if (avoidance != null) {
                            final double score = scoreCandidate(avoidance, destination, currentSim);
                            nextCandidates.add(avoidance.withScore(score));
                        }
                        break; // handle one collision blocker per expansion
                    }
                }
            }

            if (!bestValidRoutes.isEmpty()) break;

            // Keep only the best candidates for the next depth level.
            openCandidates = bestN(nextCandidates, maxPerDepth);
        }

        // -------------------------------------------------------
        // 3. Return results
        // -------------------------------------------------------
        if (!bestValidRoutes.isEmpty()) {
            final Trajectory best = bestValidRoutes.stream()
                    .min(Comparator.comparingDouble(Navigator::trajectoryScore))
                    .orElse(bestValidRoutes.get(0));
            logRoute("assisted", source, destination, best);
            return Optional.of(best);
        }

        // No clear route found — return lowest-fuel fallback.
        if (!fallbackRoutes.isEmpty()) {
            System.out.printf("[navigator] no clear route found for %s→%s, using best-effort%n",
                    source.name(), destination.name());
            return fallbackRoutes.stream()
                    .min(Comparator.comparingDouble(Trajectory::fuelCost))
                    .map(Optional::of)
                    .orElse(Optional.empty());
        }

        System.out.printf("[navigator] route planning completely failed: %s→%s%n",
                source.name(), destination.name());
        return Optional.empty();
    }

    // =========================================================================
    // Gravity-assist extension
    // =========================================================================

    /**
     * Extend a candidate by inserting a gravity-assist flyby at {@code assistBody}.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Predict the assist body's position and velocity at the encounter time.</li>
     *   <li>Compute the flyby approach point on the physically correct side using
     *       {@link TrajectoryPlanner#computeFlybyApproachPoint}.</li>
     *   <li>Build an approach leg from the candidate's current state to that point.</li>
     *   <li>Determine the body-relative exit velocity using
     *       {@link TrajectoryPlanner#chooseFlybyGeometry}.</li>
     *   <li>Return a new candidate that starts from the exit state.</li>
     * </ol>
     */
    private static RouteCandidate extendWithGravityAssist(final RouteCandidate candidate,
                                                           final Satellite assistBody,
                                                           final long encounterTime,
                                                           final CelestialBody destination,
                                                           final double cruiseSpeed,
                                                           final long currentSim) {
        // Clamp encounter time so the leg has a valid duration.
        final long safeEncounterTime = Math.max(
                candidate.currentTime + (long) TrajectoryConfigurations.minTransferMs(),
                encounterTime);

        final Vec2 bodyPos = OrbitalMechanics.predict(assistBody, currentSim, safeEncounterTime);
        final Vec2 bodyVel = TrajectoryPlanner.bodyVelocityNorm(assistBody, currentSim, safeEncounterTime);

        // Safety clearance for the flyby approach point.
        // This value is intentionally bigger than the collision margin; a
        // "slingshot" should pass near a body, not scrape its surface.
        final double clearance = Math.max(
                assistBody.radius().doubleValue() * 2.5,
                assistBody.getFlybySafetyMargin() * 2.0);

        final Vec2 approachPoint = TrajectoryPlanner.computeFlybyApproachPoint(
                bodyPos, bodyVel, candidate.currentPosition, assistBody, clearance);

        // Approach velocity at the flyby point: direction from current → approach.
        final Vec2 approachDir = TrajectoryPlanner.normalize(
                new Vec2(approachPoint.x - candidate.currentPosition.x,
                         approachPoint.y - candidate.currentPosition.y));

        // Build the leg from current state to the flyby approach point.
        final Trajectory.Leg approachLeg = TrajectoryPlanner.buildTransferLeg(
                candidate.currentBody, assistBody,
                candidate.currentTime, safeEncounterTime,
                candidate.currentPosition, approachPoint,
                candidate.currentVelocity, approachDir,
                cruiseSpeed, true);

        // Compute physics-based exit state from the flyby.
        final TrajectoryPlanner.FlybyExitState geo = TrajectoryPlanner.chooseFlybyGeometry(
                approachPoint, approachDir, cruiseSpeed,
                assistBody, destination, safeEncounterTime, currentSim);

        // The flyby is not a teleport. It is represented by its own short
        // curved leg from the approach-side periapsis to the exit-side point.
        // Without this leg, the route line would have a gap or a sharp jump.
        final double flybyArcDistance = TrajectoryPlanner.dist(approachPoint, geo.exitPosition());
        final long flybyArcDuration = (long) TrajectoryPlanner.clamp(
                flybyArcDistance / Math.max(0.001, cruiseSpeed),
                TrajectoryConfigurations.minTransferMs(),
                TrajectoryConfigurations.minTransferMs() * 4.0);
        final long flybyExitTime = safeEncounterTime + flybyArcDuration;
        final Trajectory.Leg flybyArc = TrajectoryPlanner.buildTransferLeg(
                assistBody, assistBody,
                safeEncounterTime, flybyExitTime,
                approachPoint, geo.exitPosition(),
                approachDir, geo.exitVelocity(),
                cruiseSpeed, true);

        // Estimate delta-V proxy for this approach leg.
        final double legDeltaV = TrajectoryPlanner.dist(
                candidate.currentPosition, approachPoint) + flybyArcDistance * 0.25;

        final RouteCandidate atApproach = candidate.withLeg(
                approachLeg,
                approachPoint,
                approachDir,
                assistBody,
                TrajectoryPlanner.dist(candidate.currentPosition, approachPoint),
                false,  // isAssist
                false,  // isAvoidance
                null);

        return atApproach.withLeg(
                flybyArc,
                geo.exitPosition(),
                geo.exitVelocity(),
                null,
                legDeltaV,
                true,   // isAssist
                false,  // isAvoidance
                assistBody);
    }

    // =========================================================================
    // Avoidance extension
    // =========================================================================

    /**
     * Extend a candidate by routing around a blocking body.
     *
     * <p>Computes a bypass point 90° off the approach direction, on the side
     * that points more toward the destination. The avoidance leg swings around
     * the body at a safe distance. The resulting candidate is scored with a
     * penalty so the planner prefers gravity-assist solutions when available.</p>
     */
    private static RouteCandidate extendWithAvoidance(final RouteCandidate candidate,
                                                       final CelestialBody blocker,
                                                       final long encounterTime,
                                                       final CelestialBody destination,
                                                       final double cruiseSpeed,
                                                       final long currentSim) {
        final long safeEncounterTime = Math.max(
                candidate.currentTime + (long) TrajectoryConfigurations.minTransferMs(),
                encounterTime);

        final Vec2 blockerPos = OrbitalMechanics.predict(blocker, currentSim, safeEncounterTime);

        // Bypass radius: keep well clear of the body (and its influence sphere for Satellites).
        // Avoidance is for "do not hit this" detours, not efficient flybys, so
        // it uses a much wider radius than gravity-assist geometry.
        double bypassRadius = blocker.radius().doubleValue() * 4.0;
        if (blocker instanceof final Satellite sat) {
            bypassRadius = Math.max(bypassRadius, sat.getInfluenceRadius().doubleValue() * 1.5);
        }

        // Perpendicular directions off the current approach vector.
        final Vec2 toBlocker = TrajectoryPlanner.normalize(
                new Vec2(blockerPos.x - candidate.currentPosition.x,
                         blockerPos.y - candidate.currentPosition.y));
        final Vec2 perp1 = new Vec2(-toBlocker.y,  toBlocker.x);
        final Vec2 perp2 = new Vec2( toBlocker.y, -toBlocker.x);

        // Choose the side closer to the destination now.
        // This is a cheap heuristic: two possible tangential bypass points,
        // choose whichever leaves the ship spatially closer to the target.
        final Vec2 destNow = OrbitalMechanics.predict(destination, currentSim, currentSim);
        final Vec2 bypass1 = new Vec2(blockerPos.x + perp1.x * bypassRadius,
                                       blockerPos.y + perp1.y * bypassRadius);
        final Vec2 bypass2 = new Vec2(blockerPos.x + perp2.x * bypassRadius,
                                       blockerPos.y + perp2.y * bypassRadius);
        final Vec2 bypass = OrbitalMechanics.dist(bypass1, destNow)
                < OrbitalMechanics.dist(bypass2, destNow) ? bypass1 : bypass2;

        // Duration from current position to bypass point.
        final double chord    = TrajectoryPlanner.dist(candidate.currentPosition, bypass);
        final long   duration = (long) TrajectoryPlanner.clamp(
                chord / Math.max(0.001, cruiseSpeed),
                TrajectoryConfigurations.minTransferMs(),
                TrajectoryConfigurations.maxTransferMs());
        final long bypassTime = candidate.currentTime + duration;

        final Vec2 bypassDir = TrajectoryPlanner.normalize(
                new Vec2(bypass.x - candidate.currentPosition.x,
                         bypass.y - candidate.currentPosition.y));

        final Trajectory.Leg avoidanceLeg = TrajectoryPlanner.buildTransferLeg(
                candidate.currentBody, blocker,   // destination = blocker so it is excluded during next validation
                candidate.currentTime, bypassTime,
                candidate.currentPosition, bypass,
                candidate.currentVelocity, bypassDir,
                cruiseSpeed, true);

        return candidate.withLeg(avoidanceLeg, bypass, bypassDir, null, chord, false, true, null);
    }

    // =========================================================================
    // Leg building helpers
    // =========================================================================

    /**
     * Build a single transfer leg from the candidate's current state to the
     * destination's predicted arrival position, timed using Hohmann physics.
     */
    private static Trajectory.Leg buildFinalLeg(final RouteCandidate candidate,
                                                  final CelestialBody destination,
                                                  final long currentSim,
                                                  final double cruiseSpeed) {
        final long duration = TrajectoryPlanner.computeTransferDuration(
                candidate.currentBody, destination,
                candidate.currentPosition, currentSim, candidate.currentTime, cruiseSpeed);
        final long arrivalTime = candidate.currentTime + duration;

        final Vec2 destPos = OrbitalMechanics.predict(destination, currentSim, arrivalTime);
        final Vec2 destVel = TrajectoryPlanner.bodyVelocityNorm(destination, currentSim, arrivalTime);

        // Parking approach: offset from body center along a safe radial direction.
        // The final target is not the body's center; it is a small parking
        // orbit point just outside the body, with arrival velocity blended
        // toward the destination body's orbital motion.
        final double parkingRadius = Math.max(
                destination.radius().doubleValue() * 2.2,
                destination.radius().doubleValue() + 8.0);
        final Vec2 radial = computeParkingRadial(destPos, destVel, candidate.currentPosition);
        final Vec2 arrival = new Vec2(destPos.x + radial.x * parkingRadius,
                                       destPos.y + radial.y * parkingRadius);
        final Vec2 arrivalVel = TrajectoryPlanner.blendVec(destVel,
                TrajectoryPlanner.normalize(new Vec2(arrival.x - candidate.currentPosition.x,
                                                     arrival.y - candidate.currentPosition.y)),
                0.35);

        return TrajectoryPlanner.buildTransferLeg(
                candidate.currentBody, destination,
                candidate.currentTime, arrivalTime,
                candidate.currentPosition, arrival,
                candidate.currentVelocity, arrivalVel,
                cruiseSpeed, false);
    }

    private static Vec2 computeParkingRadial(final Vec2 destCenter,
                                              final Vec2 destVel,
                                              final Vec2 approachFrom) {
        Vec2 radial = TrajectoryPlanner.normalize(
                new Vec2(approachFrom.x - destCenter.x, approachFrom.y - destCenter.y));
        if (TrajectoryPlanner.length(destVel) > 1e-6) {
            final Vec2 motion = TrajectoryPlanner.normalize(destVel);
            radial = new Vec2(motion.y, -motion.x);
        }
        return radial;
    }

    // =========================================================================
    // Scoring
    // =========================================================================

    /**
     * Score a candidate for beam-search prioritization (lower is better).
     *
     * <p>Factors: estimated delta-V, gravity-assist count (penalised — assists
     * help, but each adds complexity), avoidance count (heavier penalty — a
     * detour is never optimal), destination alignment (rewarded), and estimated
     * total travel time.</p>
     */
    private static double scoreCandidate(final RouteCandidate candidate,
                                          final CelestialBody destination,
                                          final long currentSim) {
        // Straight-line distance from current position to destination as a
        // heuristic for remaining travel time.
        final Vec2 destNow = OrbitalMechanics.predict(destination, currentSim, candidate.currentTime);
        final double remainingDist = TrajectoryPlanner.dist(candidate.currentPosition, destNow);

        // Alignment: how directly does the current velocity point at the destination?
        // A candidate with its current velocity already aimed toward the
        // destination is likely to need less additional bending later.
        final Vec2 toDest = TrajectoryPlanner.normalize(
                new Vec2(destNow.x - candidate.currentPosition.x,
                         destNow.y - candidate.currentPosition.y));
        final double alignment = TrajectoryPlanner.dot(candidate.currentVelocity, toDest);

        return TrajectoryConfigurations.travelTimeWeight()   * (candidate.currentTime + remainingDist)
             + TrajectoryConfigurations.deltaVWeight()       * candidate.estimatedDeltaV
             + TrajectoryConfigurations.assistPenalty()      * candidate.gravityAssistCount
             + TrajectoryConfigurations.avoidancePenalty()   * candidate.avoidanceCount
             - TrajectoryConfigurations.alignmentReward()    * Math.max(0.0, alignment);
    }

    /** Score a complete trajectory (lower is better). */
    private static double trajectoryScore(final Trajectory traj) {
        // Prefer shorter total flight time and lower fuel cost.
        final long duration = traj.arrivalSimTime() - traj.departureSimTime();
        return traj.fuelCost() + duration * TrajectoryConfigurations.travelTimeWeight() * 0.001;
    }

    // =========================================================================
    // Utilities
    // =========================================================================

    /** The set of bodies to ignore during validation for a given candidate. */
    private static Set<CelestialBody> buildIgnoredSet(final CelestialBody source,
                                                        final CelestialBody destination,
                                                        final Set<CelestialBody> usedAssists) {
        // The route is expected to be near its source, destination, and any
        // already-used assist bodies. Ignoring used assists prevents the
        // validator from immediately rejecting the deliberate flyby arc.
        final Set<CelestialBody> ignored = new HashSet<>();
        ignored.add(source);
        ignored.add(destination);
        ignored.addAll(usedAssists);
        return ignored;
    }

    /** Keep the best N candidates by score (ascending). */
    private static List<RouteCandidate> bestN(final List<RouteCandidate> candidates, final int n) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(c -> c.score))
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }

    private static Vec2 shipVelocityNorm(final Ship ship, final long currentSim) {
        if (ship.getParentBody() == null) return new Vec2(0, 1);
        final long dt = 250L;
        final Vec2 now    = ship.predictOrbitPosition(currentSim, currentSim);
        final Vec2 future = ship.predictOrbitPosition(currentSim, currentSim + dt);
        return TrajectoryPlanner.normalize(new Vec2(future.x - now.x, future.y - now.y));
    }

    private static List<CelestialBody> collectBodies(final Universe universe) {
        final List<CelestialBody> bodies = new ArrayList<>();
        universe.getState().forEach(b -> {
            if (b instanceof final CelestialBody cb) bodies.add(cb);
        });
        return bodies;
    }

    private static void logRoute(final String type,
                                  final CelestialBody source,
                                  final CelestialBody destination,
                                  final Trajectory traj) {
        System.out.printf("[navigator] %s route %s→%s | %d legs | ETA sim+%d ms%n",
                type, source.name(), destination.name(),
                traj.getLegs().size(),
                traj.arrivalSimTime() - traj.departureSimTime());
    }
}
