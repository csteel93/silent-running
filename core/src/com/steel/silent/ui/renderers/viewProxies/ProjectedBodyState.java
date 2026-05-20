package com.steel.silent.ui.renderers.viewProxies;

import java.util.UUID;

import com.steel.silent.simulation.snapshot.BodyState;

/**
 * A read-only view of a single body's simulation state projected into
 * camera-relative rendering space.
 *
 * <p>All coordinate values ({@link #x()}, {@link #y()}) are metre offsets
 * from the camera centre, computed in double precision and stored as
 * float. {@link #radius()} is the rendered radius in those same metre
 * units, already clamped to a minimum of 3 screen pixels.
 * {@link #radiusPixels()} is the screen-pixel equivalent, useful for
 * deciding segment counts and frustum tests.</p>
 *
 * <p>Callers should never cache instances across render frames.</p>
 */
public class ProjectedBodyState {

    private final BodyState body;
    private final float relX;
    private final float relY;
    private final float renderRadiusMeters;
    private final float renderRadiusPixels;

    public ProjectedBodyState(final BodyState body, final WorldProjection projection) {
        this.body = body;
        this.relX = projection.relX(body.positionMeters().x());
        this.relY = projection.relY(body.positionMeters().y());
        this.renderRadiusMeters = projection.renderRadiusMeters(body.radiusMeters());
        this.renderRadiusPixels = projection.metersToPixels(renderRadiusMeters);
    }

    public UUID id() {
        return body.bodyId();
    }

    public String name() {
        return body.name();
    }

    public String classification() {
        return body.classification();
    }

    public String color() {
        return body.color();
    }

    /** Camera-relative X position in metres (pass directly to ShapeRenderer). */
    public float x() {
        return relX;
    }

    /** Camera-relative Y position in metres (pass directly to ShapeRenderer). */
    public float y() {
        return relY;
    }

    /**
     * Rendered radius in metres (pass directly to ShapeRenderer). Already
     * enforces the 3-pixel minimum floor from {@link WorldProjection}.
     */
    public float radius() {
        return renderRadiusMeters;
    }

    /**
     * Rendered radius in screen pixels. Use this for segment-count decisions,
     * frustum-radius tests, and label positioning — not for ShapeRenderer calls.
     */
    public float radiusPixels() {
        return renderRadiusPixels;
    }

    /**
     * Influence-sphere radius in metres (raw, not camera-relative). Pass
     * directly to a ShapeRenderer already centred at {@link #x()}, {@link #y()}.
     */
    public float getInfluenceRadius() {
        return (float) body.influenceRadius();
    }

    /**
     * Close-approach encounter zone radius (20 % of influence sphere), in metres.
     */
    public float getEncounterRadius() {
        return (float) (body.influenceRadius() * 0.20);
    }

    public double orientationRad() {
        return body.orientationRad();
    }

    public BodyState bodyState() {
        return body;
    }
}
