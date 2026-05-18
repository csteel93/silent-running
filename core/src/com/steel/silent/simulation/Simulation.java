package com.steel.silent.simulation;

public class Simulation {

    private final Thread thread;
    private final Simulator simulator;

    public Simulation(final Simulator simulator) {
        this.thread = new Thread(simulator);
        this.simulator = simulator;
    }

    public void start() {
        thread.start();
    }

    public int getSpeed(){
        return simulator.getSpeed();
    }

    public int increaseSpeed(final int direction){
        return simulator.increaseSpeed(direction);
    }

    public void pause() {
        simulator.pause();
    }

    public void resume() {
        simulator.resume();
    }

    public void stop() {
        simulator.stop();
    }
}
