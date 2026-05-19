package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.graphics.OrthographicCamera;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZoomKeyHandler implements KeyHandler {

    private static final float MIN_ZOOM = 0.000001f;
    private static final float MAX_ZOOM = 5.0f;

    private final int keycode;
    private final OrthographicCamera camera;
    private final float zoom;

    @Override
    public int getKeycode() {
        return keycode;
    }

    @Override
    public boolean canHold() {
        return true;
    }

    @Override
    public void handleKey() {
        final float zoomDelta = camera.zoom * zoom;
        camera.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, camera.zoom + zoomDelta));
    }
}
