package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.graphics.OrthographicCamera;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ArrowKeyHandler implements KeyHandler {

    private static final int ZERO = 0;

    private final int arrowKey;
    private final OrthographicCamera camera;
    private final int x;
    private final int y;

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
        camera.translate(x, y, ZERO);
    }
}
