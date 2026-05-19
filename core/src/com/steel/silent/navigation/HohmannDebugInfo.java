package com.steel.silent.navigation;

import com.badlogic.gdx.utils.Array;
import com.steel.silent.entity.Coordinates;

public class HohmannDebugInfo {

    public LaunchWindow window;

    public Coordinates sourceCurrentPosition;
    public Coordinates destinationCurrentPosition;

    public Coordinates sourceLaunchPosition;
    public Coordinates destinationArrivalPosition;
    public Coordinates expectedArrivalPosition;

    public Coordinates parentLaunchPosition;
    public Coordinates parentArrivalPosition;

    public Coordinates sourceLaunchVelocity;
    public Coordinates destinationArrivalVelocity;

    public Array<Coordinates> transferCurvePoints = new Array<>();
}
