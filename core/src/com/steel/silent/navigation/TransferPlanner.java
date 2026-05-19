package com.steel.silent.navigation;

import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.model.orbit.OrbitState;

import static com.steel.silent.math.Angles.normalizeRadians;
import static com.steel.silent.math.Angles.signedAangleDifferenceRadians;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified patched-conic-style transfer planner.
 *
 * <p>All calculations use real SI units: metres, seconds, and m³/s² for μ.
 * This is gameplay-grade orbit planning using Keplerian two-body approximations
 * inside a body's sphere of influence.  Full n-body physics and proper
 * delta-v accounting are intentionally out of scope.
 *
 * <h3>Supported transfer types</h3>
 * <ol>
 *   <li>{@link #planetParkingOrbitToMoon} — circular parking orbit around a
 *       planet to a moon of that planet (single leg in the planet's frame).</li>
 *   <li>{@link #moonToPlanetParkingOrbit} — moon to a circular parking orbit
 *       around its parent planet (single leg in the planet's frame).</li>
 *   <li>{@link #moonToOtherPlanet} — moon of planet A to planet B; implemented
 *       as three legs: moon → departure orbit, interplanetary Hohmann,
 *       arrival/capture stub.</li>
 * </ol>
 */
public class TransferPlanner {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Number of arc samples used when building route-point curves. */
    private static final int ROUTE_SAMPLES = 160;

    /** Coarse search step for launch-window scanning. */
    private static final double COARSE_STEP_SECONDS = 3_600.0;   // 1 hour

    /** Fine search step for launch-window refinement. */
    private static final double FINE_STEP_SECONDS = 300.0;       // 5 minutes

    /** Phase-error tolerance considered a valid launch window. */
    private static final double TOLERANCE_RAD = Math.toRadians(1.0);

    /** Seconds per day, used only for debug log formatting. */
    private static final double SECONDS_PER_DAY = 86_400.0;

    // =========================================================================
    // Type 1 — Planet parking orbit → Moon
    // =========================================================================

    /**
     * Plans a Hohmann transfer from a circular parking orbit around {@code planet}
     * to the orbit of {@code moon}.
     *
     * <p>The entire calculation is performed in the planet's local frame
     * (patched-conic approximation).  The ship's angle in the parking orbit is
     * tracked deterministically from {@code shipAngleRadAtEpoch} using the
     * circular-orbit angular velocity; the search finds the launch time at which
     * the moon will be at the diametrically opposite point when the ship arrives.
     *
     * @param planet                  the planet the ship is currently orbiting;
     *                                must be a {@link Satellite} (i.e. it orbits
     *                                some central star)
     * @param parkingAltitudeMeters   altitude of the parking orbit above the
     *                                planet surface in metres
     * @param shipAngleRadAtEpoch     the ship's orbital angle (radians, in the
     *                                planet's local frame) at simulation time t = 0
     * @param moon                    target moon — must satisfy
     *                                {@code moon.getFocalPoint() == planet}
     * @param earliestLaunchTimeSeconds start of the launch-window search in seconds
     * @return a {@link TransferRoute} containing one {@link TransferLeg}
     * @throws IllegalArgumentException if {@code moon} does not orbit {@code planet}
     */
    public static TransferRoute planetParkingOrbitToMoon(
            Satellite planet,
            double parkingAltitudeMeters,
            double shipAngleRadAtEpoch,
            Satellite moon,
            double earliestLaunchTimeSeconds) {

        if (!moon.primary().id().equals(planet.id())) {
            throw new IllegalArgumentException(
                    "planetParkingOrbitToMoon: '" + moon.name()
                    + "' does not orbit '" + planet.name() + "'");
        }

        TransferLeg leg = buildParkingOrbitToMoonLeg(
                planet, parkingAltitudeMeters, shipAngleRadAtEpoch,
                moon, earliestLaunchTimeSeconds);

        debugLogLeg(leg);
        return new TransferRoute(List.of(leg));
    }

    // =========================================================================
    // Type 2 — Moon → Planet parking orbit
    // =========================================================================

    /**
     * Plans a Hohmann transfer from a moon's orbit to a circular parking orbit
     * around its parent planet.
     *
     * <p>The simplified Hohmann model means any launch time is valid — the ship
     * departs from the moon at {@code launchTimeSeconds} and arrives at the
     * diametrically opposite point on the parking orbit
     * ({@code moonLaunchAngle + π}).  No phase-matching search is required.
     *
     * @param moon                   the departing moon — must satisfy
     *                               {@code moon.getFocalPoint() == planet}
     * @param planet                 the destination planet
     * @param parkingAltitudeMeters  altitude of the arrival parking orbit in metres
     * @param launchTimeSeconds      departure time in seconds
     * @return a {@link TransferRoute} containing one {@link TransferLeg}
     * @throws IllegalArgumentException if {@code moon} does not orbit {@code planet}
     */
    public static TransferRoute moonToPlanetParkingOrbit(
            Satellite moon,
            Satellite planet,
            double parkingAltitudeMeters,
            double launchTimeSeconds) {

        if (!moon.primary().id().equals(planet.id())) {
            throw new IllegalArgumentException(
                    "moonToPlanetParkingOrbit: '" + moon.name()
                    + "' does not orbit '" + planet.name() + "'");
        }

        TransferLeg leg = buildMoonToParkingOrbitLeg(
                moon, planet, parkingAltitudeMeters, launchTimeSeconds);

        debugLogLeg(leg);
        return new TransferRoute(List.of(leg));
    }

    // =========================================================================
    // Type 3 — Moon of planet A → Planet B (multi-leg)
    // =========================================================================

    /**
     * Plans a multi-leg route from a moon of one planet to another planet.
     *
     * <p>The route consists of three {@link TransferLeg}s:
     * <ol>
     *   <li><b>Leg 1 — escape:</b> moon → source planet departure orbit, calculated
     *       in the source planet's local frame.</li>
     *   <li><b>Leg 2 — interplanetary:</b> source planet → destination planet
     *       Hohmann transfer in the heliocentric frame.  The search window opens
     *       at the arrival time of Leg 1.</li>
     *   <li><b>Leg 3 — capture stub:</b> an arrival marker at the destination
     *       planet.  Full capture mechanics (hyperbolic insertion, aerobraking,
     *       etc.) are not modelled.</li>
     * </ol>
     *
     * <p>This is a simplified patched-conic approach — sphere-of-influence
     * transitions and escape delta-v are not computed with full fidelity.
     *
     * @param moon                    the departing moon
     * @param sourcePlanet            the planet {@code moon} orbits; must share a
     *                                common parent with {@code destinationPlanet}
     * @param parkingAltitudeMeters   altitude of the intermediate departure orbit
     *                                in metres
     * @param destinationPlanet       the target planet; must orbit the same central
     *                                body as {@code sourcePlanet}
     * @param earliestLaunchTimeSeconds start of the search window in seconds
     * @return a {@link TransferRoute} containing three ordered {@link TransferLeg}s
     * @throws IllegalArgumentException if the body relationships are invalid
     */
    public static TransferRoute moonToOtherPlanet(
            Satellite moon,
            Satellite sourcePlanet,
            double parkingAltitudeMeters,
            Satellite destinationPlanet,
            double earliestLaunchTimeSeconds) {

        // Validation -----------------------------------------------------------
        if (!moon.primary().id().equals(sourcePlanet.id())) {
            throw new IllegalArgumentException(
                    "moonToOtherPlanet: '" + moon.name()
                    + "' does not orbit source planet '" + sourcePlanet.name() + "'");
        }
        if (destinationPlanet.id().equals(sourcePlanet.id())) {
            throw new IllegalArgumentException(
                    "moonToOtherPlanet: source and destination are the same planet ('"
                    + sourcePlanet.name() + "')");
        }
        if (!sourcePlanet.primary().id().equals(destinationPlanet.primary().id())) {
            throw new IllegalArgumentException(
                    "moonToOtherPlanet: '" + sourcePlanet.name() + "' and '"
                    + destinationPlanet.name()
                    + "' do not share a common parent body");
        }

        // Leg 1 — Moon → source planet departure orbit ------------------------
        TransferLeg leg1 = buildMoonToParkingOrbitLeg(
                moon, sourcePlanet, parkingAltitudeMeters, earliestLaunchTimeSeconds);

        // Leg 2 — Interplanetary Hohmann (heliocentric) -----------------------
        // Search opens at the arrival of Leg 1 so the ship has already escaped.
        double leg2EarliestLaunch = leg1.arrivalTimeSeconds();

        LaunchWindow helioWindow = OrbitalMechanics.findLaunchWindowTwoPass(
                sourcePlanet, destinationPlanet, leg2EarliestLaunch);

        double leg2Launch    = helioWindow.getLaunchTime();
        double leg2Arrival   = helioWindow.getArrivalTime();
        double leg2Transfer  = helioWindow.getTransferTime();

        List<Vector> leg2Points = HohmannDrawData.buildHohmannRoutePointsMeters(helioWindow);

        CelestialBody sunFrame = sourcePlanet.primary();
        TransferLeg leg2 = new TransferLeg(
                sunFrame,
                sourcePlanet,
                destinationPlanet,
                sourcePlanet.orbit().radiusMeters(),
                destinationPlanet.orbit().radiusMeters(),
                leg2Launch, leg2Arrival, leg2Transfer,
                leg2Points,
                sourcePlanet.name() + " → " + destinationPlanet.name()
                        + " (heliocentric Hohmann)");

        // Leg 3 — Destination planet arrival stub -----------------------------
        Vector arrivalMarker = positionMeters(destinationPlanet, leg2Arrival);
        TransferLeg leg3 = new TransferLeg(
                destinationPlanet,
                null,
                destinationPlanet,
                0.0,
                destinationPlanet.radiusMeters() + parkingAltitudeMeters,
                leg2Arrival, leg2Arrival, 0.0,
                List.of(arrivalMarker),
                "Arrival at " + destinationPlanet.name() + " (capture stub)");

        debugLogLeg(leg1);
        debugLogLeg(leg2);
        debugLogLeg(leg3);
        return new TransferRoute(List.of(leg1, leg2, leg3));
    }

    // =========================================================================
    // Private leg builders (no logging — callers handle that)
    // =========================================================================

    /**
     * Builds the {@link TransferLeg} for a parking-orbit-to-moon transfer without
     * logging.  Called by {@link #planetParkingOrbitToMoon} and indirectly by
     * {@link #moonToOtherPlanet}.
     *
     * <p>Launch-window search strategy:
     * <ol>
     *   <li>Coarse pass at 1-hour steps over up to 3 moon orbital periods.</li>
     *   <li>Fine pass at 5-minute steps over a ±2-day window around the coarse best.</li>
     * </ol>
     */
    private static TransferLeg buildParkingOrbitToMoonLeg(
            Satellite planet,
            double parkingAltitudeMeters,
            double shipAngleRadAtEpoch,
            Satellite moon,
            double earliestLaunchTimeSeconds) {

        double r1 = planet.radiusMeters() + parkingAltitudeMeters;
        double r2 = moon.orbit().radiusMeters();
        double mu = planet.mu();
        boolean outward = r2 > r1;

        double transferTime = OrbitalMechanics.calculateHohmannTransferTimeSeconds(r1, r2, mu);

        // Angular velocity of the ship in its circular parking orbit (rad/s).
        // v_circ = sqrt(mu / r1^3) for a circular orbit.
        double omegaShip = Math.sqrt(mu / (r1 * r1 * r1));

        // Bound the search to 3 moon periods so we don't run forever.
        double moonPeriodSeconds = moon.orbit().periodSeconds();
        double maxSearchDuration = moonPeriodSeconds * 3.0;
        double searchEnd = earliestLaunchTimeSeconds + maxSearchDuration;

        // ---- Coarse pass ----------------------------------------------------
        double bestAbsError = Double.MAX_VALUE;
        double bestLaunchTime = earliestLaunchTimeSeconds;

        for (double t = earliestLaunchTimeSeconds; t <= searchEnd; t += COARSE_STEP_SECONDS) {
            double absError = parkingOrbitPhaseError(
                    t, shipAngleRadAtEpoch, omegaShip, transferTime, moon);
            if (absError < bestAbsError) {
                bestAbsError = absError;
                bestLaunchTime = t;
                if (absError <= TOLERANCE_RAD) {
                    break;
                }
            }
        }

        // ---- Fine pass — ±2-day window around the coarse best ---------------
        double refineStart = Math.max(earliestLaunchTimeSeconds,
                bestLaunchTime - 2.0 * SECONDS_PER_DAY);
        double refineEnd   = bestLaunchTime + 2.0 * SECONDS_PER_DAY;

        for (double t = refineStart; t <= refineEnd; t += FINE_STEP_SECONDS) {
            double absError = parkingOrbitPhaseError(
                    t, shipAngleRadAtEpoch, omegaShip, transferTime, moon);
            if (absError < bestAbsError) {
                bestAbsError = absError;
                bestLaunchTime = t;
            }
        }

        double launchAngle  = normalizeRadians(
                shipAngleRadAtEpoch + omegaShip * bestLaunchTime);
        double arrivalTime  = bestLaunchTime + transferTime;

        List<Vector> routePoints = buildLocalHohmannRoutePoints(
                planet, r1, r2, launchAngle,
                bestLaunchTime, transferTime, outward);

        return new TransferLeg(
                planet,
                null,   // source is a parking orbit, not a named body
                moon,
                r1, r2,
                bestLaunchTime, arrivalTime, transferTime,
                routePoints,
                "Parking orbit → " + moon.name()
                        + " (in " + planet.name() + " frame)");
    }

    /**
     * Phase error (radians, absolute value) for a parking-orbit departure at time
     * {@code launchTime}.  Used by the launch-window search in
     * {@link #buildParkingOrbitToMoonLeg}.
     */
    private static double parkingOrbitPhaseError(
            double launchTime,
            double shipAngleAtEpoch,
            double omegaShip,
            double transferTime,
            Satellite moon) {

        double shipAngle            = normalizeRadians(
                shipAngleAtEpoch + omegaShip * launchTime);
        double expectedArrivalAngle = normalizeRadians(shipAngle + Math.PI);
        double moonArrivalAngle     = orbitAngleInPrimaryFrame(moon, launchTime + transferTime);
        double phaseError           = signedAangleDifferenceRadians(
                expectedArrivalAngle, moonArrivalAngle);
        return Math.abs(phaseError);
    }

    /**
     * Builds the {@link TransferLeg} for a moon-to-parking-orbit transfer without
     * logging.
     *
     * <p>No phase-matching search is needed: any departure time is valid.  The
     * ship launches from wherever the moon is and arrives at the parking orbit
     * point that is π radians (half an orbit) away from the departure direction —
     * the standard Hohmann periapsis.
     */
    private static TransferLeg buildMoonToParkingOrbitLeg(
            Satellite moon,
            Satellite planet,
            double parkingAltitudeMeters,
            double launchTimeSeconds) {

        double r1 = moon.orbit().radiusMeters();
        double r2 = planet.radiusMeters() + parkingAltitudeMeters;
        double mu = planet.mu();
        // Moon orbit > parking orbit, so this is always an inward (descending) transfer.
        boolean outward = r2 > r1;

        double transferTime = OrbitalMechanics.calculateHohmannTransferTimeSeconds(r1, r2, mu);
        double arrivalTime  = launchTimeSeconds + transferTime;

        // Launch angle comes from the moon's actual position in the planet's frame.
        double moonLaunchAngle = orbitAngleInPrimaryFrame(moon, launchTimeSeconds);

        List<Vector> routePoints = buildLocalHohmannRoutePoints(
                planet, r1, r2, moonLaunchAngle,
                launchTimeSeconds, transferTime, outward);

        return new TransferLeg(
                planet,
                moon,
                null,   // destination is a parking orbit, not a named body
                r1, r2,
                launchTimeSeconds, arrivalTime, transferTime,
                routePoints,
                moon.name() + " → parking orbit"
                        + " (in " + planet.name() + " frame)");
    }

    // =========================================================================
    // Route-point generation
    // =========================================================================

    /**
     * Builds world-space route points for a Hohmann transfer arc within the local
     * frame of {@code frameBody}.
     *
     * <p>The arc is sampled using the standard polar equation of a Keplerian
     * ellipse.  {@code frameBody.getWorldPositionMeters(t)} is added at each
     * sample to convert local-frame coordinates to world space, so the arc
     * correctly follows the moving frame body (e.g. a planet orbiting the Sun).
     *
     * @param frameBody           central body around which the transfer occurs
     * @param r1                  departure orbit radius in metres
     * @param r2                  arrival orbit radius in metres
     * @param launchAngle         departure angle in the frame body's local frame (radians)
     * @param launchTimeSeconds   absolute simulation time of departure
     * @param transferTimeSeconds duration of the transfer
     * @param outward             {@code true} if {@code r2 > r1} (ascending transfer)
     * @return world-space arc points as {@link Vector} values in metres
     */
    public static List<Vector> buildLocalHohmannRoutePoints(
            CelestialBody frameBody,
            double r1, double r2,
            double launchAngle,
            double launchTimeSeconds,
            double transferTimeSeconds,
            boolean outward) {

        double semiMajorAxis   = (r1 + r2) * 0.5;
        double eccentricity    = Math.abs(r2 - r1) / (r1 + r2);
        double semiLatusRectum = semiMajorAxis * (1.0 - eccentricity * eccentricity);

        List<Vector> points = new ArrayList<>(ROUTE_SAMPLES + 1);

        for (int i = 0; i <= ROUTE_SAMPLES; i++) {
            double t = i / (double) ROUTE_SAMPLES;

            // For an outward (ascending) transfer the true anomaly runs 0 → π.
            // For an inward (descending) transfer it runs π → 0 so the curve
            // starts at the source (apoapsis) and ends at the periapsis.
            double trueAnomaly = outward
                    ? t * Math.PI
                    : Math.PI - t * Math.PI;

            double r = semiLatusRectum / (1.0 + eccentricity * Math.cos(trueAnomaly));

            // Local-frame angle of the sample point.
            // Outward:  launchAngle + θ  (starts at launchAngle, ends at launchAngle + π)
            // Inward:   launchAngle + θ − π  (same endpoints, reversed direction)
            double worldAngle = outward
                    ? launchAngle + trueAnomaly
                    : launchAngle + trueAnomaly - Math.PI;

            double sampleTime = launchTimeSeconds + t * transferTimeSeconds;
            Vector  framePos   = positionMeters(frameBody, sampleTime);

            points.add(new Vector(
                    framePos.x() + r * Math.cos(worldAngle),
                    framePos.y() + r * Math.sin(worldAngle)));
        }

        return points;
    }

    private static Vector positionMeters(final CelestialBody body, final double timeSeconds) {
        return orbitStateFor(body, timeSeconds).positionMeters();
    }

    private static OrbitState orbitStateFor(final CelestialBody body, final double timeSeconds) {
        if (body instanceof Satellite satellite) {
            return satellite.orbit().stateAt(timeSeconds);
        }
        return OrbitState.stationary(body.initialPositionMeters());
    }

    private static double orbitAngleInPrimaryFrame(final Satellite satellite, final double timeSeconds) {
        final Vector satellitePosition = satellite.orbit().stateAt(timeSeconds).positionMeters();
        final Vector primaryPosition = positionMeters(satellite.primary(), timeSeconds);
        final Vector localPosition = satellitePosition.sub(primaryPosition);
        return normalizeRadians(Math.atan2(localPosition.y(), localPosition.x()));
    }

    // =========================================================================
    // Debug logging
    // =========================================================================

    private static void debugLogLeg(TransferLeg leg) {
        System.out.printf(
                "[TransferLeg] %s%n"
                + "  frame:    %s%n"
                + "  source:   %s%n"
                + "  dest:     %s%n"
                + "  r1:       %.3e m%n"
                + "  r2:       %.3e m%n"
                + "  launch:   %.2f days%n"
                + "  arrival:  %.2f days%n"
                + "  transit:  %.2f days%n",
                leg.description(),
                leg.centralBody()       != null ? leg.centralBody().name()       : "—",
                leg.sourceBody()      != null ? leg.sourceBody().name()      : "(parking orbit)",
                leg.destinationBody() != null ? leg.destinationBody().name() : "(parking orbit)",
                leg.startRadiusMeters(),
                leg.endRadiusMeters(),
                leg.launchTimeSeconds()    / SECONDS_PER_DAY,
                leg.arrivalTimeSeconds()   / SECONDS_PER_DAY,
                leg.transferTimeSeconds()  / SECONDS_PER_DAY);
    }
}
