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
import com.steel.silent.ui.SkyMap;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

public class PlanetButtons extends Group {
    private static final String STAR_CLASSIFICATION = "STAR";
    private static final String PLANET_CLASSIFICATION = "PLANET";
    private static final String MOON_CLASSIFICATION = "MOON";
    private static final List<String> INTERESTING_CLASSIFICATIONS = Arrays.asList(STAR_CLASSIFICATION,
            PLANET_CLASSIFICATION, MOON_CLASSIFICATION);
    private static Predicate<ProjectedBodyState> BODY_OF_INTEREST = body -> INTERESTING_CLASSIFICATIONS
            .contains(body.classification());

    private static final String TEXTURE = "button.png";
    private static final float PLANET_BUTTON_X = 0f;
    private static final float PLANET_BUTTON_HEIGHT = 64f;
    private static final float PLANET_BUTTON_GAP = 12f;
    private static final float PLANET_BUTTON_TOP_MARGIN = 116f + 100;

    public PlanetButtons(final List<ProjectedBodyState> bodies, final SkyMap map) {
        final Drawable drawable = getDrawable();
        final int[] index = { 0 };
        bodies.stream()
                .filter(BODY_OF_INTEREST)
                .forEach(body -> {
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
