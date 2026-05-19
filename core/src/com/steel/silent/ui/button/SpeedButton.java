package com.steel.silent.ui.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.steel.silent.simulation.Simulation;

import static com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class SpeedButton extends Group {

    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String MAX = "MAX";
    private static final String TEXTURE = "button.png";
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;

    private final TextButton increase;
    private final TextButton decrease;
    private final TextButton max;
    private final Label speedLabel;

    private int currentSpeed;

    public SpeedButton(final Simulation simulation) {
        this(simulation, getDrawable());
    }

    public SpeedButton(final Simulation simulation, final Drawable drawable) {
        max = getButton(MAX, drawable);
        max.setPosition(Gdx.graphics.getWidth() - WIDTH * 1.2f, Gdx.graphics.getHeight() - HEIGHT);
        max.setSize(WIDTH * 1.2f, HEIGHT);
        max.addListener(maxSpeedListener(simulation));
        increase = getButton(PLUS, drawable);
        increase.setPosition(Gdx.graphics.getWidth() - WIDTH - max.getWidth(), Gdx.graphics.getHeight() - HEIGHT);
        increase.addListener(interactionListener(simulation, 1));
        decrease = getButton(MINUS, drawable);
        decrease.setPosition(Gdx.graphics.getWidth() - WIDTH * 3 - max.getWidth(), Gdx.graphics.getHeight() - HEIGHT);
        decrease.addListener(interactionListener(simulation, -1));

        speedLabel = getSpeedLabel();
        currentSpeed = simulation.getSpeed();
        updateSpeedLabel();

        addActor(max);
        addActor(speedLabel);
        addActor(increase);
        addActor(decrease);
    }

      private static BitmapFont customFont(){
        final BitmapFont font = new BitmapFont();
        font.getData().setScale(1.75f);
        return font;
    }

    private Label getSpeedLabel() {
        final Pixmap pixmap = new Pixmap(WIDTH * 2, HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fillRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
        final LabelStyle style = new LabelStyle(customFont(), Color.BLACK);
        style.background = new TextureRegionDrawable(new Texture(pixmap));
        final Label label = new Label("0", style);
        label.setPosition(Gdx.graphics.getWidth() - WIDTH * 2.5f - WIDTH * 1.2f, Gdx.graphics.getHeight() - HEIGHT);
        label.setAlignment(Align.center);
        return label;
    }

    private TextButton getButton(final String text, final Drawable drawable) {
        final TextButtonStyle style = new TextButtonStyle(drawable, drawable, drawable, customFont());
        final TextButton textButton = new TextButton(text, style);
        textButton.setSize(WIDTH, HEIGHT);
        textButton.getStyle().fontColor = Color.BLACK;
        textButton.setColor(Color.LIGHT_GRAY);
        return textButton;
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }

    private EventListener interactionListener(final Simulation simulation, final int direction) {
        return new InputListener() {
            @Override
            public void touchUp(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
            }

            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
                currentSpeed = simulation.increaseSpeed(direction);
                updateSpeedLabel();
                return true;
            }
        };
    }

    private EventListener maxSpeedListener(final Simulation simulation) {
        return new InputListener() {
            @Override
            public void touchUp(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
            }

            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer,
                    final int button) {
                currentSpeed = simulation.maxSpeed();
                updateSpeedLabel();
                return true;
            }
        };
    }

    private void updateSpeedLabel() {
        speedLabel.setText(currentSpeed + "x");
    }
}
