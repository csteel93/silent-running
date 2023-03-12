package com.steel.silent;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;
import com.steel.silent.map.SolarSystem;
import com.steel.silent.simulation.Simulator;
import com.steel.silent.simulation.Universe;
import com.steel.silent.ui.UserInputConfigurations;
import com.steel.silent.ui.UserInputProcessor;
import com.steel.silent.ui.handler.KeyHandlerFactory;

import java.util.List;

public class SilentRunning extends ApplicationAdapter {

    private final float universe_width;
    private final float universe_height;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private ShapeRenderer renderer;

    private OrthographicCamera hudCam;
    private ScreenViewport hudViewport;

    private UserInputProcessor inputProcessor;

    private final Universe universe = new Universe();
    private final Simulator simulator = new Simulator(universe);

    public SilentRunning(final float width, final float height) {
        this.universe_width = width;
        this.universe_height = height;
    }

    @Override
    public void create() {

        // set camera
        System.out.println("setting orthographic camera");
        camera = new OrthographicCamera(universe_width, universe_height);
        camera.setToOrtho(false, universe_width, universe_height);
        viewport = new ExtendViewport(universe_width, universe_height, camera);

        hudCam = new OrthographicCamera(universe_width, universe_height);
        hudCam.setToOrtho(false, universe_width, universe_height);
        hudViewport = new ScreenViewport(hudCam);
        renderer = new ShapeRenderer();

        populateUniverse();

        // begin rendering
        System.out.println("beginning rendering");
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        universe.render(renderer);
        renderer.end();

        System.out.println("starting simulation");
        final Thread simulation = new Thread(simulator);
        simulation.start();
        System.out.println("simulation started");

        final UserInputConfigurations uiConfig =
                new UserInputConfigurations(3, 2);
        inputProcessor = new UserInputProcessor(KeyHandlerFactory.getKeyHandlers(camera, viewport, uiConfig));
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render() {

        inputProcessor.handlePressedKeys();
        camera.update();
        hudCam.update();

        // initialize rendering
        renderer.setProjectionMatrix(camera.combined);
        viewport.apply();
        ScreenUtils.clear(Color.BLACK);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        universe.render(renderer);

        renderer.setProjectionMatrix(hudCam.combined);
        hudViewport.apply();
        renderer.setColor(Color.WHITE);
        renderer.rect(0, Gdx.graphics.getHeight() - 35, 100, 50);

        // finalize rendering
        renderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        hudViewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        System.out.println("stopping simulation");
        simulator.stopSimulation();

        System.out.println("disposing: shapeRendered");
        renderer.dispose();
    }

    private void populateUniverse() {

        // populate solar system
        final FocalPoint sol = TestObjects.getFocalPoint(universe_width, universe_height);
        final List<Satellite> satellites = TestObjects.getSatellites(sol);
        final SolarSystem solarSystem = new SolarSystem(sol);
        solarSystem.withSatellites(satellites);

        universe.getSolarSystems().add(solarSystem);
    }

}
