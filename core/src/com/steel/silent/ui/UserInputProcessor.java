package com.steel.silent.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.steel.silent.entity.Ship;
import com.steel.silent.simulation.Universe;
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

    private static final float PICK_RADIUS_PIXELS = 12f;

    private final Map<Integer, KeyHandler> keyHandlers;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Queue<ImmutablePair<Float, Float>> scrolls = new ArrayDeque<>();
    private final ScrollHandler scrollHandler;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final Universe universe;

    public UserInputProcessor(final List<KeyHandler> keyHandlers,
                              final ScrollHandler scrollHandler) {
        this(keyHandlers, scrollHandler, null, null, null);
    }

    public UserInputProcessor(final List<KeyHandler> keyHandlers,
                              final ScrollHandler scrollHandler,
                              final OrthographicCamera camera,
                              final ExtendViewport viewport,
                              final Universe universe) {
        this.keyHandlers = keyHandlers.stream()
            .collect(Collectors.toMap(KeyHandler::getKeycode, Function.identity()));
        this.scrollHandler = scrollHandler;
        this.camera = camera;
        this.viewport = viewport;
        this.universe = universe;
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
        if (button != Input.Buttons.LEFT || camera == null || viewport == null || universe == null) {
            return false;
        }
        final Vector2 world = screenToWorld(screenX, screenY);
        // System.out.println("screen X: " + screenX + " screen Y: " + screenY );
        // System.out.println(" world X: " + world.x + "  world Y: " + world.y );

        // final Optional<Ship> ship = pickShip(world);
        // if (ship.isPresent()) {
        //     selectedShip = ship.get();
        //     System.out.printf("[point-click] selected ship %s%n", selectedShip.name());
        //     return true;
        // }

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

    private Vector2 screenToWorld(final int screenX, final int screenY) {
        camera.update();
        return viewport.unproject(new Vector2(screenX, screenY));
    }

    // private Optional<Ship> pickShip(final Vector2 world) {
    //     Ship closest = null;
    //     double closestDistance = Double.MAX_VALUE;
    //     for (final Ship ship : universe.getShips()) {
    //         final double distance = distanceTo(world, ship);
    //         if (distance <= pickRadiusFor(ship) && distance < closestDistance) {
    //             closest = ship;
    //             closestDistance = distance;
    //         }
    //     }
    //     return Optional.ofNullable(closest);
    // }

    // private double distanceTo(final Vector2 world, final VisibleObject body) {
    //     final double dx = world.x - body.x();
    //     final double dy = world.y - body.y();
    //     return Math.sqrt(dx * dx + dy * dy);
    // }

    // private double pickRadiusFor(final IdentifiableBody body) {
    //     return Math.max(body.radius().doubleValue(), screenPickRadiusWorld());
    // }

    // private double screenPickRadiusWorld() {
    //     final Vector2 center = viewport.unproject(new Vector2(0, 0));
    //     final Vector2 offset = viewport.unproject(new Vector2(PICK_RADIUS_PIXELS, 0));
    //     return Math.abs(offset.x - center.x);
    // }
}
