package com.steel.silent.navigation;

import java.util.ArrayList;
import java.util.List;

import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.model.orbit.OrbitState;

public record HohmannDrawData(
           Vector sourceLaunchPosMeters,
        Vector destinationArrivalPosMeters,
        Vector expectedArrivalPosMeters,
        Vector parentPosAtLaunchMeters,
        Vector parentPosAtArrivalMeters,
        List<Vector> transferPointsMeters) {

  public static HohmannDrawData buildHohmannDrawData(LaunchWindow window) {
    CelestialBody source = window.getSource();
    CelestialBody destination = window.getDestination();
    CelestialBody parent = window.getParent();

    double launchTime = window.getLaunchTime();
    double arrivalTime = window.getArrivalTime();

    Vector sourceLaunchPos =
            positionMeters(source, launchTime);

    Vector destinationArrivalPos =
            positionMeters(destination, arrivalTime);

    Vector parentLaunchPos =
            positionMeters(parent, launchTime);

    Vector parentArrivalPos =
            positionMeters(parent, arrivalTime);

    Vector expectedArrivalPos =
            calculateExpectedArrivalPositionMeters(window);

    List<Vector> transferPoints =
            buildHohmannRoutePointsMeters(
                    window
            );

    return new HohmannDrawData(
            sourceLaunchPos,
            destinationArrivalPos,
            expectedArrivalPos,
            parentLaunchPos,
            parentArrivalPos,
            transferPoints
    );
}

    public static List<Vector> buildHohmannRoutePointsMeters(final LaunchWindow window) {
        Satellite source = window.getSource();
        Satellite destination = window.getDestination();
        CelestialBody parent = window.getParent();
        double sourceRadius = source.orbit().radiusMeters();
        double destinationRadius = destination.orbit().radiusMeters();
        double semiMajorAxis = (sourceRadius + destinationRadius) * 0.5;
        double eccentricity = Math.abs(destinationRadius - sourceRadius)
                / (sourceRadius + destinationRadius);
        double semiLatusRectum = semiMajorAxis * (1.0 - eccentricity * eccentricity);
        boolean outward = destinationRadius > sourceRadius;

        int samples = 160;
        List<Vector> points = new ArrayList<>(samples + 1);

        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double trueAnomaly = outward
                    ? t * Math.PI
                    : Math.PI - t * Math.PI;
            double transferRadius = semiLatusRectum
                    / (1.0 + eccentricity * Math.cos(trueAnomaly));
            double worldAngle = outward
                    ? window.getSourceLaunchAngle() + trueAnomaly
                    : window.getSourceLaunchAngle() + trueAnomaly - Math.PI;
            double sampleTime = window.getLaunchTime() + t * window.getTransferTime();
            Vector parentPosition = positionMeters(parent, sampleTime);
            points.add(new Vector(
                    parentPosition.x() + transferRadius * Math.cos(worldAngle),
                    parentPosition.y() + transferRadius * Math.sin(worldAngle)));
        }

        return points;
    }

    public static Vector calculateExpectedArrivalPositionMeters(
            LaunchWindow window) {
        Satellite destination = window.getDestination();
        CelestialBody parent = window.getParent();

        Vector parentPositionAtArrival = positionMeters(parent, window.getArrivalTime());

        double angle = window.getExpectedArrivalAngle();

        Vector localArrivalOffset = new Vector(
                Math.cos(angle) * destination.orbit().radiusMeters(),
                Math.sin(angle) * destination.orbit().radiusMeters());

        return parentPositionAtArrival.add(localArrivalOffset);
    }

    private static Vector positionMeters(final CelestialBody body, final double timeSeconds) {
        if (body instanceof Satellite satellite) {
            return satellite.orbit().stateAt(timeSeconds).positionMeters();
        }
        return OrbitState.stationary(body.initialPositionMeters()).positionMeters();
    }

}
