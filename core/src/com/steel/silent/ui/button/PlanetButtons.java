package com.steel.silent.ui.button;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.steel.silent.entity.CelestialBody;
import com.steel.silent.entity.IdentifiableBody;
import com.steel.silent.simulation.Universe;
import com.steel.silent.ui.SkyMap;

public class PlanetButtons extends Group {
    private static final String STAR_CLASSIFICATION = "STAR";
    private static final String PLANET_CLASSIFICATION = "PLANET";
    private static final List<String> INTERESTING_CLASSIFICATIONS = Arrays.asList(STAR_CLASSIFICATION,
            PLANET_CLASSIFICATION);
    private static Predicate<IdentifiableBody> BODY_OF_INTEREST = body -> body instanceof CelestialBody
            && INTERESTING_CLASSIFICATIONS.contains(body.classification());

    private static final String TEXTURE = "button.png";
    private static final float PLANET_BUTTON_X = 0f;
    private static final float PLANET_BUTTON_HEIGHT = 32f;
    private static final float PLANET_BUTTON_GAP = 6f;
    private static final float PLANET_BUTTON_TOP_MARGIN = 58f + 50;

    public PlanetButtons(final Universe universe, final SkyMap map) {
        System.out.println("Generating body zoom buttons");
        final Drawable drawable = getDrawable();
        final int[] index = { 0 };
        universe.getState()
                .filter(BODY_OF_INTEREST)
                .map(body -> (CelestialBody) body)
                .forEach(body -> {
                    System.out.println("Generating zoom button for " + body.name());
                    final PlanetButton button = new PlanetButton(body, drawable, map);
                    button.setPosition(PLANET_BUTTON_X, getNextY(index[0]));
                    addActor(button);
                    index[0]++;
                });

    }

    private static float getNextY(final int index) {
        return Gdx.graphics.getHeight()
                - PLANET_BUTTON_TOP_MARGIN
                - (PLANET_BUTTON_HEIGHT + PLANET_BUTTON_GAP) * index;
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }

}
