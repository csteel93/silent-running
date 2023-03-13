package com.steel.silent.ui.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.steel.silent.simulation.Simulation;

public class PauseButton extends TextButton {

    private static final String PAUSE = "PAUSE";
    private static final String RESUME = "RESUME";
    private static final String TEXTURE = "button.png";
    private static final float X_COORD = 0;
    private static final float Y_COORD = Gdx.graphics.getHeight() - 50;
    private static final float WIDTH = 100;
    private static final float HEIGHT = 50;
    private boolean paused = false;

    public PauseButton(final Simulation simulation) {
        this(getDrawable(), simulation);
    }

    public PauseButton(final Drawable drawable, final Simulation simulation) {
        super(PAUSE, new TextButtonStyle(drawable, drawable, drawable, new BitmapFont()));
        setPosition(X_COORD, Y_COORD);
        setSize(WIDTH, HEIGHT);
        getStyle().fontColor = Color.BLACK;
        setColor(Color.LIGHT_GRAY);
        addListener(interactionListener(simulation));
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }

    private EventListener interactionListener(final Simulation simulation) {
        return new InputListener() {
            @Override
            public void touchUp(final InputEvent event, final float x, final float y, final int pointer, final int button) {
            }

            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer, final int button) {
                if (paused) {
                    simulation.resume();
                    paused = false;
                    setText(PAUSE);
                } else {
                    System.out.println("PAUSING");
                    simulation.pause();
                    paused = true;
                    setText(RESUME);
                }
                return true;
            }
        };
    }
}
