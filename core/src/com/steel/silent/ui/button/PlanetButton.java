package com.steel.silent.ui.button;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.ui.SkyMap;

public class PlanetButton extends TextButton {
    private static final float PLANET_BUTTON_WIDTH = 120f;
    private static final float PLANET_BUTTON_HEIGHT = 32f;

    public PlanetButton(final CelestialBody body,
            final Drawable drawable,
            final SkyMap map) {
        super(body.name(), new TextButtonStyle(drawable, drawable, drawable, new BitmapFont()));
        setSize(PLANET_BUTTON_WIDTH, PLANET_BUTTON_HEIGHT);
        getStyle().fontColor = Color.BLACK;
        setColor(Color.LIGHT_GRAY);
        addListener(focusListener(body, map));

    }

    private EventListener focusListener(final CelestialBody body, final SkyMap map) {
        return new InputListener() {
            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
                map.focusOn(body);
                return true;
            }
        };
    }
}
