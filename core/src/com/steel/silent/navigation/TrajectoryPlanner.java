package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.OrbitalMechanics;
import com.steel.silent.simulation.OrbitalMechanics.Vec2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds simplified Hohmann-style visual transfers.
 *
 * <h2>Design</h2>
 * <p>The simulation uses prescribed circular orbits rather than Newtonian
 * gravity, so this planner favors visual plausibility over physical precision.
 * All transfers are cubic Bézier curves that:</p>
 * <ul>
 *   <li>depart tangent to the source body's orbital velocity,</li>
 *   <li>arrive tangent to the destination body's orbital velocity at the
 *       predicted arrival time, and</li>
 *   <li>use Hohmann-derived timing so the arc length and duration feel
 *       physically motivated.</li>
 * </ul>
 *
 * <h2>Flyby geometry</h2>
 * <p>When a stop is a gravity-assist flyby, the exit velocity is computed in
 * the body-relative frame:</p>
 * <ol>
 *   <li>Convert inbound velocity to body-relative: {@code vRel = vShip - vBody}.</li>
 *   <li>Rotate {@code vRel} by the turn angle on the chosen side (left/right).</li>
 *   <li>Add body velocity back: {@code vExit = vRelRotated + vBody}.</li>
 * </ol>
 * <p>The side is chosen so the exit velocity points most directly toward the
 * destination. The turn angle is estimated from the body's
 * {@code gravityAssistStrength}, periapsis distance, and relative speed.</p>
 */
public final class TrajectoryPlanner {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    static final double DEFAULT_HANDLE_FRACTION = 0.36;
    static final double FLYBY_HANDLE_FRACTION   = 0.24;

    private static final double REFERENCE_CRUISE_SPEED      = 0.20;
    private static final double PARKING_ORBIT_MULTIPLIER    = 2.2;
    private static final double FLYBY_CLEARANCE_MULTIPLIER  = 3.0;
    private static final int    BASE_LAUNCH_WINDOW_SAMPLES  = 72;
    private static final double TANGENT_MISMATCH_COST       = 250.0;
    private static final double SHARED_MOON_PHASE_COST      = 900.0;

    /** Destination look-ahead when scoring flyby sides (ms of sim time). */
    private static final long   FLYBY_DESTINATION_LOOKAHEAD = 30_000L;

    private TrajectoryPlanner() {}

    // =========================================================================
    // Public API — called by Navigator and the legacy ShipCommandKeyHandler
    // =========================================================================

    /**
     * Plan a multi-stop route from the ship's current state.
     *
     * <p>Picks the optimal departure window by scanning one orbital period and
     * selecting the departure angle that minimises fuel cost + wait time +
     * tangent mismatch.</p>
     */
    public static Trajectory plan(final Ship ship,
                                  final long currentSim,
                                  final List<RouteStop> stops,
                                  final double baseCruiseSpeed) {
        if (ship == null || stops == null || stops.isEmpty()) {
            return new Trajectory(List.of());
        }
        final LaunchWindow window = chooseLaunchWindow(ship, currentSim, stops, baseCruiseSpeed);
        return planFromDeparture(ship, currentSim, stops, baseCruiseSpeed, window);
    }

    /**
     * Plan a single direct transfer from the ship to the destination.
     *
     * <p>Equivalent to calling {@link #plan} with a single
     * {@link RouteStop#destination} stop.</p>
     */
    public static Trajectory planDirect(final Ship ship,
                                         final CelestialBody destination,
                                         final long currentSim,
                                         final double cruiseSpeed) {
        return plan(ship, currentSim, List.of(RouteStop.destination(destination)), cruiseSpeed);
    }

