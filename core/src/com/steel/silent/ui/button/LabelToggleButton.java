package com.steel.silent.ui.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.steel.silent.ui.SkyMap;

public class LabelToggleButton extends TextButton {

    private static final String TEXTURE = "button.png";
    private static final String LABELS_ON = "LABELS";
    private static final String LABELS_OFF = "NO LABELS";
    private static final float X_COORD = 212f;
    private static final float WIDTH = 208f;
    private static final float HEIGHT = 100f;

    private final SkyMap map;

    public LabelToggleButton(final SkyMap map) {
        this(getDrawable(), map);
    }

    public LabelToggleButton(final Drawable drawable, final SkyMap map) {
        super(LABELS_ON, new TextButtonStyle(drawable, drawable, drawable, customFont()));
        this.map = map;
        setPosition(X_COORD, Gdx.graphics.getHeight() - HEIGHT);
        setSize(WIDTH, HEIGHT);
        getStyle().fontColor = Color.BLACK;
        setColor(Color.LIGHT_GRAY);
        addListener(interactionListener());
    }

     private static BitmapFont customFont(){
        final BitmapFont font = new BitmapFont();
        font.getData().setScale(1.75f);
        return font;
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }

    private EventListener interactionListener() {
        return new InputListener() {
            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer,final int button) {
                final boolean visible = !map.areLabelsVisible();
                map.setLabelsVisible(visible);
                setText(visible ? LABELS_ON : LABELS_OFF);
                return true;
            }
        };
    }
}
