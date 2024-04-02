package com.steel.silent.ui.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.simulation.Universe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class UniverseRenderer {

    private final Universe universe;
    private final ShapeRenderer shapeRenderer;
    private final Map<IdentifiableBody, EntityRenderer> entityRenderers = new HashMap<>();
    private final Map<String, Function<IdentifiableBody, EntityRenderer>> rendererSupplier;

    public UniverseRenderer(final Universe universe, final ShapeRenderer shapeRenderer) {
        this.universe = universe;
        this.shapeRenderer = shapeRenderer;
        rendererSupplier = new HashMap<String, Function<IdentifiableBody, EntityRenderer>>() {{
            put("STAR", body -> new EntityTextureRenderer(body, new Texture(Gdx.files.internal("sun.png"))));
        }};
        loadRenderers(universe);
    }

    public void render(final Matrix4 projection) {
        universe.getState()
            .forEach(body -> entityRenderers
                .getOrDefault(body, new DefaultEntityRenderer(body, shapeRenderer))
                .render(projection));
    }

    private void loadRenderers(final Universe universe) {
        universe.getState().forEach(body -> {
            Optional.ofNullable(rendererSupplier.get(body.classification()))
                    .ifPresent(func -> entityRenderers.put(body, func.apply(body)));
        });
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