    /**
     * Estimate Hohmann transfer parameters between two bodies sharing a common
     * parent (or between a body and its child — e.g. planet to moon).
     *
     * <p>Uses the parent's {@link CelestialBody#getVisualMu()} for the
     * half-period formula: {@code T = π √(a³/μ)}.</p>
     *
     * <p>Returns {@code null} if no usable common parent frame can be found.</p>
     *
     * @param source      departure body
     * @param destination arrival body
     * @param startTime   simulation time of departure
     * @param currentSim  current simulation time (for position prediction)
     */
    public static HohmannEstimate estimateHohmann(final CelestialBody source,
                                                   final CelestialBody destination,
                                                   final long startTime,
                                                   final long currentSim) {
        // The "focus" is the simplified gravity center for the transfer. In a
        // moon-to-moon trip this is the planet; in a planet-to-planet trip it is
        // the star; in a planet-to-moon trip it is the planet.
        final CelestialBody parent = commonTransferFocus(source, destination);
        if (parent == null) return null;

        // Hohmann math is expressed in the focus body's local frame, so compute
        // both orbit radii relative to the shared parent at departure time.
        final Vec2 parentPos  = OrbitalMechanics.predict(parent, currentSim, startTime);
        final Vec2 sourcePos  = OrbitalMechanics.predict(source, currentSim, startTime);

        final double r1 = Math.max(1.0, dist(sourcePos, parentPos));
        final double r2 = Math.max(1.0, orbitalRadiusAround(destination, parent));
        final double a  = (r1 + r2) * 0.5;
        final double mu = parent.getVisualMu();

        // T_transfer = π √(a³ / μ). visualMu is deliberately tunable, so this
        // has the shape of a real Hohmann half-period without pretending to be
        // physically exact.
        final double rawTransferTime = Math.PI * Math.sqrt(a * a * a / Math.max(1e-15, mu));
        final double transferTime = clamp(rawTransferTime,
                TrajectoryConfigurations.minTransferMs(),
                TrajectoryConfigurations.maxTransferMs());

        // Circular orbital velocities
        final double vc1 = Math.sqrt(mu / r1);
        final double vc2 = Math.sqrt(mu / r2);

        // Transfer ellipse velocities
        final double vPeri = Math.sqrt(mu * (2.0 / r1 - 1.0 / a));
        final double vApo  = Math.sqrt(mu * (2.0 / r2 - 1.0 / a));

        // deltaV — signed so the planner knows burn direction
        final double dv1 = (r2 > r1) ? (vPeri - vc1) : (vPeri - vc1); // prograde departure
        final double dv2 = (r2 > r1) ? (vc2  - vApo) : (vc2  - vApo); // arrival insertion

        final long arrivalTime = startTime + (long) transferTime;

        return new HohmannEstimate(r1, r2, a, transferTime, dv1, dv2,
                r2 >= r1, parent, arrivalTime);
    }

    // =========================================================================
    // Package-private helpers used by Navigator
    // =========================================================================

    /**
     * Build a single Bézier transfer leg between two arbitrary world positions.
     *
     * <p>Called by the Navigator when constructing gravity-assist approach and
     * continuation legs. The Bézier control points are derived from the supplied
     * velocity directions and chord length, matching the convention used in
     * {@link #planFromDeparture}.</p>
     */
    static Trajectory.Leg buildTransferLeg(final CelestialBody origin,
                                            final CelestialBody destination,
                                            final long departureTime,
                                            final long arrivalTime,
                                            final Vec2 departure,
                                            final Vec2 arrival,
                                            final Vec2 departureVelocity,
                                            final Vec2 arrivalVelocity,
                                            final double cruiseSpeed,
                                            final boolean isFlyby) {
        final double chord  = Math.max(1.0, dist(departure, arrival));
        final double handle = chord * (isFlyby ? FLYBY_HANDLE_FRACTION : DEFAULT_HANDLE_FRACTION);
        // Place Bezier handles along velocity directions, but clamp them so
        // neither endpoint points backward relative to the leg chord. This is
        // what prevents visible hooks and loops in route previews.
        final Vec2 chordDir = normalize(new Vec2(arrival.x - departure.x, arrival.y - departure.y));
        final Vec2 startTangent = forwardTangent(departureVelocity, chordDir);
        final Vec2 endTangent = forwardTangent(arrivalVelocity, chordDir);
        final Vec2 c1 = new Vec2(departure.x + startTangent.x * handle,
                                  departure.y + startTangent.y * handle);
        final Vec2 c2 = new Vec2(arrival.x   - endTangent.x   * handle,
                                  arrival.y   - endTangent.y   * handle);
        final double fuelCost = chord * (isFlyby ? 0.55 : 1.0);
        return new Trajectory.Leg(origin, destination, departureTime, arrivalTime,
                departure.x, departure.y, arrival.x, arrival.y,
                c1.x, c1.y, c2.x, c2.y,
                startTangent.x, startTangent.y,
                endTangent.x,   endTangent.y,
                cruiseSpeed, fuelCost);
    }

    /**
     * Compute how long a transfer from {@code departure} toward {@code destination}
     * should take, using Hohmann timing when a common parent exists.
     */
    static long computeTransferDuration(final CelestialBody source,
                                         final CelestialBody destination,
                                         final Vec2 departure,
                                         final long currentSim,
                                         final long departureTime,
                                         final double cruiseSpeed) {
        return transferDuration(source, destination, departure, currentSim, departureTime, cruiseSpeed);
    }

