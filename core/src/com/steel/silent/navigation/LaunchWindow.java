package com.steel.silent.navigation;

import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.Satellite;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LaunchWindow {
    public Satellite source;
    public Satellite destination;
    public CelestialBody parent;

    public double launchTime;
    public double arrivalTime;
    public double transferTime;

    public double sourceLaunchAngle;
    public double expectedArrivalAngle;
    public double destinationArrivalAngle;

    public double phaseError;
    public boolean valid;
}
