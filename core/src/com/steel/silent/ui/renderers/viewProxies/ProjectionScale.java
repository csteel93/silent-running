package com.steel.silent.ui.renderers.viewProxies;

/**
 * Central tuning values for display-space projection.
 *
 * <p>These values affect rendering/readability only. They should not be used
 * by simulation, orbit planning, collision logic, or physics.</p>
 *
 * @param mapFill fraction of the smaller map dimension used by the furthest
 *   body from the origin. Increase this to spread the solar system closer to
 *   the edges of the map. Decrease it to leave more empty border around the
 *   system and make orbital distances look more compact.
 * @param bodyExaggeration multiplier applied to physically projected body
 *   radii before clamping. Increase this to make planets, moons, and ships
 *   visually larger at the same zoom level. Decrease it to make body sizes
 *   closer to their physical projected scale.
 * @param minBodyRadius smallest rendered body radius in map units. Increase
 *   this when tiny moons or ships are too hard to see or click. Decrease it
 *   when small bodies look too chunky, oversized, or visually misleading.
 * @param maxBodyRadius largest rendered body radius in map units. Increase
 *   this if large planets or the star should feel more massive when zoomed
 *   out. Decrease it if large bodies dominate the view or cover nearby orbit
 *   context.
 */
public record ProjectionScale(
        double mapFill,
        double bodyExaggeration,
        double minBodyRadius,
        double maxBodyRadius) {

    public static final ProjectionScale DEFAULT = new ProjectionScale(
            0.80,
            1.0,
            0.00000001,
            20.0);
}
