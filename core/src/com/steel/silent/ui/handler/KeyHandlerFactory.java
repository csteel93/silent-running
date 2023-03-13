package com.steel.silent.ui.handler;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.steel.silent.ui.UserInputConfigurations;
import com.steel.silent.ui.handler.key.ArrowKeyHandler;
import com.steel.silent.ui.handler.key.KeyHandler;
import com.steel.silent.ui.handler.key.SpaceKeyHandler;
import com.steel.silent.ui.handler.key.ZoomKeyHandler;

import java.util.Arrays;
import java.util.List;

public class KeyHandlerFactory {

    private static final int ZERO = 0;

    public static List<KeyHandler> getKeyHandlers(final OrthographicCamera camera,
                                                  final ExtendViewport viewport,
                                                  final UserInputConfigurations uiConfig) {
        return Arrays.asList(spaceKeyHandler(camera, viewport),
            upKeyHandler(uiConfig.getPanSpeed(), camera),
            downKeyHandler(uiConfig.getPanSpeed(), camera),
            leftKeyHandler(uiConfig.getPanSpeed(), camera),
            rightKeyHandler(uiConfig.getPanSpeed(), camera),
            zoomInKeyHandler(uiConfig.getZoomSpeed(), camera),
            zoomOutKeyHandler(uiConfig.getZoomSpeed(), camera));
    }

    public static KeyHandler spaceKeyHandler(final OrthographicCamera camera, final ExtendViewport viewport) {
        return new SpaceKeyHandler(camera, viewport);
    }

    public static KeyHandler upKeyHandler(final int panSpeed, final OrthographicCamera camera) {
        return new ArrowKeyHandler(Input.Keys.UP, camera, ZERO, panSpeed);
    }

    public static KeyHandler downKeyHandler(final int panSpeed, final OrthographicCamera camera) {
        return new ArrowKeyHandler(Input.Keys.DOWN, camera, ZERO, -panSpeed);
    }

    public static KeyHandler leftKeyHandler(final int panSpeed, final OrthographicCamera camera) {
        return new ArrowKeyHandler(Input.Keys.LEFT, camera, -panSpeed, ZERO);
    }

    public static KeyHandler rightKeyHandler(final int panSpeed, final OrthographicCamera camera) {
        return new ArrowKeyHandler(Input.Keys.RIGHT, camera, panSpeed, ZERO);
    }

    public static KeyHandler zoomInKeyHandler(final int zoomSpeed, final OrthographicCamera camera) {
        return new ZoomKeyHandler(Input.Keys.EQUALS, camera, -0.01f * zoomSpeed);
    }

    public static KeyHandler zoomOutKeyHandler(final int zoomSpeed, final OrthographicCamera camera) {
        return new ZoomKeyHandler(Input.Keys.MINUS, camera, 0.01f * zoomSpeed);
    }
}
