package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A visual flight plan made of cubic transfer arcs.
 *
 * This is intentionally not a Newtonian trajectory. Each leg captures the
 * world-space departure point, destination intercept point, and the tangent
 * directions the ship should follow at both ends. Sampling the same curve is
 * used for movement, rendering, collision checks, and ship heading.
 */
public final class Trajectory {

    @Getter
    private final List<Leg> legs;

    public Trajectory(final List<Leg> legs) {
        // Copy into an unmodifiable list so a planned trajectory is a stable
        // snapshot. The simulation, renderer, and validators can all sample it
        // without worrying that another caller will mutate the leg list midway.
        this.legs = Collections.unmodifiableList(new ArrayList<>(legs));
    }

    public Sample sampleAt(final long simTime) {
        if (legs.isEmpty()) {
            return new Sample(0, 0, true, null);
        }
        final Leg first = legs.get(0);
        if (simTime <= first.departureSimTime) {
            // A future launch window means the ship is still waiting in its
            // current orbit. The Ship class handles that waiting behavior; this
            // sample only reports the planned transfer's departure point.
            return new Sample(first.departureX, first.departureY, false, null);
        }
        for (final Leg leg : legs) {
            if (simTime <= leg.arrivalSimTime) {
                // Convert absolute simulation time into normalized curve
                // progress. All route consumers use the same cubic position,
                // so the drawn line, collision samples, and ship movement agree.
                final double t = leg.progressAt(simTime);
                return new Sample(leg.xAt(t), leg.yAt(t), false, null);
            }
        }
        // Past the final leg: report arrival and tell Ship which body to enter
        // orbit around.
        final Leg last = legs.get(legs.size() - 1);
        return new Sample(last.arrivalX, last.arrivalY, true, last.destination);
    }

    public long departureSimTime() {
        return legs.isEmpty() ? 0 : legs.get(0).departureSimTime;
    }

    public long arrivalSimTime() {
        return legs.isEmpty() ? 0 : legs.get(legs.size() - 1).arrivalSimTime;
    }

    public CelestialBody finalDestination() {
        return legs.isEmpty() ? null : legs.get(legs.size() - 1).destination;
    }

    public double fuelCost() {
        // "Fuel" is a gameplay/route-scoring proxy, not a real propellant
        // calculation. Legs decide their local cost; the trajectory sums them.
        double cost = 0;
        for (final Leg leg : legs) {
            cost += leg.fuelCost;
        }
        return cost;
    }

    public static final class Leg {
        private static final int LENGTH_SAMPLES = 36;

        // origin/destination are not just labels: validation skips them because
        // deliberately leaving/arriving near those bodies should not count as a
        // collision.
        public final CelestialBody origin;
        public final CelestialBody destination;

        // Absolute simulation times. These are in Universe simulation
        // milliseconds, so changing simulation speed changes how quickly the
        // wall clock reaches them, not the planned path.
        public final long departureSimTime;
        public final long arrivalSimTime;

        // Cubic Bezier endpoint positions.
        public final double departureX;
        public final double departureY;
        public final double arrivalX;
        public final double arrivalY;

        // Cubic Bezier control points. control1 shapes the departure tangent;
        // control2 shapes the arrival tangent.
        public final double control1X;
        public final double control1Y;
        public final double control2X;
        public final double control2Y;

        // Normalized tangent directions captured at planning time. These are
        // retained for scoring/debugging even though the cubic derivative is
        // the authoritative direction during flight.
        public final double departureVelocityX;
        public final double departureVelocityY;
        public final double arrivalVelocityX;
        public final double arrivalVelocityY;
        public final double pathLength;
        public final double fuelCost;
        public final double cruiseSpeed;

        public Leg(final CelestialBody origin,
                   final CelestialBody destination,
                   final long departureSimTime,
                   final long arrivalSimTime,
                   final double departureX,
                   final double departureY,
                   final double arrivalX,
                   final double arrivalY,
                   final double control1X,
                   final double control1Y,
                   final double control2X,
                   final double control2Y,
                   final double departureVelocityX,
                   final double departureVelocityY,
                   final double arrivalVelocityX,
                   final double arrivalVelocityY,
                   final double cruiseSpeed,
                   final double fuelCost) {
            this.origin = origin;
            this.destination = destination;
            this.departureSimTime = departureSimTime;
            this.arrivalSimTime = arrivalSimTime;
            this.departureX = departureX;
            this.departureY = departureY;
            this.arrivalX = arrivalX;
            this.arrivalY = arrivalY;
            this.control1X = control1X;
            this.control1Y = control1Y;
            this.control2X = control2X;
            this.control2Y = control2Y;
            this.departureVelocityX = departureVelocityX;
            this.departureVelocityY = departureVelocityY;
            this.arrivalVelocityX = arrivalVelocityX;
            this.arrivalVelocityY = arrivalVelocityY;
            this.cruiseSpeed = cruiseSpeed;
            this.pathLength = approximateLength();
            this.fuelCost = fuelCost;
        }

        public double progressAt(final long simTime) {
            // Clamp to [0, 1] so a caller that is slightly early/late never
            // samples outside the Bezier curve.
            final long span = Math.max(1, arrivalSimTime - departureSimTime);
            final double raw = (double) (simTime - departureSimTime) / (double) span;
            return Math.max(0.0, Math.min(1.0, raw));
        }

        public double xAt(final double t) {
            // Cubic Bezier basis:
            // P(t) = (1-t)^3 P0 + 3(1-t)^2t C1 + 3(1-t)t^2 C2 + t^3 P1.
            final double u = 1.0 - t;
            return u * u * u * departureX
                + 3.0 * u * u * t * control1X
                + 3.0 * u * t * t * control2X
                + t * t * t * arrivalX;
        }

        public double yAt(final double t) {
            final double u = 1.0 - t;
            return u * u * u * departureY
                + 3.0 * u * u * t * control1Y
                + 3.0 * u * t * t * control2Y
                + t * t * t * arrivalY;
        }

        public double dxAt(final double t) {
            // First derivative of the cubic Bezier x coordinate. The renderer
            // uses this for ship facing; validators can also reason about the
            // local path direction.
            final double u = 1.0 - t;
            return 3.0 * u * u * (control1X - departureX)
                + 6.0 * u * t * (control2X - control1X)
                + 3.0 * t * t * (arrivalX - control2X);
        }

        public double dyAt(final double t) {
            final double u = 1.0 - t;
            return 3.0 * u * u * (control1Y - departureY)
                + 6.0 * u * t * (control2Y - control1Y)
                + 3.0 * t * t * (arrivalY - control2Y);
        }

        private double approximateLength() {
            // Bezier arc length has no cheap closed form, so we approximate by
            // walking small straight segments. This is good enough for route
            // scoring and UI summaries.
            double length = 0;
            double prevX = departureX;
            double prevY = departureY;
            for (int i = 1; i <= LENGTH_SAMPLES; i++) {
                final double t = (double) i / LENGTH_SAMPLES;
                final double x = xAt(t);
                final double y = yAt(t);
                length += distance(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
            return length;
        }

        private static double distance(final double ax, final double ay, final double bx, final double by) {
            final double dx = bx - ax;
            final double dy = by - ay;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    public static final class Sample {
        public final double x;
        public final double y;
        public final boolean arrived;
        public final CelestialBody arrivalBody;

        public Sample(final double x, final double y, final boolean arrived, final CelestialBody arrivalBody) {
            this.x = x;
            this.y = y;
            this.arrived = arrived;
            this.arrivalBody = arrivalBody;
        }
    }
}
