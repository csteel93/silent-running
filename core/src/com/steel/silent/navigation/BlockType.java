package com.steel.silent.navigation;

/**
 * The reason a celestial body blocks a planned route.
 *
 * <ul>
 *   <li>{@link #COLLISION}: the path would intersect the body's physical
 *       radius plus the configured safety margin. These events reject a route.</li>
 *   <li>{@link #GRAVITY_WELL}: the path enters the body's sphere of influence
 *       (its {@code influenceRadius}) without an explicit flyby plan. These
 *       events are candidates for conversion into gravity-assist waypoints.</li>
 * </ul>
 */
public enum BlockType {
    COLLISION,
    GRAVITY_WELL
}
