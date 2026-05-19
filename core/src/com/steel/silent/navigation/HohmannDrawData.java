package com.steel.silent.navigation;

import java.util.ArrayList;
import java.util.List;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;
import com.steel.silent.ui.renderers.Vec2d;

public record HohmannDrawData(
           Vec2d sourceLaunchPosMeters,
        Vec2d destinationArrivalPosMeters,
        Vec2d expectedArrivalPosMeters,
        Vec2d parentPosAtLaunchMeters,
        Vec2d parentPosAtArrivalMeters,
        List<Vec2d> transferPointsMeters) {

  public static HohmannDrawData buildHohmannDrawData(LaunchWindow window) {
    CelestialBody source = window.getSource();
    CelestialBody destination = window.getDestination();
    CelestialBody parent = window.getParent();

    double launchTime = window.getLaunchTime();
    double arrivalTime = window.getArrivalTime();

    Vec2d sourceLaunchPos =
            source.getWorldPositionMeters(launchTime);

    Vec2d destinationArrivalPos =
            destination.getWorldPositionMeters(arrivalTime);

    Vec2d parentLaunchPos =
            parent.getWorldPositionMeters(launchTime);

    Vec2d parentArrivalPos =
            parent.getWorldPositionMeters(arrivalTime);

    Vec2d expectedArrivalPos =
            calculateExpectedArrivalPositionMeters(window);

    List<Vec2d> transferPoints =
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

    public static List<Vec2d> buildHohmannRoutePointsMeters(final LaunchWindow window) {
        Satellite source = window.getSource();
        Satellite destination = window.getDestination();
        CelestialBody parent = window.getParent();
        double sourceRadius = source.getOrbitalRadius();
        double destinationRadius = destination.getOrbitalRadius();
        double semiMajorAxis = (sourceRadius + destinationRadius) * 0.5;
        double eccentricity = Math.abs(destinationRadius - sourceRadius)
                / (sourceRadius + destinationRadius);
        double semiLatusRectum = semiMajorAxis * (1.0 - eccentricity * eccentricity);
        boolean outward = destinationRadius > sourceRadius;

        int samples = 160;
        List<Vec2d> points = new ArrayList<>(samples + 1);

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
            Vec2d parentPosition = parent.getWorldPositionMeters(sampleTime);
            points.add(new Vec2d(
                    parentPosition.x() + transferRadius * Math.cos(worldAngle),
                    parentPosition.y() + transferRadius * Math.sin(worldAngle)));
        }

        return points;
    }

    public static Vec2d calculateExpectedArrivalPositionMeters(
            LaunchWindow window) {
        Satellite destination = window.getDestination();
        CelestialBody parent = window.getParent();

        Vec2d parentPositionAtArrival = parent.getWorldPositionMeters(window.getArrivalTime());

        double angle = window.getExpectedArrivalAngle();

        Vec2d localArrivalOffset = new Vec2d(
                Math.cos(angle) * destination.getOrbitalRadius(),
                Math.sin(angle) * destination.getOrbitalRadius());

        return parentPositionAtArrival.add(localArrivalOffset);
    }

}
