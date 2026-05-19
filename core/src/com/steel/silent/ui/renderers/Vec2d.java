package com.steel.silent.ui.renderers;

public record Vec2d(double x, double y) {

    public Vec2d add(Vec2d other) {
        return new Vec2d(x + other.x, y + other.y);
    }

    public Vec2d sub(Vec2d other) {
        return new Vec2d(x - other.x, y - other.y);
    }

    public Vec2d scl(double scalar) {
        return new Vec2d(x * scalar, y * scalar);
    }

    public double len() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2d nor() {
        double length = len();

        if (length == 0.0) {
            return new Vec2d(0.0, 0.0);
        }

        return new Vec2d(x / length, y / length);
    }
}