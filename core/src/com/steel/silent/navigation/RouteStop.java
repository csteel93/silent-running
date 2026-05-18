package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;

/**
 * One waypoint of a routed path: a body to fly to, plus optional hints about
 * how to approach it.
 *
 * {@code approachClearance} controls how far outside the body's radius we
 * target on arrival (so the ship doesn't end up at the body's center).
 *
 * {@code speedMultiplier} is the slingshot/gravity-assist boost applied to
 * the leg leading into this stop. A value of 1.0 is a normal cruise; values
 * greater than 1.0 model momentum gained from a prior flyby.
 */
public final class RouteStop {

    public final CelestialBody body;
    /** Extra distance outside the body's radius when choosing an approach point. */
    public final double approachClearance;
    /** Multiplier applied to the leg ending at this stop. Used for simple assist boosts. */
    public final double speedMultiplier;
    /** True when this body is a through-point, false when it is the final capture target. */
    public final boolean isFlyby;

    public RouteStop(final CelestialBody body,
                     final double approachClearance,
                     final double speedMultiplier,
                     final boolean isFlyby) {
        this.body = body;
        this.approachClearance = approachClearance;
        this.speedMultiplier = speedMultiplier;
        this.isFlyby = isFlyby;
    }

    public static RouteStop destination(final CelestialBody body) {
        // Park comfortably outside the body's radius.
        // Destinations use a wider clearance than flybys because the ship will
        // transition into orbit after arrival.
        final double r = body.radius().doubleValue();
        return new RouteStop(body, Math.max(8.0, r * 0.6), 1.0, false);
    }

    public static RouteStop flyby(final CelestialBody body, final double speedMultiplier) {
        final double r = body.radius().doubleValue();
        // Skim close on a flyby; the boost is the reward for the close pass.
        // Actual collision safety still comes from RouteValidator.
        return new RouteStop(body, Math.max(4.0, r * 0.3), speedMultiplier, true);
    }
}
