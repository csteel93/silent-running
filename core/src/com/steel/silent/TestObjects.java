package com.steel.silent;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.navigation.LaunchWindow;
import com.steel.silent.navigation.OrbitalMechanics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.steel.silent.AstroConstants.SUN_MU;
import static com.steel.silent.AstroConstants.SUN_RADIUS;
import static com.steel.silent.AstroConstants.SUN_ROTATION;

public class TestObjects {

    // private static final double ORBIT_STEP = 400.0;
    // private static final double MILLIS_PER_ORBITAL_DAY =
    // Duration.ofDays(1).toMillis();

    public static FocalPoint getFocalPoint(float scaled_width, float scaled_height) {
        final FocalPoint sol = new FocalPoint(
                SUN_RADIUS,
                0.0,
                0.0,
                SUN_MU,
                SUN_ROTATION);
        sol.getCharacteristics().setColor(Color.YELLOW.toString());
        sol.getCharacteristics().setName("Sol");
        sol.getCharacteristics().setClassification("STAR");

        return sol;
    }

    public static List<Satellite> getSatellites(FocalPoint sol) {
        final Satellite mercury = planet(sol, "Mercury",
                Color.LIGHT_GRAY,
                2.4394e6,
                5.7909227e10,
                2.2031868551e13,
                7_600_543.81992,
                5_067_034.56);
        final Satellite venus = planet(sol, "Venus",
                Color.GOLD,
                6.0518e6,
                1.08209475e11,
                3.24858592e14,
                19_414_149.052176,
                20_995_200.0);
        final Satellite earth = planet(sol, "Earth",
                Color.BLUE,
                6.3710084e6,
                1.49598262e11,
                3.98600435507e14,
                31_558_149.10224,
                86_164.1);
        final Satellite moon = moon(earth, "Moon",
                Color.LIGHT_GRAY,
                1.7374e6,
                3.844e8,
                4.902800e12,
                2_360_620.8,
                2_360_620.8);
        final Satellite mars = planet(sol, "Mars",
                Color.RED,
                3.3895e6,
                2.27943824e11,
                4.2828375816e13,
                59_355_036.22176,
                88_642.7);
        final Satellite phobos = moon(mars, "Phobos",
                Color.GRAY,
                1.108e4,
                9.375e6,
                7.087e5,
                27_535.68,
                27_535.68);
        final Satellite deimos = moon(mars, "Deimos",
                Color.DARK_GRAY,
                6.2e3,
                2.3457e7,
                9.62e4,
                109_080.0,
                109_080.0);

        double transferTimeSeconds = OrbitalMechanics.calculateTransferTime(earth, mars);
        System.out.println("transfer time earch - mars: " + transferTimeSeconds);

        // LaunchWindow best = OrbitalMechanics.findLaunchWindow(earth, mars, 0);

        // System.out.println("Launch Window:");
        // System.out.println(" valid: " + best.valid);
        // System.out.println(" launch time: " + best.launchTime);
        // System.out.println(" arrival time: " + best.arrivalTime);
        // System.out.println(" transfer time: " + best.transferTime);
        // System.out.println(" source launch angle: " + best.sourceLaunchAngle);
        // System.out.println(" expected arrival angle: " + best.expectedArrivalAngle);
        // System.out.println(" destination arrival angle: " +
        // best.destinationArrivalAngle);
        // System.out.println(" phase error radians: " + best.phaseError);
        // System.out.println(" phase error degrees: " + best.phaseError *
        // MathUtils.radiansToDegrees);

        return Arrays.asList(mercury, venus, earth, moon, mars, phobos, deimos);
    }

    /**
     * A debug ship that starts in low orbit around the given parent body.
     */
    // public static Ship getTestShip(final CelestialBody parent) {
    // final BigDecimal orbitalRadius = parent.artificialSatelliteOrbitRadius();
    // final BigDecimal periodSeconds = shipOrbitalPeriodSeconds(parent,
    // orbitalRadius);
    // final Ship ship = new Ship(parent, bd(1), orbitalRadius, periodSeconds);
    // ship.getCharacteristics().setColor(Color.WHITE.toString());
    // ship.getCharacteristics().setName("Wayfarer");
    // ship.getCharacteristics().setClassification("SHIP");
    // return ship;
    // }

    // -------------------------------------------------------------------------
    // Private factory helpers
    // -------------------------------------------------------------------------

    private static BigDecimal bd(final double val) {
        return BigDecimal.valueOf(val);
    }

    // private static BigDecimal days(final double days) {
    // return bd(days * MILLIS_PER_ORBITAL_DAY);
    // }

    // private static BigDecimal shipOrbitalPeriodSeconds(final CelestialBody
    // parent,
    // final BigDecimal orbitalRadius) {
    // final double mu = Math.max(1e-15, parent.getVisualMu());
    // final double radius = orbitalRadius.doubleValue();
    // final double periodMs = Math.PI * 2.0 * Math.sqrt(radius * radius * radius /
    // mu);
    // return bd(Math.max(30.0, periodMs / 1_000.0));
    // }

    private static Satellite planet(final FocalPoint sol,
            final String name,
            final Color color,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double mu,
            final double orbitalPeriodSeconds,
            final double rotationPeriodSeconds) {
        final Satellite planet = new Satellite(sol,
                radiusMeters, orbitalRadiusMeters,
                orbitalPeriodSeconds, mu, rotationPeriodSeconds, Math.random() * Math.PI * 2);
        planet.getCharacteristics().setColor(color.toString());
        planet.getCharacteristics().setName(name);
        planet.getCharacteristics().setClassification("PLANET");
        System.out.println("=  " + name);
        System.out.println(" - radius    " + radiusMeters);
        System.out.println(" - influence " + planet.getInfluenceRadius());
        System.out.println(" - orbital   " + orbitalRadiusMeters);
        System.out.println(" - period    " + orbitalPeriodSeconds + " (seconds)");
        System.out.println(" - period    " + orbitalPeriodSeconds / 60 / 60 + " (hours)");

        // System.out.println(planet.name() + " satellite radius: " +
        // planet.artificialSatelliteOrbitRadius());
        // System.out.println(planet.name() + " influence radius: " +
        // planet.influenceRadius());
        return planet;
    }

    private static Satellite moon(final CelestialBody parent,
            final String name,
            final Color color,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double mu,
            final double orbitalPeriodSeconds,
            final double rotationPeriodSeconds) {
        final Satellite moon = new Satellite(parent,
                radiusMeters, orbitalRadiusMeters,
                orbitalPeriodSeconds, mu, rotationPeriodSeconds, Math.random() * Math.PI * 2);
        moon.getCharacteristics().setColor(color.toString());
        moon.getCharacteristics().setName(name);
        moon.getCharacteristics().setClassification("MOON");

        System.out.println("=  " + name);
        System.out.println(" - radius  " + radiusMeters);
        System.out.println(" - orbital " + orbitalRadiusMeters);
        System.out.println(" - period  " + orbitalPeriodSeconds + " (seconds)");
        System.out.println(" - period  " + orbitalPeriodSeconds / 60 / 60 + " (hours)");
        return moon;
    }
}
