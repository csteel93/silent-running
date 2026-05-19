package com.steel.silent.ui;

import java.util.List;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.steel.silent.simulation.Simulation;
import com.steel.silent.ui.button.LabelToggleButton;
import com.steel.silent.ui.button.PauseButton;
import com.steel.silent.ui.button.PlanetButtons;
import com.steel.silent.ui.button.SpeedButton;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

public class Gui extends Layer {

    public Gui(final float width, final float height,final Simulation simulation) {
        this(new OrthographicCamera(width, height), simulation, null, null);
    }

    public Gui(final float width, final float height,
            final Simulation simulation,
            final List<ProjectedBody> bodies,
            final SkyMap map) {
        this(new OrthographicCamera(width, height), simulation, bodies, map);
    }

    public Gui(final OrthographicCamera camera,
            final Simulation simulation,
            final List<ProjectedBody> bodies,
            final SkyMap map) {
        super(new ScreenViewport(camera));
        camera.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);
        getStage().addActor(new PauseButton(simulation));
        getStage().addActor(new LabelToggleButton(map));
        getStage().addActor(new SpeedButton(simulation));
        getStage().addActor(new PlanetButtons(bodies, map));
    }
}
