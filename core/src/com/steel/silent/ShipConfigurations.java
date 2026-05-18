package com.steel.silent;

public class ShipConfigurations {

    public static final double DEFAULT_CRUISE_SPEED = 0.45;
    public static final String CRUISE_SPEED_PROPERTY = "silent.ship.cruiseSpeed";
    public static final String CRUISE_SPEED_ENV = "SILENT_SHIP_CRUISE_SPEED";

    private final double cruiseSpeed;

    public ShipConfigurations() {
        this(readCruiseSpeed());
    }

    public ShipConfigurations(final double cruiseSpeed) {
        this.cruiseSpeed = cruiseSpeed;
    }

    public double getCruiseSpeed() {
        return cruiseSpeed;
    }

    private static double readCruiseSpeed() {
        final String configured = firstPresent(
            System.getProperty(CRUISE_SPEED_PROPERTY),
            System.getenv(CRUISE_SPEED_ENV));
        if (configured == null || configured.isBlank()) {
            return DEFAULT_CRUISE_SPEED;
        }
        try {
            return Math.max(0.001, Double.parseDouble(configured));
        } catch (final NumberFormatException e) {
            System.out.printf("[ship-config] invalid cruise speed '%s', using %.2f%n",
                configured, DEFAULT_CRUISE_SPEED);
            return DEFAULT_CRUISE_SPEED;
        }
    }

    private static String firstPresent(final String first, final String second) {
        return first != null ? first : second;
    }
}
