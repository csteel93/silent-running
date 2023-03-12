package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SpaceKeyHandler implements KeyHandler {

    private final OrthographicCamera camera;
    private final ExtendViewport viewport;

    @Override
    public int getKeycode() {
        return Input.Keys.SPACE;
    }

    @Override
    public boolean canHold() {
        return false;
    }

    @Override
    public void handleKey() {
        System.out.println();
        System.out.println("VIEWPORT pos " + viewport.getScreenX() + " " + viewport.getScreenY());
        System.out.println("VIEWPORT size " + viewport.getScreenWidth() + " " + viewport.getScreenHeight());
        System.out.println("ZOOM          " + camera.zoom);
        System.out.println("CAMERA        " + camera.viewportWidth + " " + camera.viewportHeight);
        System.out.println("CAMERA   pos  " + camera.position.x + " " + camera.position.y);
        System.out.println("GRAPHICS       " + Gdx.graphics.getHeight() + " " + Gdx.graphics.getHeight());
        System.out.println();
    }
}
