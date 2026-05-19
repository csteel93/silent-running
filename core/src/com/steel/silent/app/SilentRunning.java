package com.steel.silent.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.steel.silent.model.body.FocalPoint;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.simulation.Simulation;
import com.steel.silent.simulation.Simulator;
import com.steel.silent.simulation.SolarSystem;
import com.steel.silent.simulation.Universe;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.ui.Gui;
import com.steel.silent.ui.SkyMap;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

import java.util.List;

public class SilentRunning extends ApplicationAdapter {

    private final float universeWidth;
    private final float universeHeight;

    private final Universe universe = new Universe();
    private final Simulator simulator = new Simulator(universe);
    private final Simulation simulation = new Simulation(simulator);

    private SkyMap skyMap;
    private Gui gui;

    public SilentRunning(final float width, final float height) {
        this.universeWidth = width;
        this.universeHeight = height;
    }

    @Override
    public void create() {
        // Body radii need visual exaggeration; orbital distances are scaled to the map size.
        final double bodyExaggeration = 60.0;

        populateUniverse();

        final SimulationSnapshot initialSnapshot = universe.buildSnapshot();
        final WorldProjection projection = WorldProjection.fromSnapshot(
                initialSnapshot, universeWidth, universeHeight, bodyExaggeration);

        final List<ProjectedBodyState> bodies = initialSnapshot.bodies().stream()
                .map(body -> new ProjectedBodyState(body, projection))
                .toList();

        skyMap = new SkyMap(universeWidth, universeHeight, universe, projection);
        gui = new Gui(universeWidth, universeHeight, simulation, bodies, skyMap);

        skyMap.render();
        gui.render();

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
        simulation.stop();
        skyMap.dispose();
    }

    private void populateUniverse() {
        final FocalPoint sol = DemoUniverseFactory.getSol();
        final List<Satellite> satellites = DemoUniverseFactory.getSatellites(sol);
        final SolarSystem solarSystem = new SolarSystem(sol);
        solarSystem.withSatellites(satellites);
        universe.withSolarSystem(solarSystem);
    }
}
