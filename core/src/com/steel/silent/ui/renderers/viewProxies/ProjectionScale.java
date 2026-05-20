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
 * @param childOrbitGap minimum display-space gap between a child body and its
 *   primary after both bodies' rendered radii are considered. Increase this
 *   to push moons farther away from their planets. Decrease it to keep moons
 *   closer to the primary and more tightly clustered.
 * @param childOrbitLogSpacing extra child-orbit spacing based on the physical
 *   orbit radius relative to the primary radius. Increase this to preserve
 *   more visible separation between close and far moons, such as Phobos and
 *   Deimos. Decrease it to make child systems more compact and less spread
 *   by their physical distance ratios.
 * @param minInfluenceRadiusGap minimum display-space gap between a body edge
 *   and its influence ring. Increase this if influence rings are too close to
 *   body sprites or hard to see. Decrease it if influence rings feel too
 *   inflated around bodies with small or placeholder influence values.
 * @param influenceRadiusLogSpacing extra influence-ring radius based on the
 *   physical influence-radius-to-body-radius ratio. Increase this to make
 *   meaningful influence differences more obvious, especially for moons.
 *   Decrease it if influence rings become too large or visually overpower the
 *   local system.
 * @param shipOrbitGap minimum display-space gap between a ship and its
 *   primary after rendered radii are considered. Increase this to keep ships
 *   from sitting visually on top of small bodies. Decrease it to make ships
 *   appear closer to their parent body.
 * @param shipOrbitInfluenceFill maximum fraction of the primary body's
 *   projected influence radius that a ship orbit may occupy. Increase this to
 *   allow ships to render farther from the primary while still staying inside
 *   the influence ring. Decrease it to pull ships closer toward the primary.
 */
public record ProjectionScale(
        double mapFill,
        double bodyExaggeration,
        double minBodyRadius,
        double maxBodyRadius,
        double childOrbitGap,
        double childOrbitLogSpacing,
        double minInfluenceRadiusGap,
        double influenceRadiusLogSpacing,
        double shipOrbitGap,
        double shipOrbitInfluenceFill) {

    public static final ProjectionScale DEFAULT = new ProjectionScale(
            0.80,
            60.0,
            0.2,
            20.0,
            12.0,
            14.0,
            3.0,
            8.0,
            0.5,
            0.70);
}
