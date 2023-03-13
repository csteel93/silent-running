package com.steel.silent.ui;

import com.badlogic.gdx.InputProcessor;
import com.steel.silent.ui.handler.key.KeyHandler;
import com.steel.silent.ui.handler.key.ScrollHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SuspiciousNameCombination")
public class UserInputProcessor implements InputProcessor {

    private final Map<Integer, KeyHandler> keyHandlers;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Queue<ImmutablePair<Float, Float>> scrolls = new ArrayDeque<>();
    private final ScrollHandler scrollHandler;

    public UserInputProcessor(final List<KeyHandler> keyHandlers,
                              final ScrollHandler scrollHandler) {
        this.keyHandlers = keyHandlers.stream()
            .collect(Collectors.toMap(KeyHandler::getKeycode, Function.identity()));
        this.scrollHandler = scrollHandler;
    }

    public void handleInput() {
        pressedKeys.forEach(keycode -> {
            final KeyHandler keyHandler = keyHandlers.get(keycode);
            keyHandler.handleKey();
            if (!keyHandler.canHold()) {
                pressedKeys.remove(keycode);
            }
        });
        Optional.ofNullable(scrolls.poll())
            .ifPresent(poll -> scrollHandler.scroll(poll.getLeft(), -poll.getRight()));
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
        scrolls.add(ImmutablePair.of(amountX, amountY));
        return true;
    }
}
