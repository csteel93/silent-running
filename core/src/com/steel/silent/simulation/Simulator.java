package com.steel.silent.simulation;

import java.util.concurrent.atomic.AtomicBoolean;

public class Simulator implements Runnable {

    private final Universe universe;
    private final AtomicBoolean simulating = new AtomicBoolean(true);

    public Simulator(final Universe universe) {
        this.universe = universe;
    }

    @Override
    public void run() {
        System.out.println("Started background simulation");
        long previousTime = System.currentTimeMillis();
        while (simulating.get()) {
            previousTime = universe.update(previousTime);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        System.out.println("simulation stopped");
    }

    public void stopSimulation() {
        simulating.set(false);
    }

}
