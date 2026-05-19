package com.steel.silent.math;

public record Vector(double x, double y) {

    public Vector add(final Vector other) {
        return new Vector(x + other.x, y + other.y);
    }

    public Vector sub(final Vector other) {
        return new Vector(x - other.x, y - other.y);
    }

    public Vector scl(final double scalar) {
        return new Vector(x * scalar, y * scalar);
    }

    public double len() {
        return Math.sqrt(x * x + y * y);
    }

    public double len2() {
        return x * x + y * y;
    }

    public double cross(final Vector other) {
        return x * other.y - y * other.x;
    }

    public Vector nor() {
        double length = len();
        if (length == 0.0) {
            return new Vector(0.0, 0.0);
        }
        return new Vector(x / length, y / length);
    }

    public static Vector ZERO(){
        return new Vector(0, 0);
    }
}
