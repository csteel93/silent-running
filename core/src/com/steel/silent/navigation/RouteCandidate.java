package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.simulation.OrbitalMechanics.Vec2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A partially or fully planned route used during beam search.
 *
 * <p>A candidate starts at the ship's initial state (no legs yet) and grows
 * one segment at a time. Each expansion appends a leg and advances the
 * candidate's "current state" (position, velocity, time) to the end of that
 * leg. The beam search in {@link Navigator} selects the best candidates at
 * each depth level and discards the rest.</p>
 *
 * <p>Candidates are immutable once created; {@link #withLeg} and
 * {@link #withScore} return new instances.</p>
 */
public final class RouteCandidate {

    /**
     * The body the candidate currently orbits/departs from, or {@code null}
     * when departing from a mid-space point (e.g., after a flyby exit).
     */
    public final CelestialBody currentBody;

    /** World-space departure position for the next segment. */
    public final Vec2 currentPosition;

    /** Normalized departure velocity direction for the next segment. */
    public final Vec2 currentVelocity;

    /** Simulation time at the start of the next segment. */
    public final long currentTime;

    /** All trajectory legs planned so far (empty for the initial candidate). */
    public final List<Trajectory.Leg> legs;

    /** Bodies already used as gravity-assist waypoints (prevents reuse). */
    public final Set<CelestialBody> usedAssistBodies;

    /** Aggregate candidate score (lower is better). Updated by {@link Navigator}. */
    public final double score;

    /** Total absolute delta-V estimated across all legs so far. */
    public final double estimatedDeltaV;

    /** Number of gravity-assist flyby segments included. */
    public final int gravityAssistCount;

    /** Number of avoidance-detour segments included. */
    public final int avoidanceCount;

    public RouteCandidate(final CelestialBody currentBody,
                          final Vec2 currentPosition,
                          final Vec2 currentVelocity,
                          final long currentTime,
                          final List<Trajectory.Leg> legs,
                          final Set<CelestialBody> usedAssistBodies,
                          final double score,
                          final double estimatedDeltaV,
                          final int gravityAssistCount,
                          final int avoidanceCount) {
        this.currentBody = currentBody;
        this.currentPosition = currentPosition;
        this.currentVelocity = currentVelocity;
        this.currentTime = currentTime;
        // Defensive copies make branch exploration safe: every route candidate
        // can be extended independently without mutating siblings in the beam.
        this.legs = List.copyOf(legs);
        this.usedAssistBodies = Set.copyOf(usedAssistBodies);
        this.score = score;
        this.estimatedDeltaV = estimatedDeltaV;
        this.gravityAssistCount = gravityAssistCount;
        this.avoidanceCount = avoidanceCount;
    }

    /** Build the initial candidate representing the ship's departure state. */
    public static RouteCandidate initial(final CelestialBody source,
                                          final Vec2 position,
                                          final Vec2 velocity,
                                          final long time) {
        return new RouteCandidate(source, position, velocity, time,
                List.of(), Set.of(), 0.0, 0.0, 0, 0);
    }

    /**
     * Return a new candidate with one additional leg appended.
     *
     * @param leg          the new trajectory leg
     * @param exitPosition world position at the end of the leg
     * @param exitVelocity normalized velocity direction at the end of the leg
     * @param nextBody     the body at the end of the leg (may be null)
     * @param legDeltaV    estimated delta-V for this leg (absolute value)
     * @param isAssist     true if this leg is a gravity-assist flyby
     * @param isAvoidance  true if this leg is an avoidance detour
     * @param assistBody   body used as the gravity assist (null if not an assist)
     */
    public RouteCandidate withLeg(final Trajectory.Leg leg,
                                   final Vec2 exitPosition,
                                   final Vec2 exitVelocity,
                                   final CelestialBody nextBody,
                                   final double legDeltaV,
                                   final boolean isAssist,
                                   final boolean isAvoidance,
                                   final CelestialBody assistBody) {
        final List<Trajectory.Leg> newLegs = new ArrayList<>(this.legs);
        newLegs.add(leg);
        final Set<CelestialBody> newUsed = new HashSet<>(this.usedAssistBodies);
        if (assistBody != null) {
            // Remember assist bodies so the search does not loop forever by
            // repeatedly using the same moon/planet as a gravity assist.
            newUsed.add(assistBody);
        }
        return new RouteCandidate(
                nextBody, exitPosition, exitVelocity, leg.arrivalSimTime,
                newLegs, newUsed,
                this.score, // updated separately by Navigator.scoreCandidate
                this.estimatedDeltaV + Math.abs(legDeltaV),
                this.gravityAssistCount + (isAssist ? 1 : 0),
                this.avoidanceCount + (isAvoidance ? 1 : 0));
    }

    /** Return a copy of this candidate with an updated score. */
    public RouteCandidate withScore(final double newScore) {
        return new RouteCandidate(currentBody, currentPosition, currentVelocity, currentTime,
                legs, usedAssistBodies, newScore, estimatedDeltaV, gravityAssistCount, avoidanceCount);
    }

    /** Assemble all legs into a complete {@link Trajectory}. */
    public Trajectory toTrajectory() {
        return new Trajectory(legs);
    }
}
