package com.steel.silent;

import com.badlogic.gdx.graphics.Color;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TestObjects {

    private static final double ORBIT_STEP = 400.0;
    private static final double MILLIS_PER_ORBITAL_DAY = Duration.ofDays(1).toMillis();

    public static FocalPoint getFocalPoint(float scaled_width, float scaled_height) {
        final FocalPoint sol = new FocalPoint(
            bd(36),
            bd(scaled_width).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(scaled_height).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(Duration.ofMinutes(2).toMillis()));
        sol.getCharacteristics().setColor(Color.YELLOW.toString());
        sol.getCharacteristics().setName("Sol");
        sol.getCharacteristics().setClassification("STAR");

        return sol;
    }

    public static List<Satellite> getSatellites(FocalPoint sol) {
        final Satellite mercury = planet(sol, "Mercury", Color.GRAY,     5,  ORBIT_STEP,     87.969);
        final Satellite venus   = planet(sol, "Venus",   Color.GOLD,     9,  ORBIT_STEP * 2, 224.701);
        final Satellite earth   = planet(sol, "Earth",   Color.BLUE,     10, ORBIT_STEP * 3, 365.256);
        final Satellite moon    = moon(earth, "Moon",    Color.LIGHT_GRAY, 3,  70,            27.3217);
        final Satellite mars    = planet(sol, "Mars",    Color.CORAL,    7,  ORBIT_STEP * 4, 686.980);
        final Satellite phobos  = moon(mars,  "Phobos",  Color.TAN,      2,  55,             0.31891);
        final Satellite deimos  = moon(mars,  "Deimos",  Color.SLATE,    2,  85,             1.26244);

        return Arrays.asList(mercury, venus, earth, moon, mars, phobos, deimos);
    }

    /**
     * A debug ship that starts in low orbit around the given parent body.
     */
    public static Ship getTestShip(final CelestialBody parent) {
        final BigDecimal orbitalRadius  = parent.artificialSatelliteOrbitRadius();
        final BigDecimal periodSeconds  = shipOrbitalPeriodSeconds(parent, orbitalRadius);
        final Ship ship = new Ship(parent, bd(1), orbitalRadius, periodSeconds);
        ship.getCharacteristics().setColor(Color.WHITE.toString());
        ship.getCharacteristics().setName("Wayfarer");
        ship.getCharacteristics().setClassification("SHIP");
        return ship;
    }

    // -------------------------------------------------------------------------
    // Private factory helpers
    // -------------------------------------------------------------------------

    private static BigDecimal bd(final double val) {
        return BigDecimal.valueOf(val);
    }

    private static BigDecimal days(final double days) {
        return bd(days * MILLIS_PER_ORBITAL_DAY);
    }

    private static BigDecimal shipOrbitalPeriodSeconds(final CelestialBody parent,
                                                       final BigDecimal orbitalRadius) {
        final double mu = Math.max(1e-15, parent.getVisualMu());
        final double radius = orbitalRadius.doubleValue();
        final double periodMs = Math.PI * 2.0 * Math.sqrt(radius * radius * radius / mu);
        return bd(Math.max(30.0, periodMs / 1_000.0));
    }

    private static Satellite planet(final FocalPoint sol,
                                    final String name,
                                    final Color color,
                                    final double radius,
                                    final double orbitalRadius,
                                    final double orbitalPeriodDays) {
        final Satellite planet = new Satellite(sol,
                bd(radius), bd(orbitalRadius),
                days(orbitalPeriodDays), BigDecimal.ZERO, Math.random() * Math.PI * 2);
        planet.getCharacteristics().setColor(color.toString());
        planet.getCharacteristics().setName(name);
        planet.getCharacteristics().setClassification("PLANET");
        System.out.println(planet.name() + " satellite radius: " + planet.artificialSatelliteOrbitRadius());
        System.out.println(planet.name() + " influence radius: " + planet.influenceRadius());
        return planet;
    }

    private static Satellite moon(final CelestialBody parent,
                                  final String name,
                                  final Color color,
                                  final double radius,
                                  final double orbitalRadius,
                                  final double orbitalPeriodDays) {
        final Satellite moon = new Satellite(parent,
                bd(radius), bd(orbitalRadius),
                days(orbitalPeriodDays), BigDecimal.ZERO, Math.random() * Math.PI * 2);
        moon.getCharacteristics().setColor(color.toString());
        moon.getCharacteristics().setName(name);
        moon.getCharacteristics().setClassification("MOON");
        return moon;
    }
}
