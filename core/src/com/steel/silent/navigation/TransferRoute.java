package com.steel.silent.navigation;

import java.util.List;

/**
 * An ordered sequence of {@link TransferLeg}s that together describe a complete
 * transfer route from a source body (or parking orbit) to a destination body
 * (or parking orbit).
 *
 * <p>Single-leg routes are used for same-frame transfers (parking orbit ↔ moon).
 * Multi-leg routes are used for cross-planet transfers (moon → other planet).
 */
public record TransferRoute(List<TransferLeg> legs) {}
