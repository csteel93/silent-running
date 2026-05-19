package com.steel.silent.ui.button;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.steel.silent.ui.SkyMap;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

import java.util.UUID;

public class PlanetButton extends TextButton {
    private static final float PLANET_BUTTON_WIDTH = 140f;
    private static final float PLANET_BUTTON_HEIGHT = 64f;

    public PlanetButton(final ProjectedBodyState body,
            final Drawable drawable,
            final SkyMap map) {
        super(body.name(), buttonStyle(drawable));
        setSize(PLANET_BUTTON_WIDTH, PLANET_BUTTON_HEIGHT);
        getStyle().fontColor = Color.BLACK;
        setColor(Color.LIGHT_GRAY);
        addListener(focusListener(body.id(), body.name(), map));
    }

    private static TextButtonStyle buttonStyle(final Drawable drawable) {
        final BitmapFont font = new BitmapFont();
        font.getData().setScale(1.75f);

        final TextButtonStyle style = new TextButtonStyle(drawable, drawable, drawable, font);
        style.fontColor = Color.BLACK;
        return style;
    }

    private EventListener focusListener(final UUID bodyId, final String bodyName, final SkyMap map) {
        return new InputListener() {
            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y,
                    final int pointer, final int button) {
                // System.out.println("focusing on " + bodyName);
                map.focusOn(bodyId);
                return true;
            }
        };
    }
}
