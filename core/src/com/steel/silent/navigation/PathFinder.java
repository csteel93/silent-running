package com.steel.silent.navigation;

import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.Satellite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hierarchy utilities for the orbital route planner.
 *
 * <p>The candidate-route generation previously housed here has been absorbed by
 * {@link Navigator}'s beam search, which dynamically evaluates gravity-assist
 * candidates rather than pre-generating fixed route templates.</p>
 *
 * <p>What remains are two small helpers useful for debugging and for computing
 * the common-ancestor relationship between bodies:</p>
 * <ul>
 *   <li>{@link #ancestorChain} — builds the chain from a body to its root.</li>
 *   <li>{@link #buildParentMap} — flat map of body → parent for all bodies.</li>
 * </ul>
 */
public final class PathFinder {

    private PathFinder() {}

    /**
     * Ancestor chain of a body, inclusive, from the body itself up to the
     * root (a body with no parent). The first element is always {@code body}.
     */
    public static List<CelestialBody> ancestorChain(final CelestialBody body) {
        final List<CelestialBody> chain = new ArrayList<>();
        CelestialBody current = body;
        while (current != null) {
            chain.add(current);
            // Only satellites have parents in this model. Focal points are
            // roots, so the chain ends once current is no longer a Satellite.
            current = current instanceof final Satellite sat ? sat.getFocalPoint() : null;
        }
        return Collections.unmodifiableList(chain);
    }

    /**
     * Flat map of body → parent for every body in the collection.
     * Focal points (root stars) map to {@code null}.
     */
    public static Map<CelestialBody, CelestialBody> buildParentMap(
            final Iterable<? extends CelestialBody> allBodies) {
        final Map<CelestialBody, CelestialBody> parents = new HashMap<>();
        for (final CelestialBody body : allBodies) {
            // This mirrors ancestorChain's parent rule in a lookup-friendly
            // form for debugging and future route heuristics.
            parents.put(body, body instanceof final Satellite sat ? sat.getFocalPoint() : null);
        }
        return Collections.unmodifiableMap(parents);
    }
}
