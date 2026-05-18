package com.steel.silent.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.simulation.Simulation;
import com.steel.silent.simulation.Universe;
import com.steel.silent.ui.button.PauseButton;
import com.steel.silent.ui.button.SpeedButton;

import java.util.function.Consumer;

public class Gui extends Layer {

    private static final String PLANET_CLASSIFICATION = "PLANET";
    private static final String TEXTURE = "button.png";
    private static final float PLANET_BUTTON_X = 0f;
    private static final float PLANET_BUTTON_WIDTH = 120f;
    private static final float PLANET_BUTTON_HEIGHT = 32f;
    private static final float PLANET_BUTTON_GAP = 6f;
    private static final float PLANET_BUTTON_TOP_MARGIN = 58f + 50;

    public Gui(final float width, final float height,
            final Simulation simulation) {
        this(new OrthographicCamera(width, height), simulation, null, null);
    }

    public Gui(final float width, final float height,
            final Simulation simulation,
            final Universe universe,
            final Consumer<CelestialBody> planetFocusHandler) {
        this(new OrthographicCamera(width, height), simulation, universe, planetFocusHandler);
    }

    public Gui(final OrthographicCamera camera,
            final Simulation simulation,
            final Universe universe,
            final Consumer<CelestialBody> planetFocusHandler) {
        super(new ScreenViewport(camera));
        camera.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);
        getStage().addActor(new PauseButton(simulation));
        getStage().addActor(new SpeedButton(simulation));
        addPlanetButtons(universe, planetFocusHandler);
    }

    private void addPlanetButtons(final Universe universe,
            final Consumer<CelestialBody> planetFocusHandler) {
        if (universe == null || planetFocusHandler == null)
            return;
        final Drawable drawable = getDrawable();
        final int[] index = { 0 };
        universe.getState().forEach(body -> {
            if (body instanceof final CelestialBody celestialBody
                    && (PLANET_CLASSIFICATION.equals(celestialBody.classification())
                            || "STAR".equals(celestialBody.classification()))) {
                final TextButton button = planetButton(celestialBody, drawable);
                button.setPosition(
                        PLANET_BUTTON_X,
                        Gdx.graphics.getHeight() - PLANET_BUTTON_TOP_MARGIN
                                - (PLANET_BUTTON_HEIGHT + PLANET_BUTTON_GAP) * index[0]);
                button.addListener(focusListener(celestialBody, planetFocusHandler));
                getStage().addActor(button);
                index[0]++;
            }
        });
    }

    private TextButton planetButton(final CelestialBody body, final Drawable drawable) {
        final TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(drawable, drawable, drawable,
                new BitmapFont());
        final TextButton button = new TextButton(body.name(), style);
        button.setSize(PLANET_BUTTON_WIDTH, PLANET_BUTTON_HEIGHT);
        button.getStyle().fontColor = Color.BLACK;
        button.setColor(Color.LIGHT_GRAY);
        button.getLabel().setAlignment(Align.center);
        return button;
    }

    private EventListener focusListener(final CelestialBody body,
            final Consumer<CelestialBody> planetFocusHandler) {
        return new InputListener() {
            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
                planetFocusHandler.accept(body);
                return true;
            }
        };
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }
}
