package com.steel.silent;

import com.badlogic.gdx.graphics.Color;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.OrbitalMechanics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TestObjects {

    private static final double ORBIT_STEP = 400.0;
    private static final double MILLIS_PER_ORBITAL_DAY = Duration.ofMinutes(1).toMillis();

    public static FocalPoint getFocalPoint(float scaled_width, float scaled_height) {
        final FocalPoint sol = new FocalPoint(
            bd(36),
            bd(scaled_width).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(scaled_height).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(Duration.ofMinutes(2).toMillis()));
        sol.getCharacteristics().setColor(Color.YELLOW.toString());
        sol.getCharacteristics().setName("Sol");
        sol.getCharacteristics().setClassification("STAR");

        // Calibrate Sol's visualMu from Earth's orbit so Hohmann timing is
        // consistent across all interplanetary transfers.
        //   mu = (2π / T_earth)² * r_earth³
        final long earthPeriodMs = daysMs(365.256);
        sol.setVisualMu(OrbitalMechanics.muFromOrbit(ORBIT_STEP * 3, earthPeriodMs));

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

        // --- gravity-assist strengths ---
        // Tuned so a typical cruise-speed approach gives a 30–90° deflection.
        // Formula: turnAngle = gs / (periapsis * relSpeed²)
        // With periapsis ≈ radius*2 and relSpeed ≈ cruiseSpeed ≈ 0.45 wu/ms:
        //   gs = turnAngle_rad * periapsis * 0.2025
        mercury.setGravityAssistStrength(1.5);   // ~40° at typical approach
        venus  .setGravityAssistStrength(4.0);   // ~60° — dense atmosphere bonus
        earth  .setGravityAssistStrength(3.5);   // ~50°
        moon   .setGravityAssistStrength(0.8);   // ~20° — low mass
        mars   .setGravityAssistStrength(2.5);   // ~40°
        phobos .setGravityAssistStrength(0.4);   // ~15° — tiny moon
        deimos .setGravityAssistStrength(0.4);   // ~15°

        // --- visualMu for planet parents (calibrated from their moons) ---
        earth.setVisualMu(OrbitalMechanics.muFromOrbit(70,  daysMs(27.3217)));
        mars .setVisualMu(OrbitalMechanics.muFromOrbit(55,  daysMs(0.31891)));

        return Arrays.asList(mercury, venus, earth, moon, mars, phobos, deimos);
    }

    /**
     * A debug ship that starts in low orbit around the given parent body.
     * Cruise speed is in world units per ms of simulation time; tuned roughly
     * to cross a planet-to-planet hop in tens of seconds of wall time at 1×.
     */
    public static Ship getTestShip(final CelestialBody parent) {
        return getTestShip(parent, new ShipConfigurations());
    }

    public static Ship getTestShip(final CelestialBody parent, final ShipConfigurations shipConfig) {
        final BigDecimal orbitalRadius  = bd(parent.radius().doubleValue() * 2.5 + 10.0);
        final BigDecimal periodSeconds  = bd(Duration.ofMinutes(2).getSeconds());
        final Ship ship = new Ship(parent, bd(4), orbitalRadius, periodSeconds, shipConfig.getCruiseSpeed());
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

    private static long daysMs(final double days) {
        return (long) (days * MILLIS_PER_ORBITAL_DAY);
    }

    private static BigDecimal days(final double days) {
        return bd(days * MILLIS_PER_ORBITAL_DAY);
    }

    private static Satellite planet(final FocalPoint sol,
                                    final String name,
                                    final Color color,
                                    final double radius,
                                    final double orbitalRadius,
                                    final double orbitalPeriodDays) {
        // influenceRadius: roughly 1/3 of the distance to the next planet (simplified).
        // For visual purposes, set to 1.5× the orbital radius as a generous SOI.
        final double influenceRadius = orbitalRadius * 0.18 + radius * 2.0;
        final Satellite planet = new Satellite(sol,
                bd(radius), bd(orbitalRadius), bd(influenceRadius),
                days(orbitalPeriodDays), BigDecimal.ZERO, Math.random() * Math.PI * 2);
        planet.getCharacteristics().setColor(color.toString());
        planet.getCharacteristics().setName(name);
        planet.getCharacteristics().setClassification("PLANET");
        return planet;
    }

    private static Satellite moon(final CelestialBody parent,
                                  final String name,
                                  final Color color,
                                  final double radius,
                                  final double orbitalRadius,
                                  final double orbitalPeriodDays) {
        // Moon influence radius: a fraction of its orbital distance from its parent.
        final double influenceRadius = orbitalRadius * 0.3 + radius * 2.0;
        final Satellite moon = new Satellite(parent,
                bd(radius), bd(orbitalRadius), bd(influenceRadius),
                days(orbitalPeriodDays), BigDecimal.ZERO, Math.random() * Math.PI * 2);
        moon.getCharacteristics().setColor(color.toString());
        moon.getCharacteristics().setName(name);
        moon.getCharacteristics().setClassification("MOON");
        return moon;
    }
}
