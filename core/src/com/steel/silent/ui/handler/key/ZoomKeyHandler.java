package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.graphics.OrthographicCamera;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZoomKeyHandler implements KeyHandler {

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
        camera.zoom += zoom;
    }
}
