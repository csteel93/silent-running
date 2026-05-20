package com.steel.silent.ui.handler.key;

import com.steel.silent.ui.SolarCamera;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZoomKeyHandler implements KeyHandler {

    private final int keycode;
    private final SolarCamera solarCamera;
    /** Zoom factor per key-repeat. Values < 1 zoom in; values > 1 zoom out. */
    private final double factor;

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
        solarCamera.zoomAtCenter(factor);
    }
}
