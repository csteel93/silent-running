package com.steel.silent.ui.handler.key;

import com.steel.silent.ui.SolarCamera;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ArrowKeyHandler implements KeyHandler {

    private final int arrowKey;
    private final SolarCamera solarCamera;
    /** Pan amount in screen pixels per key-repeat tick. */
    private final float dx;
    private final float dy;

    @Override
    public int getKeycode() {
        return arrowKey;
    }

    @Override
    public boolean canHold() {
        return true;
    }

    @Override
    public void handleKey() {
        solarCamera.panByPixels(dx, dy);
    }
}
