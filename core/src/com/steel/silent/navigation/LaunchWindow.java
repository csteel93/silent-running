package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;

public class LaunchWindow {
    public Satellite source;
    public Satellite destination;
    public CelestialBody parent;
    public double launchTime;
    public double arrivalTime;
    public double transferTime;
    public double phaseError;
    public boolean valid;
    public double sourceLaunchAngle;
    public double expectedArrivalAngle;
    public double destinationArrivalAngle;
}
