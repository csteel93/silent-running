package com.steel.silent.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.steel.silent.simulation.Simulation;
import com.steel.silent.ui.button.PauseButton;
import com.steel.silent.ui.button.SpeedButton;

public class Gui extends Layer {

    public Gui(final float width, final float height,
               final Simulation simulation) {
        this(new OrthographicCamera(width, height), simulation);
    }

    public Gui(final OrthographicCamera camera, final Simulation simulation) {
        super(new ScreenViewport(camera));
        camera.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);
        getStage().addActor(new PauseButton(simulation));
        getStage().addActor(new SpeedButton(simulation));
    }

}
