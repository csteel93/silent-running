package com.steel.silent.navigation;

import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;

import java.util.List;

/**
 * One leg of a multi-leg transfer route.
 *
 * <p>All distances are in real metres and all times are in real seconds.
 * This is a simplified patched-conic-style gameplay record — not a full
 * n-body physics solution.
 *
 * @param centralBody           the central body that defines the orbital frame for this leg
 *                            (e.g. Earth for a moon-to-parking-orbit leg, the Sun for
 *                            an interplanetary leg)
 * @param sourceBody          the named body the ship departs from, or {@code null} if the
 *                            source is a synthetic orbit (e.g. a parking orbit)
 * @param destinationBody     the named body the ship is heading to, or {@code null} if the
 *                            destination is a synthetic orbit (e.g. a parking orbit)
 * @param startRadiusMeters   orbital radius of the departure point in metres
 * @param endRadiusMeters     orbital radius of the arrival point in metres
 * @param launchTimeSeconds   absolute simulation time of departure in seconds
 * @param arrivalTimeSeconds  absolute simulation time of arrival in seconds
 * @param transferTimeSeconds duration of the transfer in seconds
 * @param routePointsMeters   world-space curve points for drawing the transfer arc
 * @param description         human-readable summary of this leg
 */
public record TransferLeg(
        CelestialBody centralBody,
        CelestialBody sourceBody,
        CelestialBody destinationBody,
        double startRadiusMeters,
        double endRadiusMeters,
        double launchTimeSeconds,
        double arrivalTimeSeconds,
        double transferTimeSeconds,
        List<Vector> routePointsMeters,
        String description
) {}
