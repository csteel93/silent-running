package com.steel.silent.app;

import com.badlogic.gdx.graphics.Color;
import com.steel.silent.math.Vector;
import com.steel.silent.model.body.CelestialBody;
import com.steel.silent.model.body.FocalPoint;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.model.craft.Ship;
import com.steel.silent.model.orbit.CircularOrbit;
import com.steel.silent.model.orbit.Orbit;

import java.util.Arrays;
import java.util.List;

import static com.steel.silent.app.AstroConstants.SUN_MU;
import static com.steel.silent.app.AstroConstants.SUN_RADIUS;
import static com.steel.silent.app.AstroConstants.SUN_ROTATION;

public class DemoUniverseFactory {

    public static FocalPoint getSol() {
        final FocalPoint sol = new FocalPoint(
                SUN_MU,
                SUN_RADIUS,
                Vector.ZERO(),
                Math.random() * Math.PI * 2,
                SUN_ROTATION);
        sol.characteristics().setColor(Color.YELLOW.toString());
        sol.characteristics().setName("Sol");
        sol.characteristics().setClassification("STAR");
        return sol;
    }

    public static List<Satellite> getSatellites(final FocalPoint sol) {
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
        // final Satellite phobos = moon(mars, "Phobos",
        // Color.GRAY,
        // 1.108e4,
        // 9.375e6,
        // 7.087e5,
        // 27_535.68,
        // 27_535.68);
        // final Satellite deimos = moon(mars, "Deimos",
        // Color.DARK_GRAY,
        // 6.2e3,
        // 2.3457e7,
        // 9.62e4,
        // 109_080.0,
        // 109_080.0);
        final Satellite phobos = moon(mars, "Phobos",
                Color.GRAY,
                1.108e5, // gameplay radius: 10x real Phobos radius
                5.0e7, // gameplay orbital radius: 50,000 km
                7.087e8, // gameplay mu: 1000x real
                114_525.0, // orbital period from Mars mu at 5.0e7 m, ~31.8 h
                27_535.68); // keep real-ish rotation period

        final Satellite deimos = moon(mars, "Deimos",
                Color.DARK_GRAY,
                6.2e4, // gameplay radius: 10x real Deimos radius
                1.25e8, // gameplay orbital radius: 125,000 km
                9.62e7, // gameplay mu: 1000x real
                452_707.0, // orbital period from Mars mu at 1.25e8 m, ~125.8 h
                109_080.0); // keep real-ish rotation period

        return Arrays.asList(mercury, venus, earth, moon, mars, phobos, deimos);
    }

    /** A debug ship starting in low orbit around the given parent body. */
    public static Ship getTestShip(final CelestialBody parent) {
        final double orbitalRadiusMeters = parent.radiusMeters() + Math.max(1_000.0, parent.radiusMeters() * 100.0);
        final double orbitalPeriodSeconds = circularOrbitPeriodSeconds(parent, orbitalRadiusMeters);
        final Ship ship = new Ship(parent, 100.0, orbitalRadiusMeters, orbitalPeriodSeconds);
        ship.characteristics().setColor(Color.WHITE.toString());
        ship.characteristics().setName("Wayfarer");
        return ship;
    }

    private static double circularOrbitPeriodSeconds(final CelestialBody parent,
            final double orbitalRadiusMeters) {
        return Math.PI * 2.0 * Math.sqrt(
                orbitalRadiusMeters * orbitalRadiusMeters * orbitalRadiusMeters / parent.mu());
    }

    private static Satellite planet(final FocalPoint sol,
            final String name,
            final Color color,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double mu,
            final double orbitalPeriodSeconds,
            final double rotationPeriodSeconds) {
        final Orbit circularOrbit = new CircularOrbit(sol, orbitalRadiusMeters, orbitalPeriodSeconds);
        final Vector initialPosition = initializeVector(sol, orbitalRadiusMeters, Math.random() * Math.PI * 2);
        final Satellite planet = new Satellite(
                circularOrbit,
                mu,
                radiusMeters,
                initialPosition,
                0.0,
                rotationPeriodSeconds);
        planet.characteristics().setColor(color.toString());
        planet.characteristics().setName(name);
        planet.characteristics().setClassification("PLANET");
        return planet;
    }

    private static Vector initializeVector(final CelestialBody primary,
            final double orbitalRadius,
            final double angle) {
        final double xOffset = orbitalRadius * Math.cos(angle);
        final double yOffset = orbitalRadius * Math.sin(angle);
        final double xCoord = xOffset + primary.initialPositionMeters().x();
        final double yCoord = yOffset + primary.initialPositionMeters().y();
        return new Vector(xCoord, yCoord);
    }

    private static Satellite moon(final CelestialBody parent,
            final String name,
            final Color color,
            final double radiusMeters,
            final double orbitalRadiusMeters,
            final double mu,
            final double orbitalPeriodSeconds,
            final double rotationPeriodSeconds) {
        final Orbit circularOrbit = new CircularOrbit(parent, orbitalRadiusMeters, orbitalPeriodSeconds);
        final Vector initialPosition = initializeVector(parent, orbitalRadiusMeters, Math.random() * Math.PI * 2);
        final Satellite moon = new Satellite(
                circularOrbit,
                mu,
                radiusMeters,
                initialPosition,
                0.0,
                rotationPeriodSeconds);
        moon.characteristics().setColor(color.toString());
        moon.characteristics().setName(name);
        moon.characteristics().setClassification("MOON");
        return moon;
    }
}
