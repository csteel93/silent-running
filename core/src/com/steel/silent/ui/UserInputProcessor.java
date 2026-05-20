package com.steel.silent.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.steel.silent.ui.handler.key.KeyHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserInputProcessor implements InputProcessor {

    private static final double SCROLL_ZOOM_FACTOR = 1.15;

    private final Map<Integer, KeyHandler> keyHandlers;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final SolarCamera solarCamera;

    private boolean dragging = false;
    private float lastDragX;
    private float lastDragY;

    public UserInputProcessor(final List<KeyHandler> keyHandlers,
                              final SolarCamera solarCamera) {
        this.keyHandlers = keyHandlers.stream()
                .collect(Collectors.toMap(KeyHandler::getKeycode, Function.identity()));
        this.solarCamera = solarCamera;
    }

    public void handleInput() {
        pressedKeys.forEach(keycode -> {
            final KeyHandler keyHandler = keyHandlers.get(keycode);
            keyHandler.handleKey();
            if (!keyHandler.canHold()) {
                pressedKeys.remove(keycode);
            }
        });
    }

    @Override
    public boolean keyDown(final int keycode) {
        Optional.ofNullable(keyHandlers.get(keycode))
                .ifPresent(h -> pressedKeys.add(h.getKeycode()));
        return true;
    }

    @Override
    public boolean keyUp(final int keycode) {
        pressedKeys.remove(keycode);
        return true;
    }

    @Override
    public boolean keyTyped(final char character) {
        return false;
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        if (button == Input.Buttons.LEFT) {
            dragging = true;
            lastDragX = screenX;
            lastDragY = screenY;
        }
        return false;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        if (button == Input.Buttons.LEFT) {
            dragging = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        if (dragging) {
            solarCamera.panByPixels(screenX - lastDragX, screenY - lastDragY);
            lastDragX = screenX;
            lastDragY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        // amountY > 0 = wheel down = zoom out; amountY < 0 = wheel up = zoom in
        final double factor = Math.pow(SCROLL_ZOOM_FACTOR, amountY);
        final int mx = com.badlogic.gdx.Gdx.input.getX();
        final int my = com.badlogic.gdx.Gdx.input.getY();
        solarCamera.zoomToward(mx, my, factor);
        return true;
    }
}