    /**
     * Compute the flyby approach point on the correct side of the body.
     * The point is placed at {@code flybyRadius} from the body's center, on the
     * side that doesn't face the incoming approach direction.
     */
    static Vec2 computeFlybyApproachPoint(final Vec2 bodyCenter,
                                           final Vec2 bodyVelocity,
                                           final Vec2 approachFrom,
                                           final CelestialBody body,
                                           final double clearance) {
        return flybyPoint(bodyCenter, bodyVelocity, approachFrom, body, clearance);
    }

    /**
     * Encapsulates the exit state produced by a gravity-assist flyby.
     *
     * @param exitPosition world position after the flyby arc
     * @param exitVelocity normalized exit velocity direction in world frame
     * @param turnAngle    actual turn angle applied (radians)
     * @param side         +1.0 for counter-clockwise bend, -1.0 for clockwise
     */
    public record FlybyExitState(Vec2 exitPosition, Vec2 exitVelocity, double turnAngle, double side) {}

    /**
     * Choose flyby geometry: pick the approach side that bends the ship most
     * directly toward the destination after the assist.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Compute body velocity in world-units/ms (not normalized).</li>
     *   <li>Subtract body velocity from ship velocity to get relative entry
     *       velocity ({@code vRel}).</li>
     *   <li>Estimate turn angle from {@code body.gravityAssistStrength},
     *       periapsis distance, and relative speed.</li>
     *   <li>Try rotating {@code vRel} by ±turnAngle. For each side, add body
     *       velocity back to get world exit velocity.</li>
     *   <li>Score each side by {@code dot(normalised worldExitVel, dirToDestination)}.</li>
     *   <li>Return the side with the best score.</li>
     * </ol>
     *
     * @param entryPosition   world position where the ship arrives at the assist body
     * @param entryVelocityDir normalized ship velocity direction on arrival
     * @param entrySpeed       ship speed in world-units/ms (typically cruise speed)
     * @param assistBody       the satellite used for gravity assist
     * @param destination      final route destination
     * @param encounterTime    simulation time of the flyby encounter
     * @param currentSim       current simulation time
     */
    static FlybyExitState chooseFlybyGeometry(final Vec2 entryPosition,
                                               final Vec2 entryVelocityDir,
                                               final double entrySpeed,
                                               final Satellite assistBody,
                                               final CelestialBody destination,
                                               final long encounterTime,
                                               final long currentSim) {
        final Vec2 bodyPos    = OrbitalMechanics.predict(assistBody, currentSim, encounterTime);
        final Vec2 bodyVelRaw = OrbitalMechanics.bodyVelocityRaw(assistBody, currentSim, encounterTime);

        // Ship velocity in world-units/ms for frame transformation.
        // We receive only a normalized direction from the route candidate, so
        // multiply by speed before subtracting the body's actual world velocity.
        final double speed = Math.max(1e-6, entrySpeed);
        final Vec2 shipVelRaw = new Vec2(entryVelocityDir.x * speed,
                                          entryVelocityDir.y * speed);

        // Relative entry velocity in body frame.
        final Vec2 vRel = new Vec2(shipVelRaw.x - bodyVelRaw.x,
                                    shipVelRaw.y - bodyVelRaw.y);
        final double relSpeed = length(vRel);

        // Periapsis distance: body surface + safety margin.
        final double periapsis = assistBody.radius().doubleValue()
                + assistBody.getFlybySafetyMargin();

        // Turn angle estimate, clamped to sane range. Stronger bodies bend
        // more; high relative speeds and wider periapsis distances bend less.
        final double minTurn = Math.toRadians(TrajectoryConfigurations.minTurnAngleDeg());
        final double maxTurn = Math.toRadians(TrajectoryConfigurations.maxTurnAngleDeg());
        final double rawTurn = assistBody.getGravityAssistStrength()
                / (periapsis * Math.max(1e-9, relSpeed * relSpeed));
        final double turnAngle = clamp(rawTurn, minTurn, maxTurn);

        // Destination predicted position well after the flyby (for scoring).
        final Vec2 destFuture = OrbitalMechanics.predict(destination, currentSim,
                encounterTime + FLYBY_DESTINATION_LOOKAHEAD);

        // Evaluate both flyby sides.
        double bestScore = Double.NEGATIVE_INFINITY;
        double bestSide  = 1.0;
        Vec2   bestExitVel = null;
        Vec2   bestExitPos = null;

        for (final double side : new double[]{1.0, -1.0}) {
            // Rotate relative velocity by ±turnAngle.
            // The gravity-assist approximation happens in the body's frame:
            // bend the relative velocity, then add the body's velocity back.
            final Vec2 vRelExit   = rotate(vRel, side * turnAngle);
            // Convert back to world frame.
            final Vec2 worldExitVel = new Vec2(vRelExit.x + bodyVelRaw.x,
                                                vRelExit.y + bodyVelRaw.y);
            final Vec2 worldExitDir = normalize(worldExitVel);

            // Exit position: offset from body center along exit direction. The
            // Navigator inserts a short flyby arc from periapsis to this point,
            // so the route is continuous rather than a teleport.
            final Vec2 exitPos = new Vec2(bodyPos.x + worldExitDir.x * periapsis * 2.5,
                                           bodyPos.y + worldExitDir.y * periapsis * 2.5);

            // Score: alignment of exit velocity toward destination.
            final Vec2 toDestination = new Vec2(destFuture.x - exitPos.x,
                                                 destFuture.y - exitPos.y);
            final double score = dot(worldExitDir, normalize(toDestination));

            if (score > bestScore) {
                bestScore   = score;
                bestSide    = side;
                bestExitVel = worldExitDir;
                bestExitPos = exitPos;
            }
        }

        if (bestExitVel == null) {
            // Degenerate fallback: keep incoming direction.
            bestExitVel = entryVelocityDir;
            bestExitPos = new Vec2(bodyPos.x + entryVelocityDir.x * periapsis * 2.5,
                                    bodyPos.y + entryVelocityDir.y * periapsis * 2.5);
        }

        return new FlybyExitState(bestExitPos, bestExitVel, turnAngle, bestSide);
    }

