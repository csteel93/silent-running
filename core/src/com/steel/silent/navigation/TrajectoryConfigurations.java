package com.steel.silent.navigation;

/**
 * Centralized, tunable configuration for all route-planning parameters.
 *
 * <p>Every value can be overridden at run-time via a Java system property
 * {@code silent.trajectory.<name>} (e.g.
 * {@code -Dsilent.trajectory.maxGravityAssists=4}), making it easy to tune
 * behaviour without recompiling.</p>
 *
 * <h2>Categories</h2>
 * <ul>
 *   <li><b>Transfer timing</b> — Hohmann time scale, clamps, launch window search.</li>
 *   <li><b>Gravity-assist geometry</b> — turn angle range, safety margins.</li>
 *   <li><b>Search limits</b> — beam width, assist depth, expansion budget.</li>
 *   <li><b>Route scoring</b> — weights for time, delta-V, assists, alignment, etc.</li>
 * </ul>
 */
public final class TrajectoryConfigurations {

    // =========================================================================
    // Transfer timing
    // =========================================================================

    /** No longer used for timing (replaced by visualMu-based formula), but kept
     *  as a backward-compat default for bodies that haven't been explicitly
     *  calibrated: 1/28² ≈ 0.00128 matches the legacy hohmannTimeScale=28. */
    public static final double DEFAULT_HOHMANN_TIME_SCALE   = 28.0;
    public static final double DEFAULT_MIN_TRANSFER_MS      = 1_500.0;
    public static final double DEFAULT_MAX_TRANSFER_MS      = 45_000.0;
    public static final double DEFAULT_BALANCED_WAIT_COST   = 0.65;
    public static final long   DEFAULT_MAX_LAUNCH_WAIT_MS   = 120_000L;
    public static final long   DEFAULT_MAX_SHARED_MOON_WAIT_MS = 240_000L;

    // =========================================================================
    // Validation
    // =========================================================================

    /** Minimum gap between the ship surface and a body's surface (world units). */
    public static final double DEFAULT_COLLISION_SAFETY_MARGIN = 4.0;

    // =========================================================================
    // Gravity-assist geometry
    // =========================================================================

    /** Minimum flyby deflection angle (degrees). */
    public static final double DEFAULT_MIN_TURN_ANGLE_DEG = 5.0;
    /** Maximum flyby deflection angle (degrees). */
    public static final double DEFAULT_MAX_TURN_ANGLE_DEG = 120.0;

    // =========================================================================
    // Search limits
    // =========================================================================

    /** Maximum gravity-assist hops in a single route. Caps search depth. */
    public static final int DEFAULT_MAX_GRAVITY_ASSISTS = 3;
    /** Beam width: how many candidates survive to the next search depth level. */
    public static final int DEFAULT_MAX_CANDIDATES_PER_DEPTH = 8;
    /** How many gravity-well blocking events to try as assist candidates per expansion. */
    public static final int DEFAULT_MAX_ASSIST_OPTIONS_PER_EXPANSION = 3;

    // =========================================================================
    // Route scoring weights (lower total score = better route)
    // =========================================================================

    public static final double DEFAULT_TRAVEL_TIME_WEIGHT = 1.0;
    public static final double DEFAULT_DELTA_V_WEIGHT     = 50.0;
    public static final double DEFAULT_ASSIST_PENALTY     = 3_000.0;
    public static final double DEFAULT_AVOIDANCE_PENALTY  = 8_000.0;
    public static final double DEFAULT_ALIGNMENT_REWARD   = 5_000.0;

    // =========================================================================
    // Accessors
    // =========================================================================

    private static final String PREFIX = "silent.trajectory.";

    // --- Transfer timing ---

    public static double hohmannTimeScale() {
        return doubleProperty("hohmannTimeScale", DEFAULT_HOHMANN_TIME_SCALE);
    }

    public static double minTransferMs() {
        return doubleProperty("minTransferMs", DEFAULT_MIN_TRANSFER_MS);
    }

    public static double maxTransferMs() {
        return doubleProperty("maxTransferMs", DEFAULT_MAX_TRANSFER_MS);
    }

    public static double balancedWaitCost() {
        return doubleProperty("balancedWaitCost", DEFAULT_BALANCED_WAIT_COST);
    }

    public static long maxLaunchWaitMs() {
        return longProperty("maxLaunchWaitMs", DEFAULT_MAX_LAUNCH_WAIT_MS);
    }

    public static long maxSharedMoonWaitMs() {
        return longProperty("maxSharedMoonWaitMs", DEFAULT_MAX_SHARED_MOON_WAIT_MS);
    }

    // --- Validation ---

    public static double collisionSafetyMargin() {
        return doubleProperty("collisionSafetyMargin", DEFAULT_COLLISION_SAFETY_MARGIN);
    }

    // --- Gravity-assist geometry ---

    public static double minTurnAngleDeg() {
        return doubleProperty("minTurnAngleDeg", DEFAULT_MIN_TURN_ANGLE_DEG);
    }

    public static double maxTurnAngleDeg() {
        return doubleProperty("maxTurnAngleDeg", DEFAULT_MAX_TURN_ANGLE_DEG);
    }

    // --- Search limits ---

    public static int maxGravityAssists() {
        return intProperty("maxGravityAssists", DEFAULT_MAX_GRAVITY_ASSISTS);
    }

    public static int maxCandidatesPerDepth() {
        return intProperty("maxCandidatesPerDepth", DEFAULT_MAX_CANDIDATES_PER_DEPTH);
    }

    public static int maxAssistOptionsPerExpansion() {
        return intProperty("maxAssistOptionsPerExpansion", DEFAULT_MAX_ASSIST_OPTIONS_PER_EXPANSION);
    }

    // --- Scoring ---

    public static double travelTimeWeight() {
        return doubleProperty("travelTimeWeight", DEFAULT_TRAVEL_TIME_WEIGHT);
    }

    public static double deltaVWeight() {
        return doubleProperty("deltaVWeight", DEFAULT_DELTA_V_WEIGHT);
    }

    public static double assistPenalty() {
        return doubleProperty("assistPenalty", DEFAULT_ASSIST_PENALTY);
    }

    public static double avoidancePenalty() {
        return doubleProperty("avoidancePenalty", DEFAULT_AVOIDANCE_PENALTY);
    }

    public static double alignmentReward() {
        return doubleProperty("alignmentReward", DEFAULT_ALIGNMENT_REWARD);
    }

    // =========================================================================
    // Property readers
    // =========================================================================

    private static double doubleProperty(final String name, final double fallback) {
        // Properties are read lazily so tweaking JVM flags between runs does
        // not require changing source code. Example:
        // -Dsilent.trajectory.maxTransferMs=30000
        final String value = System.getProperty(PREFIX + name);
        if (value == null || value.isBlank()) return fallback;
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException e) {
            System.out.printf("[trajectory-config] invalid %s='%s', using %.4f%n", name, value, fallback);
            return fallback;
        }
    }

    private static long longProperty(final String name, final long fallback) {
        // Long-valued knobs are used for simulation-time windows and caps.
        final String value = System.getProperty(PREFIX + name);
        if (value == null || value.isBlank()) return fallback;
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            System.out.printf("[trajectory-config] invalid %s='%s', using %d%n", name, value, fallback);
            return fallback;
        }
    }

    private static int intProperty(final String name, final int fallback) {
        // Integer knobs primarily control beam-search breadth/depth.
        final String value = System.getProperty(PREFIX + name);
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            System.out.printf("[trajectory-config] invalid %s='%s', using %d%n", name, value, fallback);
            return fallback;
        }
    }

    private TrajectoryConfigurations() {}
}
