package com.steel.silent.ui.handler.key;

import com.badlogic.gdx.Input;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.entity.Ship;
import com.steel.silent.navigation.Navigator;
import com.steel.silent.navigation.Trajectory;
import com.steel.silent.simulation.Universe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Debug command: pressing G cycles the test ship's destination through every
 * non-source body in the universe and asks the {@link Navigator} to plan a
 * trajectory to it. The ship enters TRANSITING state if a plan is found.
 *
 * Holding G does not repeat — one press per command.
 */
public class ShipCommandKeyHandler implements KeyHandler {

    private final Ship ship;
    private final Universe universe;
    private final double cruiseSpeed;
    private int destinationIndex = -1;

    public ShipCommandKeyHandler(final Ship ship, final Universe universe, final double cruiseSpeed) {
        this.ship = ship;
        this.universe = universe;
        this.cruiseSpeed = cruiseSpeed;
    }

    @Override
    public int getKeycode() {
        return Input.Keys.G;
    }

    @Override
    public boolean canHold() {
        return false;
    }

    @Override
    public void handleKey() {
        final List<CelestialBody> candidates = collectCandidates();
        if (candidates.isEmpty()) {
            System.out.println("[ship-command] no candidate destinations available");
            return;
        }
        destinationIndex = (destinationIndex + 1) % candidates.size();
        final CelestialBody destination = candidates.get(destinationIndex);
        // Skip the body the ship is currently around so we always move.
        if (ship.getParentBody() != null && destination.equals(ship.getParentBody())) {
            destinationIndex = (destinationIndex + 1) % candidates.size();
        }
        final CelestialBody target = candidates.get(destinationIndex);

        final Optional<Trajectory> plan = Navigator.route(ship, target, universe, cruiseSpeed);
        if (plan.isPresent()) {
            ship.commandTrajectory(plan.get());
            System.out.printf(
                "[ship-command] ship %s -> %s | %d legs, ETA sim+%d ms%n",
                ship.name(),
                target.name(),
                plan.get().getLegs().size(),
                plan.get().arrivalSimTime() - universe.getSimTime());
        } else {
            System.out.printf("[ship-command] no route from %s to %s%n",
                ship.getParentBody() != null ? ship.getParentBody().name() : "?",
                target.name());
        }
    }

    private List<CelestialBody> collectCandidates() {
        final List<CelestialBody> out = new ArrayList<>();
        for (final IdentifiableBody body : (Iterable<IdentifiableBody>) universe.getState()::iterator) {
            if (body instanceof CelestialBody cb && !(body instanceof Ship)) {
                out.add(cb);
            }
        }
        return out;
    }
}
