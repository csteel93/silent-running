package com.steel.silent.navigation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;

public class OrbitalMechanics {

    public static double calculateTransferTime(final Satellite source, final Satellite destination) {
        // System.out.println("Transfer time between " + source.name() + " and " +
        //         destination.name());

        final CelestialBody parent = source.getFocalPoint();

        final double sourceRads = source.getOrbitalRadius();
        final double destRads = destination.getOrbitalRadius();
        final double mu = parent.getMu();

        final double semiMajorAxis = (sourceRads + destRads) / 2.0;
        final double transferTimeSeconds = Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3.0) / mu);

        final boolean outward = destRads > sourceRads;

        // System.out.println("    parent: " + parent.name());
        // System.out.println("    source radius: " + sourceRads);
        // System.out.println("    destination radius: " + destRads);
        // System.out.println("    parent mu: " + mu);
        // System.out.println("    semi major axis: " + semiMajorAxis);
        // System.out.println("    transfer time: " + transferTimeSeconds);
        // System.out.println("    outward: " + outward);
        return transferTimeSeconds;
    }

    public static LaunchWindow findLaunchWindow(
            Satellite source,
            Satellite destination,
            double earliestLaunchTimeSeconds) {
        CelestialBody parent = source.getFocalPoint();

        double transferTimeSeconds = calculateTransferTime(source, destination);
        double destinationPeriodSeconds = (Math.PI * 2.0) / Math.abs(destination.getAngularVelocity());

        double searchStepSeconds = 3_600.0; // 1 hour
        double maxSearchTimeSeconds = destinationPeriodSeconds * 3.0;

        double toleranceRad = Math.toRadians(1.0);
        LaunchWindow best = null;
        double bestAbsError = Double.MAX_VALUE;

        double searchStart = earliestLaunchTimeSeconds;
        double searchEnd = earliestLaunchTimeSeconds + maxSearchTimeSeconds;

        for (double launchTime = searchStart; launchTime <= searchEnd; launchTime += searchStepSeconds) {

            double arrivalTime = launchTime + transferTimeSeconds;
            double sourceLaunchAngle = source.getOrbitAngleRad(launchTime);
            double expectedArrivalAngle = normalizeAngle(sourceLaunchAngle + Math.PI);
            double destinationArrivalAngle = destination.getOrbitAngleRad(arrivalTime);
            double phaseError = angleDifference(expectedArrivalAngle, destinationArrivalAngle);
            double absError = Math.abs(phaseError);

            LaunchWindow candidate = new LaunchWindow(
                    source,
                    destination,
                    parent,

                    launchTime,
                    arrivalTime,
                    transferTimeSeconds,

                    sourceLaunchAngle,
                    expectedArrivalAngle,
                    destinationArrivalAngle,

                    phaseError,
                    absError <= toleranceRad);

            if (absError < bestAbsError) {
                bestAbsError = absError;
                best = candidate;
            }

            if (candidate.isValid()) {
                return candidate;
            }
        }
        return best;
    }

    public static LaunchWindow findLaunchWindowStep(
            Satellite source,
            Satellite destination,
            double earliestLaunchTimeSeconds,
            double searchStepSeconds,
            double toleranceRad,
            double maxSearchPeriods) {
        CelestialBody parent = source.getFocalPoint();

        double transferTimeSeconds = calculateTransferTime(source, destination);
        double destinationPeriodSeconds = (Math.PI * 2.0) / Math.abs(destination.getAngularVelocity());

        double maxSearchTimeSeconds = destinationPeriodSeconds * maxSearchPeriods;

        LaunchWindow best = null;
        double bestAbsError = Double.MAX_VALUE;

        double searchStart = earliestLaunchTimeSeconds;
        double searchEnd = earliestLaunchTimeSeconds + maxSearchTimeSeconds;

        for (double launchTime = searchStart; launchTime <= searchEnd; launchTime += searchStepSeconds) {

            double arrivalTime = launchTime + transferTimeSeconds;
            double sourceLaunchAngle = source.getOrbitAngleRad(launchTime);
            double expectedArrivalAngle = normalizeAngle(sourceLaunchAngle + Math.PI);
            double destinationArrivalAngle = destination.getOrbitAngleRad(arrivalTime);
            double phaseError = angleDifference(expectedArrivalAngle, destinationArrivalAngle);
            double absError = Math.abs(phaseError);

            LaunchWindow candidate = new LaunchWindow(
                    source,
                    destination,
                    parent,

                    launchTime,
                    arrivalTime,
                    transferTimeSeconds,

                    sourceLaunchAngle,
                    expectedArrivalAngle,
                    destinationArrivalAngle,

                    phaseError,
                    absError <= toleranceRad);

            if (absError < bestAbsError) {
                bestAbsError = absError;
                best = candidate;
            }

            if (candidate.isValid()) {
                return candidate;
            }
        }
        return best;
    }

    public static LaunchWindow findLaunchWindowWithStepForDuration(
            Satellite source,
            Satellite destination,
            double searchStartSeconds,
            double searchDurationSeconds,
            double searchStepSeconds,
            double toleranceRad) {
        CelestialBody parent = source.getFocalPoint();

        double transferTimeSeconds = calculateTransferTime(source, destination);

        LaunchWindow best = null;
        double bestAbsError = Double.MAX_VALUE;

        double searchEndSeconds = searchStartSeconds + searchDurationSeconds;

        for (double launchTime = searchStartSeconds; launchTime <= searchEndSeconds; launchTime += searchStepSeconds) {
            double arrivalTime = launchTime + transferTimeSeconds;

            double sourceLaunchAngle = source.getOrbitAngleRad(launchTime);

            double expectedArrivalAngle = normalizeAngle(sourceLaunchAngle + Math.PI);

            double destinationArrivalAngle = destination.getOrbitAngleRad(arrivalTime);

            double phaseError = angleDifference(expectedArrivalAngle, destinationArrivalAngle);

            double absError = Math.abs(phaseError);

            LaunchWindow candidate = new LaunchWindow(
                    source,
                    destination,
                    parent,
                    launchTime,
                    arrivalTime,
                    transferTimeSeconds,
                    sourceLaunchAngle,
                    expectedArrivalAngle,
                    destinationArrivalAngle,
                    phaseError,
                    absError <= toleranceRad);

            if (absError < bestAbsError) {
                bestAbsError = absError;
                best = candidate;
            }

            if (candidate.isValid()) {
                return candidate;
            }
        }

        return best;
    }

    public static LaunchWindow refineLaunchWindowAround(
            Satellite source,
            Satellite destination,
            double roughLaunchTimeSeconds) {
        double refineWindowSeconds = 20.0 * 86_400.0; // 20 days total
        double searchStepSeconds = 300.0; // 5 minutes
        double toleranceRad = Math.toRadians(0.25);

        double searchStart = roughLaunchTimeSeconds - refineWindowSeconds * 0.5;
        double searchEnd = roughLaunchTimeSeconds + refineWindowSeconds * 0.5;

        double transferTimeSeconds = calculateTransferTime(source, destination);

        LaunchWindow best = null;
        double bestAbsError = Double.MAX_VALUE;

        for (double launchTime = searchStart; launchTime <= searchEnd; launchTime += searchStepSeconds) {

            double arrivalTime = launchTime + transferTimeSeconds;

            double sourceLaunchAngle = source.getOrbitAngleRad(launchTime);

            double expectedArrivalAngle = normalizeAngle(sourceLaunchAngle + Math.PI);

            double destinationArrivalAngle = destination.getOrbitAngleRad(arrivalTime);

            double phaseError = angleDifference(expectedArrivalAngle, destinationArrivalAngle);

            double absError = Math.abs(phaseError);

            LaunchWindow candidate = new LaunchWindow(
                    source,
                    destination,
                    source.getFocalPoint(),
                    launchTime,
                    arrivalTime,
                    transferTimeSeconds,
                    sourceLaunchAngle,
                    expectedArrivalAngle,
                    destinationArrivalAngle,
                    phaseError,
                    absError <= toleranceRad);

            if (absError < bestAbsError) {
                bestAbsError = absError;
                best = candidate;
            }
        }

        return best;
    }

    public static LaunchWindow findLaunchWindowTwoPass(
            Satellite source,
            Satellite destination,
            double earliestLaunchTimeSeconds) {

        LaunchWindow rough = findLaunchWindowStep(
                source,
                destination,
                earliestLaunchTimeSeconds,
                86_400.0, // 1 day
                Math.toRadians(5.0),
                10.0);

        double refineStart = Math.max(earliestLaunchTimeSeconds, rough.getLaunchTime() - 5.0 * 86_400.0);

        // return findLaunchWindowWithStepForDuration(
        //         source,
        //         destination,
        //         refineStart,
        //         10.0 * 86_400.0, // search 10 days around rough result
        //         300.0, // 5 minute step
        //         Math.toRadians(0.25));
        return refineLaunchWindowAround(source, destination, rough.getLaunchTime());
    }

    public static double normalizeAngle(final double angle) {
        final double twoPi = MathUtils.PI2;
        double newAngle = angle % twoPi;
        if (newAngle < 0) {
            newAngle += twoPi;
        }
        return newAngle;
    }

    // public static float expectedArrivalAngle(final CelestialBody source,
    // final float launchTime) {
    // return normalizeAngle(launchTime);
    // }

    public static double angleDifference(final double angle1, final double angle2) {
        double difference = normalizeAngle(angle1 - angle2);
        if (difference > MathUtils.PI) {
            difference -= MathUtils.PI2;
        }
        return difference;
    }

    // public static HohmannDebugInfo buildHohmannDebugInfo(
    // LaunchWindow window,
    // float rawTime) {
    // float currentTime = rawTime/1000f;
    // System.out.println("Hohmann Calculations: ");
    // System.out.println(" raw time: " + rawTime);
    // System.out.println(" current time: " + currentTime);

    // Satellite source = window.source;
    // Satellite destination = window.destination;
    // CelestialBody parent = window.parent;

    // HohmannDebugInfo debug = new HohmannDebugInfo();
    // debug.window = window;

    // System.out.println(" source.getPosition " + currentTime);
    // debug.sourceCurrentPosition = source.getPosition(currentTime);
    // System.out.println(" source current position: " +
    // debug.sourceCurrentPosition.x + " " + debug.sourceCurrentPosition.y);

    // System.out.println(" destination.getPosition " + currentTime);
    // debug.destinationCurrentPosition = destination.getPosition(currentTime);
    // System.out.println(" destination current position: " +
    // debug.destinationCurrentPosition.x + " " +
    // debug.destinationCurrentPosition.y);

    // System.out.println(" source.getPosition " + window.launchTime);
    // debug.sourceLaunchPosition = source.getPosition(window.launchTime);
    // System.out.println(" source launch position: " + debug.sourceLaunchPosition.x
    // + " " + debug.sourceLaunchPosition.y);

    // System.out.println(" destination.getPosition " + window.launchTime);
    // debug.destinationArrivalPosition =
    // destination.getPosition(window.arrivalTime);
    // System.out.println(" destination arrival position: " +
    // debug.destinationArrivalPosition.x + " " +
    // debug.destinationArrivalPosition.y);

    // System.out.println(" parent.getPosition " + window.launchTime);
    // debug.parentLaunchPosition = parent.getPosition( window.launchTime );
    // System.out.println(" parent launch position: " + debug.parentLaunchPosition.x
    // + " " + debug.parentLaunchPosition.y);

    // System.out.println(" parent.getPosition " + window.launchTime);
    // debug.parentArrivalPosition = parent.getPosition( window.arrivalTime);
    // System.out.println(" parent arrival position: " +
    // debug.parentArrivalPosition.x + " " + debug.parentArrivalPosition.y);

    // debug.sourceLaunchVelocity = source.getVelocity( window.launchTime);

    // debug.destinationArrivalVelocity = destination.getVelocity(
    // window.arrivalTime);

    // debug.expectedArrivalPosition = calculateExpectedArrivalPosition(window);

    // buildTransferCurvePoints(debug);

    // return debug;
    // }

    // public static void buildTransferCurvePoints(HohmannDebugInfo debug) {
    // Vector2 p0 = debug.sourceLaunchPosition;
    // Vector2 p3 = debug.destinationArrivalPosition;

    // Vector2 startDirection = new Vector2(debug.sourceLaunchVelocity).nor();

    // Vector2 endDirection = new Vector2(debug.destinationArrivalVelocity).nor();

    // float distance = p0.dst(p3);
    // float controlDistance = distance * 0.4f;

    // Vector2 p1 = new Vector2(p0).mulAdd(startDirection, controlDistance);
    // Vector2 p2 = new Vector2(p3).mulAdd(endDirection, -controlDistance);

    // int samples = 100;

    // debug.transferCurvePoints.clear();

    // for (int i = 0; i <= samples; i++) {
    // float t = i / (float) samples;

    // Vector2 point = cubicBezier(p0, p1, p2, p3, t);

    // debug.transferCurvePoints.add(point);
    // }
    // }

    // public static Vector2 calculateExpectedArrivalPosition(LaunchWindow window) {
    // Satellite destination = window.destination;
    // CelestialBody parent = window.parent;

    // Vector2 parentArrivalPosition = parent.getPosition((long)
    // window.arrivalTime);
    // System.out.println(" parent arrival position: " + parentArrivalPosition.x + "
    // " + parentArrivalPosition.y);

    // float expectedAngle = window.expectedArrivalAngle;

    // Vector2 localArrivalOffset = new Vector2(
    // MathUtils.cos(expectedAngle) * destination.getOrbitalRadius().floatValue(),
    // MathUtils.sin(expectedAngle) * destination.getOrbitalRadius().floatValue());

    // return parentArrivalPosition.add(localArrivalOffset);
    // }

    // public static Vector2 cubicBezier(
    // Vector2 p0,
    // Vector2 p1,
    // Vector2 p2,
    // Vector2 p3,
    // float t) {
    // float u = 1f - t;

    // Vector2 result = new Vector2();

    // result.mulAdd(p0, u * u * u);
    // result.mulAdd(p1, 3f * u * u * t);
    // result.mulAdd(p2, 3f * u * t * t);
    // result.mulAdd(p3, t * t * t);

    // return result;
    // }

}