    // =========================================================================
    // Private planning helpers
    // =========================================================================

    private static Trajectory planFromDeparture(final Ship ship,
                                                  final long currentSim,
                                                  final List<RouteStop> stops,
                                                  final double baseCruiseSpeed,
                                                  final LaunchWindow window) {
        final List<Trajectory.Leg> legs = new ArrayList<>(stops.size());
        CelestialBody origin = ship.getParentBody();
        Vec2 departure         = window.position;
        Vec2 departureVelocity = window.velocity;
        long departureTime     = window.time;

        for (final RouteStop stop : stops) {
            if (stop == null || stop.body == null) continue;

            // RouteStop is the "what to visit" description; this block turns it
            // into concrete geometry: arrival point, arrival tangent, and time.
            final double legSpeed  = Math.max(0.001, baseCruiseSpeed * stop.speedMultiplier);
            final long duration    = transferDuration(origin, stop.body, departure,
                                                      currentSim, departureTime, legSpeed);
            final long arrivalTime = departureTime + duration;

            final Vec2 destCenter  = OrbitalMechanics.predict(stop.body, currentSim, arrivalTime);
            final Vec2 destVel     = bodyVelocityNorm(stop.body, currentSim, arrivalTime);

            final Vec2 arrival;
            final Vec2 arrivalVelocity;
            if (stop.isFlyby) {
                arrival = flybyPoint(destCenter, destVel, departure, stop.body, stop.approachClearance);
                // Use the legacy blend for multi-stop plans — the Navigator uses the
                // physics-based body-relative frame when inserting explicit gravity assists.
                arrivalVelocity = legacyFlybyExitVelocity(destVel, departure, arrival, departureVelocity);
            } else {
                arrival = parkingPoint(destCenter, destVel, departure, stop.body, stop.approachClearance);
                arrivalVelocity = blend(destVel,
                        tangentToward(departure, arrival, departureVelocity), 0.35);
            }

            final Trajectory.Leg leg = buildRawLeg(origin, stop.body, departureTime, arrivalTime,
                    departure, arrival, departureVelocity, arrivalVelocity, legSpeed, stop);
            legs.add(leg);

            origin             = stop.body;
            // The next leg begins exactly where this one ends, carrying the
            // outgoing tangent forward. That continuity is what makes chained
            // flybys and destination legs look like one route instead of a
            // series of disconnected commands.
            departure          = arrival;
            departureVelocity  = arrivalVelocity;
            departureTime      = arrivalTime;
        }

        return new Trajectory(legs);
    }

    // -------------------------------------------------------------------------
    // Launch window search
    // -------------------------------------------------------------------------

