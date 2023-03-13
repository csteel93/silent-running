package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.graphics.OrthographicCamera;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScrollHandler {

    private final OrthographicCamera camera;

    public void scroll(final float xScroll, final float yScroll) {
        camera.translate(xScroll, yScroll);
    }
}
