package com.steel.silent.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.steel.silent.ui.handler.key.KeyHandler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserInputProcessor implements InputProcessor {

    private final Map<Integer, KeyHandler> keyHandlers;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public UserInputProcessor(final List<KeyHandler> keyHandlers) {
        this.keyHandlers = keyHandlers.stream()
                .collect(Collectors.toMap(KeyHandler::getKeycode, Function.identity()));
    }

    public void handlePressedKeys(){
        pressedKeys.forEach(keycode -> {
            final KeyHandler keyHandler = keyHandlers.get(keycode);
            keyHandler.handleKey();
            if(!keyHandler.canHold()){
                pressedKeys.remove(keycode);
            }
        });
    }

    @Override
    public boolean keyDown(final int keycode) {
        Optional.ofNullable(keyHandlers.get(keycode))
                .ifPresent(keyHandler -> pressedKeys.add(keyHandler.getKeycode()));
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
        return false;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        return false;
    }

    private boolean shouldProcess(final int keycode) {
        return keyHandlers.containsKey(keycode)
                && (keyHandlers.get(keycode).canHold()
                || (!keyHandlers.get(keycode).canHold()
                && !pressedKeys.contains(keycode)));
    }
}
