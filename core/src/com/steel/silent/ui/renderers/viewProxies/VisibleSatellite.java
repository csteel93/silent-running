package com.steel.silent.ui.renderers.viewProxies;

import com.steel.silent.entity.Satellite;
import com.steel.silent.ui.renderers.Vec2d;

import lombok.Getter;

public class VisibleSatellite extends VisibleBody {

    @Getter
    private final Satellite satellite;

    public VisibleSatellite(final Satellite satellite, final MapScale mapScale) {
        super(satellite, mapScale);
        this.satellite = satellite;
    }

    public static VisibleSatellite fromSatellite(final Satellite satellite, final MapScale mapScale) {
        VisibleSatellite visibleSatellite = new VisibleSatellite(satellite, mapScale);
        // System.out.println(visibleSatellite.name() + " x: " + visibleSatellite.x());
        // System.out.println(visibleSatellite.name() + " y: " + visibleSatellite.y());
        // System.out.println(visibleSatellite.name() + " r: " +
        // visibleSatellite.radius());
        // System.out.println(visibleSatellite.name() + " i: " +
        // visibleSatellite.getInfluenceRadius());
        // System.out.println(visibleSatellite.name() + " e: " +
        // visibleSatellite.getEncounterRadius());

        return visibleSatellite;
    }

    public double getInfluenceRadius() {
        return mapScale.radius(satellite.getInfluenceRadius());
    }

    public double getEncounterRadius() {
        return Math.min(radius() * 50.0, getInfluenceRadius() * 0.10);
    }


    public Vec2d getWorldPositionMeters(double simTime){
        Vec2d pos = satellite.getWorldPositionMeters(simTime);
        return new Vec2d(mapScale.x(pos.x()), mapScale.y(pos.y()));
    }

    @Override
    public double x() {
        return mapScale.x(satellite);
    }

    @Override
    public double y() {
        return mapScale.y(satellite);
    }
}
