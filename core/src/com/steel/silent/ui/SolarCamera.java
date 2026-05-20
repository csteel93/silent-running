package com.steel.silent.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

/**
 * Double-precision camera state for solar-system scale navigation.
 *
 * <p>The LibGDX {@link OrthographicCamera} is kept at world-origin (0, 0)
 * every frame. Body positions are expressed as <em>offsets from the camera
 * centre</em>, computed in double before casting to float (the camera-relative
 * coordinate trick that eliminates floating-point jitter at high zoom).
 * {@code camera.zoom} is set to {@code metersPerPixel} so that the
 * ShapeRenderer's metre-unit coordinates map correctly to screen pixels.</p>
 *
 * <p>Zoom range: {@value #MIN_VIEW_METERS} m (≈1 000 km viewport) to
 * {@value #MAX_VIEW_METERS} m (≈6 Tm viewport, well beyond the inner solar
 * system).</p>
 */
public class SolarCamera {

    public static final double MIN_VIEW_METERS = 1e6;
    public static final double MAX_VIEW_METERS = 6e12;

    private static final double INITIAL_VIEW_METERS = 6e11;

    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private double viewWidthMeters = INITIAL_VIEW_METERS;

    private float viewportWidth;
    private float viewportHeight;

    public SolarCamera(final float viewportWidth, final float viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void resize(final float width, final float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    /**
     * Pushes the current camera state into a LibGDX camera. The camera's
     * position is always (0, 0, 0); all world positioning is handled by
     * {@link WorldProjection} offsetting body coordinates.
     */
    public void applyTo(final OrthographicCamera camera) {
        camera.position.set(0f, 0f, 0f);
        camera.zoom = (float) metersPerPixel();
        camera.update();
    }

    /**
     * Returns a {@link WorldProjection} for the current camera state. Call
     * once per frame after {@link #applyTo(OrthographicCamera)}.
     */
    public WorldProjection buildProjection() {
        return new WorldProjection(cameraX, cameraY, metersPerPixel());
    }

    /**
     * Zoom toward a screen point, keeping that point's world position fixed.
     *
     * @param screenX  mouse/touch X in LibGDX screen coords (0 = left)
     * @param screenY  mouse/touch Y in LibGDX screen coords (0 = top)
     * @param factor   zoom factor: {@literal <} 1 zooms in, {@literal >} 1 zooms out
     */
    public void zoomToward(final float screenX, final float screenY, final double factor) {
        // Cursor offset from viewport centre in current metre-space
        final double mpp = metersPerPixel();
        final double curRelX = (screenX - viewportWidth * 0.5) * mpp;
        final double curRelY = (viewportHeight * 0.5 - screenY) * mpp; // flip Y

        // Actual world coords of cursor before zoom
        final double worldX = cameraX + curRelX;
        final double worldY = cameraY + curRelY;

        // Apply zoom
        viewWidthMeters = clamp(viewWidthMeters * factor, MIN_VIEW_METERS, MAX_VIEW_METERS);

        // Cursor offset in new metre-space
        final double newMpp = metersPerPixel();
        final double newRelX = (screenX - viewportWidth * 0.5) * newMpp;
        final double newRelY = (viewportHeight * 0.5 - screenY) * newMpp;

        // Shift camera so cursor remains at the same world position
        cameraX = worldX - newRelX;
        cameraY = worldY - newRelY;
    }

    /**
     * Pan the camera by a screen-pixel delta. Signs follow LibGDX screen
     * conventions: positive {@code dxPixels} pans right, positive
     * {@code dyPixels} pans up (screen Y is inverted relative to world Y).
     */
    public void panByPixels(final float dxPixels, final float dyPixels) {
        final double mpp = metersPerPixel();
        cameraX -= dxPixels * mpp;
        cameraY += dyPixels * mpp; // screen dy is inverted
    }

    /**
     * Centre on a world position and set zoom to show the body comfortably.
     *
     * @param worldX          body position in metres (absolute)
     * @param worldY          body position in metres (absolute)
     * @param influenceRadius Hill-sphere or display context radius in metres
     */
    public void focusOn(final double worldX, final double worldY, final double influenceRadius) {
        cameraX = worldX;
        cameraY = worldY;
        viewWidthMeters = clamp(influenceRadius * 20.0, MIN_VIEW_METERS, MAX_VIEW_METERS);
    }

    /** Zoom keeping the camera centre fixed (used by keyboard +/- keys). */
    public void zoomAtCenter(final double factor) {
        viewWidthMeters = clamp(viewWidthMeters * factor, MIN_VIEW_METERS, MAX_VIEW_METERS);
    }

    public double metersPerPixel() {
        return viewWidthMeters / viewportWidth;
    }

    public double viewWidthMeters() {
        return viewWidthMeters;
    }

    public double cameraX() {
        return cameraX;
    }

    public double cameraY() {
        return cameraY;
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
}
