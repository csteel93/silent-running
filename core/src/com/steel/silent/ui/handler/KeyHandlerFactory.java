package com.steel.silent.ui.handler;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.steel.silent.ui.SolarCamera;
import com.steel.silent.ui.UserInputConfigurations;
import com.steel.silent.ui.handler.key.ArrowKeyHandler;
import com.steel.silent.ui.handler.key.KeyHandler;
import com.steel.silent.ui.handler.key.SpaceKeyHandler;
import com.steel.silent.ui.handler.key.ZoomKeyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyHandlerFactory {

    private static final double ZOOM_IN_FACTOR  = 0.95;  // 5 % zoom-in per tick
    private static final double ZOOM_OUT_FACTOR = 1.05;  // 5 % zoom-out per tick

    public static List<KeyHandler> getKeyHandlers(final OrthographicCamera camera,
                                                  final Viewport viewport,
                                                  final UserInputConfigurations uiConfig,
                                                  final SolarCamera solarCamera) {
        return new ArrayList<>(Arrays.asList(
                spaceKeyHandler(camera, viewport),
                upKeyHandler(uiConfig.getPanSpeed(), solarCamera),
                downKeyHandler(uiConfig.getPanSpeed(), solarCamera),
                leftKeyHandler(uiConfig.getPanSpeed(), solarCamera),
                rightKeyHandler(uiConfig.getPanSpeed(), solarCamera),
                zoomInKeyHandler(solarCamera),
                zoomOutKeyHandler(solarCamera)));
    }

    public static KeyHandler spaceKeyHandler(final OrthographicCamera camera, final Viewport viewport) {
        return new SpaceKeyHandler(camera, viewport);
    }

    public static KeyHandler upKeyHandler(final float panSpeed, final SolarCamera solarCamera) {
        return new ArrowKeyHandler(Input.Keys.UP, solarCamera, 0, panSpeed);
    }

    public static KeyHandler downKeyHandler(final float panSpeed, final SolarCamera solarCamera) {
        return new ArrowKeyHandler(Input.Keys.DOWN, solarCamera, 0, -panSpeed);
    }

    public static KeyHandler leftKeyHandler(final float panSpeed, final SolarCamera solarCamera) {
        return new ArrowKeyHandler(Input.Keys.LEFT, solarCamera, -panSpeed, 0);
    }

    public static KeyHandler rightKeyHandler(final float panSpeed, final SolarCamera solarCamera) {
        return new ArrowKeyHandler(Input.Keys.RIGHT, solarCamera, panSpeed, 0);
    }

    public static KeyHandler zoomInKeyHandler(final SolarCamera solarCamera) {
        return new ZoomKeyHandler(Input.Keys.EQUALS, solarCamera, ZOOM_IN_FACTOR);
    }

    public static KeyHandler zoomOutKeyHandler(final SolarCamera solarCamera) {
        return new ZoomKeyHandler(Input.Keys.MINUS, solarCamera, ZOOM_OUT_FACTOR);
    }
}