    private static LaunchWindow chooseLaunchWindow(final Ship ship,
                                                    final long currentSim,
                                                    final List<RouteStop> stops,
                                                    final double baseCruiseSpeed) {
        final CelestialBody source = ship.getParentBody();
        if (source == null || ship.getOrbitalPeriodSeconds() == null || stops.isEmpty()) {
            return launchWindowAt(ship, currentSim, currentSim);
        }

        final RouteStop firstStop    = stops.get(0);
        final long      searchHorizon = launchSearchHorizon(ship, source, firstStop.body);
        final int        samples      = Math.max(BASE_LAUNCH_WINDOW_SAMPLES,
                (int) Math.min(180, searchHorizon / 2_000L));

        // Brute-force the launch window over the search horizon. This is simple
        // and robust for circular orbits: try future departures, build the full
        // route for each, and keep the best score.
        LaunchWindow best      = null;
        double       bestScore = Double.MAX_VALUE;

        for (int i = 0; i <= samples; i++) {
            final long departureTime = currentSim + searchHorizon * i / samples;
            final LaunchWindow candidate = launchWindowAt(ship, currentSim, departureTime);
            final Trajectory traj = planFromDeparture(ship, currentSim, stops, baseCruiseSpeed, candidate);
            if (traj.getLegs().isEmpty()) continue;
            final double score = launchScore(traj, currentSim, candidate.time, source, firstStop.body);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best != null ? best : launchWindowAt(ship, currentSim, currentSim);
    }

    private static long launchSearchHorizon(final Ship ship,
                                             final CelestialBody source,
                                             final CelestialBody destination) {
        final long sourcePeriod = Duration.ofSeconds(
                ship.getOrbitalPeriodSeconds().longValue()).toMillis();
        long horizon = Math.max(1L, sourcePeriod);

        if (source instanceof final Satellite srcMoon
                && destination instanceof final Satellite dstMoon
                && srcMoon.getFocalPoint().equals(dstMoon.getFocalPoint())) {
            // Shared-parent moons have repeated phase alignments. Search enough
            // of the relative alignment cycle to find a cleaner Hohmann-style
            // departure instead of always leaving immediately.
            final long srcPeriod = srcMoon.getOrbitalSpeed().longValue();
            final long dstPeriod = dstMoon.getOrbitalSpeed().longValue();
            horizon = Math.max(horizon, Math.max(srcPeriod, dstPeriod) * 2L);
            final long synodic = synodicPeriod(srcPeriod, dstPeriod);
            if (synodic > 0) horizon = Math.max(horizon, synodic * 2L);
            return Math.min(Math.max(horizon, 10_000L),
                    TrajectoryConfigurations.maxSharedMoonWaitMs());
        }

        return Math.min(Math.max(horizon, 10_000L),
                TrajectoryConfigurations.maxLaunchWaitMs());
    }

    private static double launchScore(final Trajectory trajectory,
                                       final long currentSim,
                                       final long departureTime,
                                       final CelestialBody source,
                                       final CelestialBody destination) {
        final Trajectory.Leg first = trajectory.getLegs().get(0);
        final double waitTime = Math.max(0.0, departureTime - currentSim);
        final double tangentMismatch = 1.0 - Math.max(-1.0, Math.min(1.0,
                dot(normalize(new Vec2(first.departureVelocityX, first.departureVelocityY)),
                    normalize(new Vec2(first.dxAt(0.0),          first.dyAt(0.0))))));
        // Lower is better. This balances patience against quality: the planner
        // may wait for a good phase, but waitTime keeps it from choosing absurd
        // future windows just to shave a tiny bit of fuel.
        return trajectory.fuelCost()
                + waitTime * TrajectoryConfigurations.balancedWaitCost()
                + tangentMismatch * TANGENT_MISMATCH_COST
                + sharedMoonPhasePenalty(source, destination, trajectory, currentSim, departureTime);
    }

    private static LaunchWindow launchWindowAt(final Ship ship,
                                                final long currentSim,
                                                final long departureTime) {
        final Vec2 position = ship.predictOrbitPosition(currentSim, departureTime);
        final Vec2 velocity = shipVelocity(ship, currentSim, departureTime);
        return new LaunchWindow(departureTime, position, velocity);
    }

    // -------------------------------------------------------------------------
    // Leg construction
    // -------------------------------------------------------------------------

    private static Trajectory.Leg buildRawLeg(final CelestialBody origin,
                                               final CelestialBody destination,
                                               final long departureTime,
                                               final long arrivalTime,
                                               final Vec2 departure,
                                               final Vec2 arrival,
                                               final Vec2 departureVelocity,
                                               final Vec2 arrivalVelocity,
                                               final double cruiseSpeed,
                                               final RouteStop stop) {
        final double chord  = Math.max(1.0, dist(departure, arrival));
        final double handle = chord * (stop.isFlyby ? FLYBY_HANDLE_FRACTION : DEFAULT_HANDLE_FRACTION);
        // Same Bezier-handle rule as buildTransferLeg: use velocity-aligned
        // handles, but only if they progress toward the endpoint.
        final Vec2 chordDir = normalize(new Vec2(arrival.x - departure.x, arrival.y - departure.y));
        final Vec2 startTangent = forwardTangent(departureVelocity, chordDir);
        final Vec2 endTangent = forwardTangent(arrivalVelocity, chordDir);
        final Vec2 c1 = new Vec2(departure.x + startTangent.x * handle,
                                  departure.y + startTangent.y * handle);
        final Vec2 c2 = new Vec2(arrival.x   - endTangent.x   * handle,
                                  arrival.y   - endTangent.y   * handle);
        final double fuelCost = chord * (stop.isFlyby ? 0.55 : 1.0)
                / Math.max(1.0, stop.speedMultiplier);
        return new Trajectory.Leg(origin, destination, departureTime, arrivalTime,
                departure.x, departure.y, arrival.x, arrival.y,
                c1.x, c1.y, c2.x, c2.y,
                startTangent.x, startTangent.y,
                endTangent.x,   endTangent.y,
                cruiseSpeed, fuelCost);
    }

    // -------------------------------------------------------------------------
    // Transfer timing
    // -------------------------------------------------------------------------

    private static long transferDuration(final CelestialBody source,
                                          final CelestialBody destination,
                                          final Vec2 departure,
                                          final long currentSim,
                                          final long departureTime,
                                          final double cruiseSpeed) {
        final CelestialBody focus = commonTransferFocus(source, destination);
        if (focus != null) {
            // Common-focus route: use the simplified Hohmann half-period.
            // Slower/faster ships scale this duration through speedScale.
            final Vec2 focusAt  = OrbitalMechanics.predict(focus, currentSim, departureTime);
            final double r1     = Math.max(1.0, dist(departure, focusAt));
            final double r2     = Math.max(1.0, orbitalRadiusAround(destination, focus));
            final double a      = (r1 + r2) * 0.5;
            final double mu     = focus.getVisualMu();
            final double speedScale = Math.max(0.25, cruiseSpeed / REFERENCE_CRUISE_SPEED);
            final double rawTime    = Math.PI * Math.sqrt(a * a * a / Math.max(1e-15, mu)) / speedScale;
            return (long) clamp(rawTime,
                    TrajectoryConfigurations.minTransferMs(),
                    TrajectoryConfigurations.maxTransferMs());
        }

        // No shared parent: fall back to distance / cruise speed. This is less
        // orbital-looking, but it gives the planner a deterministic duration.
        final double distance = dist(departure,
                OrbitalMechanics.predict(destination, currentSim, currentSim));
        return (long) clamp(distance / Math.max(0.001, cruiseSpeed),
                TrajectoryConfigurations.minTransferMs(),
                TrajectoryConfigurations.maxTransferMs());
    }

    // -------------------------------------------------------------------------
    // Approach geometry
    // -------------------------------------------------------------------------

    private static Vec2 parkingPoint(final Vec2 destCenter,
                                      final Vec2 destVelocity,
                                      final Vec2 departure,
                                      final CelestialBody destination,
                                      final double clearance) {
        final double parkingRadius = Math.max(
                destination.radius().doubleValue() + clearance,
                destination.radius().doubleValue() * PARKING_ORBIT_MULTIPLIER);

        // The ship should enter near a parking orbit, not the planet/moon
        // center. If the body has a known motion direction, choose the side
        // whose circular tangent matches that motion for a smoother insertion.
        Vec2 radial = normalize(new Vec2(departure.x - destCenter.x, departure.y - destCenter.y));
        if (length(destVelocity) > 1e-6) {
            final Vec2 motion = normalize(destVelocity);
            radial = new Vec2(motion.y, -motion.x); // perpendicular to motion
        }

        return new Vec2(destCenter.x + radial.x * parkingRadius,
                        destCenter.y + radial.y * parkingRadius);
    }

    static Vec2 flybyPoint(final Vec2 bodyCenter,
                            final Vec2 bodyVelocity,
                            final Vec2 approachFrom,
                            final CelestialBody body,
                            final double clearance) {
        final Vec2 inbound = normalize(new Vec2(bodyCenter.x - approachFrom.x,
                                                 bodyCenter.y - approachFrom.y));
        // Pick a point beside the body rather than on the centerline. Then
        // choose the side that is carried along by the body's own motion.
        Vec2 side = new Vec2(-inbound.y, inbound.x);
        if (length(bodyVelocity) > 1e-6 && dot(side, bodyVelocity) < 0) {
            side = new Vec2(-side.x, -side.y);
        }
        final double flybyRadius = body.radius().doubleValue()
                + Math.max(clearance, body.radius().doubleValue() * FLYBY_CLEARANCE_MULTIPLIER);
        return new Vec2(bodyCenter.x + side.x * flybyRadius,
                        bodyCenter.y + side.y * flybyRadius);
    }

    // -------------------------------------------------------------------------
    // Legacy flyby exit (used as fallback for non-Satellite bodies)
    // -------------------------------------------------------------------------

    private static Vec2 legacyFlybyExitVelocity(final Vec2 bodyVelocity,
                                                  final Vec2 departure,
                                                  final Vec2 flybyPoint,
                                                  final Vec2 inboundVelocity) {
        final Vec2 inbound     = normalize(new Vec2(flybyPoint.x - departure.x,
                                                    flybyPoint.y - departure.y));
        // Lightweight flyby exit used for RouteStop flybys. Explicit gravity
        // assists use chooseFlybyGeometry(), which performs body-frame velocity
        // bending. This older path just blends inbound motion with body motion.
        final Vec2 bodyMotion  = normalize(bodyVelocity);
        final Vec2 carried     = length(bodyVelocity) > 1e-6
                ? blend(inbound, bodyMotion, 0.45)
                : inbound;
        return dot(carried, inboundVelocity) < -0.4 ? inbound : carried;
    }

    // -------------------------------------------------------------------------
    // Shared-moon phase penalty
    // -------------------------------------------------------------------------

    private static double sharedMoonPhasePenalty(final CelestialBody source,
                                                   final CelestialBody destination,
                                                   final Trajectory trajectory,
                                                   final long currentSim,
                                                   final long departureTime) {
        if (!(source instanceof final Satellite srcMoon)
                || !(destination instanceof final Satellite dstMoon)
                || !srcMoon.getFocalPoint().equals(dstMoon.getFocalPoint())
                || trajectory.getLegs().isEmpty()) {
            return 0.0;
        }
        final CelestialBody parent    = srcMoon.getFocalPoint();
        final long transferTime       = Math.max(1L,
                trajectory.getLegs().get(0).arrivalSimTime
                        - trajectory.getLegs().get(0).departureSimTime);
        final double dstOmega         = angularVelocity(dstMoon.getOrbitalSpeed().longValue());
        final double idealPhase       = normalizePositive(Math.PI - dstOmega * transferTime);
        // actualPhase is destination angle minus source angle around their
        // shared parent at departure. The ideal phase means "destination will
        // have rotated into the transfer arc's arrival side by arrival time."
        final double actualPhase      = normalizePositive(
                bodyAngle(dstMoon, parent, currentSim, departureTime)
                        - bodyAngle(srcMoon, parent, currentSim, departureTime));
        final double error            = angularDistance(actualPhase, idealPhase);
        return error * error * SHARED_MOON_PHASE_COST;
    }

    private static double bodyAngle(final CelestialBody body,
                                     final CelestialBody parent,
                                     final long currentSim,
                                     final long simTime) {
        final Vec2 bodyAt   = OrbitalMechanics.predict(body,   currentSim, simTime);
        final Vec2 parentAt = OrbitalMechanics.predict(parent, currentSim, simTime);
        return Math.atan2(bodyAt.y - parentAt.y, bodyAt.x - parentAt.x);
    }

    // -------------------------------------------------------------------------
    // Hierarchy helpers
    // -------------------------------------------------------------------------

    static CelestialBody commonTransferFocus(final CelestialBody source,
                                              final CelestialBody destination) {
        if (source == null || destination == null) return null;
        final CelestialBody srcParent = parentOf(source);
        final CelestialBody dstParent = parentOf(destination);
        // Same parent: transfer in that parent's frame. Parent/child cases use
        // the parent body itself as the transfer focus.
        if (srcParent != null && srcParent.equals(dstParent)) return srcParent;
        if (source.equals(dstParent))      return source;
        if (destination.equals(srcParent)) return destination;
        return srcParent != null ? srcParent : dstParent;
    }

    private static CelestialBody parentOf(final CelestialBody body) {
        return body instanceof final Satellite sat ? sat.getFocalPoint() : null;
    }

    private static double orbitalRadiusAround(final CelestialBody body, final CelestialBody focus) {
        if (body instanceof final Satellite sat && sat.getFocalPoint().equals(focus)) {
            return sat.getOrbitalRadius().doubleValue();
        }
        final double dx = body.x().doubleValue() - focus.x().doubleValue();
        final double dy = body.y().doubleValue() - focus.y().doubleValue();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // -------------------------------------------------------------------------
    // Velocity helpers
    // -------------------------------------------------------------------------

    private static Vec2 shipVelocity(final Ship ship, final long currentSim, final long simTime) {
        if (ship == null || ship.getParentBody() == null) return new Vec2(0, 1);
        final long dt = 250L;
        final Vec2 now    = ship.predictOrbitPosition(currentSim, simTime);
        final Vec2 future = ship.predictOrbitPosition(currentSim, simTime + dt);
        return normalize(new Vec2(future.x - now.x, future.y - now.y));
    }

    static Vec2 bodyVelocityNorm(final CelestialBody body, final long currentSim, final long simTime) {
        final Vec2 raw = OrbitalMechanics.bodyVelocityRaw(body, currentSim, simTime);
        final double len = length(raw);
        if (len < 1e-12) return new Vec2(0, 1);
        return new Vec2(raw.x / len, raw.y / len);
    }

    // -------------------------------------------------------------------------
    // Math utilities
    // -------------------------------------------------------------------------

    static Vec2 normalize(final Vec2 v) {
        final double d = length(v);
        return d < 1e-9 ? new Vec2(0, 1) : new Vec2(v.x / d, v.y / d);
    }

    static double length(final Vec2 v) {
        return Math.sqrt(v.x * v.x + v.y * v.y);
    }

    static double dist(final Vec2 a, final Vec2 b) {
        return OrbitalMechanics.dist(a, b);
    }

    static double dot(final Vec2 a, final Vec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    /**
     * Rotate vector {@code v} by {@code angle} radians (counter-clockwise positive).
     */
    static Vec2 rotate(final Vec2 v, final double angle) {
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        return new Vec2(v.x * cos - v.y * sin, v.x * sin + v.y * cos);
    }

    private static Vec2 tangentToward(final Vec2 from, final Vec2 to, final Vec2 preferred) {
        final Vec2 chord = normalize(new Vec2(to.x - from.x, to.y - from.y));
        // If the preferred tangent points mostly away from the target, flip it
        // so the route does not start by reversing direction.
        return dot(chord, preferred) < -0.2 ? new Vec2(-preferred.x, -preferred.y) : preferred;
    }

    private static Vec2 forwardTangent(final Vec2 preferred, final Vec2 chordDir) {
        final Vec2 tangent = normalize(preferred);
        final double alignment = dot(tangent, chordDir);
        if (alignment >= 0.35) {
            return tangent;
        }
        if (alignment <= 0.0) {
            // Fully backward relative to the leg: use the chord direction. This
            // sacrifices some orbital-tangent purity to keep the preview sane.
            return chordDir;
        }
        // Partially misaligned: blend toward the chord to avoid hooks while
        // retaining a hint of the original velocity direction.
        return blendVec(tangent, chordDir, 0.65);
    }

    private static Vec2 blend(final Vec2 a, final Vec2 b, final double bWeight) {
        return blendVec(a, b, bWeight);
    }

    /** Package-private blend — returns the weighted blend of two vectors, normalized. */
    static Vec2 blendVec(final Vec2 a, final Vec2 b, final double bWeight) {
        final double aw = 1.0 - bWeight;
        return normalize(new Vec2(a.x * aw + b.x * bWeight, a.y * aw + b.y * bWeight));
    }

    private static long synodicPeriod(final long periodA, final long periodB) {
        final double omegaA = angularVelocity(periodA);
        final double omegaB = angularVelocity(periodB);
        final double rel    = Math.abs(omegaA - omegaB);
        return rel < 1e-9 ? 0L : (long) ((Math.PI * 2.0) / rel);
    }

    private static double angularVelocity(final long periodMs) {
        return periodMs > 0 ? (Math.PI * 2.0) / periodMs : 0.0;
    }

    private static double angularDistance(final double a, final double b) {
        return Math.abs(normalizeSigned(a - b));
    }

    private static double normalizePositive(final double angle) {
        double out = angle % (Math.PI * 2.0);
        return out < 0 ? out + Math.PI * 2.0 : out;
    }

    private static double normalizeSigned(final double angle) {
        double out = normalizePositive(angle);
        return out > Math.PI ? out - Math.PI * 2.0 : out;
    }

    static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    // -------------------------------------------------------------------------
    // Internal record
    // -------------------------------------------------------------------------

    private record LaunchWindow(long time, Vec2 position, Vec2 velocity) {}
}
