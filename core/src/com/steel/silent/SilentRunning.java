package com.steel.silent;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;
import com.steel.silent.map.SolarSystem;
import com.steel.silent.simulation.Simulation;
import com.steel.silent.simulation.Simulator;
import com.steel.silent.simulation.Universe;
import com.steel.silent.ui.Gui;
import com.steel.silent.ui.SkyMap;

import java.util.List;

public class SilentRunning extends ApplicationAdapter {

    private final float universe_width;
    private final float universe_height;

    private final Universe universe = new Universe();
    private final Simulator simulator = new Simulator(universe);
    private final Simulation simulation = new Simulation(simulator);

    private SkyMap skyMap;
    private Gui gui;

    public SilentRunning(final float width, final float height) {
        this.universe_width = width;
        this.universe_height = height;
    }

    @Override
    public void create() {
        populateUniverse();

        skyMap = new SkyMap(universe_width, universe_height, universe);
        gui = new Gui(universe_width, universe_height, simulation);

        System.out.println("beginning rendering");
        skyMap.render();
        gui.render();

        System.out.println("starting simulation");
        simulation.start();

        final InputMultiplexer inputMultiplexer = new InputMultiplexer();
        skyMap.registerInput(inputMultiplexer);
        gui.registerInput(inputMultiplexer);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render() {
        skyMap.render();
        gui.render();
    }

    @Override
    public void resize(final int width, final int height) {
        skyMap.update(width, height);
        gui.update(width, height);
    }

    @Override
    public void dispose() {
        System.out.println("stopping simulation");
        simulation.stop();

        System.out.println("disposing: shapeRendered");
        skyMap.dispose();
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
