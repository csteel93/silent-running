package com.steel.silent.ui.renderers.viewProxies;

/**
 * Projects SI-unit simulation positions into camera-relative rendering space.
 *
 * <p>The camera is always placed at the origin of the rendering coordinate
 * system. Each body's position is expressed as a <em>relative offset</em>
 * (in meters) from the camera centre. The offset is computed in double
 * precision and only cast to float at the last moment, keeping full
 * accuracy even when the camera is deep inside the solar system.</p>
 *
 * <p>Rendering coordinates passed to {@link com.badlogic.gdx.graphics.glutils.ShapeRenderer}
 * are in meters; the LibGDX camera's {@code zoom} is set to
 * {@code metersPerPixel} so that 1 m in render-space = 1/metersPerPixel
 * pixels on screen.</p>
 */
public class WorldProjection {

    private static final float MIN_SCREEN_RADIUS_PX = 3f;

    private final double cameraX;        // world metres
    private final double cameraY;        // world metres
    private final double metersPerPixel; // camera.zoom value

    public WorldProjection(final double cameraX,
            final double cameraY,
            final double metersPerPixel) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.metersPerPixel = metersPerPixel;
    }

    /** Body centre expressed as a camera-relative offset in metres. */
    public float relX(final double positionMeters) {
        return (float) (positionMeters - cameraX);
    }

    /** Body centre expressed as a camera-relative offset in metres. */
    public float relY(final double positionMeters) {
        return (float) (positionMeters - cameraY);
    }

    /**
     * Rendered radius in metres for the ShapeRenderer. Enforces the
     * {@link #MIN_SCREEN_RADIUS_PX} floor so no body disappears at low zoom.
     */
    public float renderRadiusMeters(final double radiusMeters) {
        return (float) Math.max(MIN_SCREEN_RADIUS_PX * metersPerPixel, radiusMeters);
    }

    /** Converts a metre value to screen pixels at the current zoom. */
    public float metersToPixels(final double meters) {
        return (float) (meters / metersPerPixel);
    }

    public double metersPerPixel() {
        return metersPerPixel;
    }

    public double cameraX() {
        return cameraX;
    }

    public double cameraY() {
        return cameraY;
    }
}
