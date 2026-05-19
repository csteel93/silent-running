package com.steel.silent.math;

import com.badlogic.gdx.math.MathUtils;

public class Angles {

    public static double normalizeRadians(final double radians) {
        final double twoPiRadians = MathUtils.PI2;
        double normalizedRadians = radians % twoPiRadians;
        if (normalizedRadians < 0) {
            normalizedRadians += twoPiRadians;
        }
        return normalizedRadians;
    }

    public static double signedAangleDifferenceRadians(final double radiansA, final double radiansB) {
        double differenceRad = normalizeRadians(radiansA - radiansB);
        if (differenceRad > MathUtils.PI) {
            differenceRad -= MathUtils.PI2;
        }
        return differenceRad;
    }

}
