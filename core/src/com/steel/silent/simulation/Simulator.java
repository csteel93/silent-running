package com.steel.silent.simulation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulator implements Runnable {

    private final Universe universe;
    private final AtomicBoolean simulating = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger speed = new AtomicInteger(1);

    public Simulator(final Universe universe) {
        this.universe = universe;
    }

    @Override
    public void run() {
        System.out.println("Started background simulation");
        long previousTime = System.currentTimeMillis();
        simulating.set(true);
        while (simulating.get()) {
            previousTime = paused.get()
                ? System.currentTimeMillis()
                : universe.update(previousTime, speed.get());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        System.out.println("simulation stopped");
    }

    public int getSpeed() {
        return speed.get();
    }

    public int increaseSpeed(final int increase) {
        return speed.accumulateAndGet(increase, (Integer::sum));
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public void stop() {
        simulating.set(false);
    }
}
