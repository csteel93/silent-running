package com.steel.silent.simulation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulator implements Runnable {

    private static final int MIN_SPEED_MULTIPLIER = 1;
    private static final int MAX_SPEED_MULTIPLIER = 4096;

    private final Universe universe;
    private final AtomicBoolean simulating = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger speedMultiplier = new AtomicInteger(MIN_SPEED_MULTIPLIER);

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
                : universe.update(previousTime, speedMultiplier.get());
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
        return speedMultiplier.get();
    }

    public int increaseSpeed(final int direction) {
        return speedMultiplier.updateAndGet(current -> {
            if (direction > 0) {
                return Math.min(MAX_SPEED_MULTIPLIER, current * 2);
            }
            if (direction < 0) {
                return Math.max(MIN_SPEED_MULTIPLIER, current / 2);
            }
            return current;
        });
    }

    public int maxSpeed() {
        return speedMultiplier.updateAndGet(current -> MAX_SPEED_MULTIPLIER);
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
