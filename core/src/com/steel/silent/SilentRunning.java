package com.steel.silent;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

import java.util.List;

public class SilentRunning extends ApplicationAdapter {

    private final float universe_width;
    private final float universe_height;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private ShapeRenderer renderer;

    private OrthographicCamera hudCam;
    private ScreenViewport hudViewport;

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
    }

    @Override
    public void render() {
        handleInput();
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

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        hudViewport.update(width,height,false);
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
