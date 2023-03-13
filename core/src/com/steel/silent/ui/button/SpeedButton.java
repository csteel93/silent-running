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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.steel.silent.simulation.Simulation;

import static com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class SpeedButton extends Group {

    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String TEXTURE = "button.png";
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private final TextButton increase;
    private final TextButton decrease;
    private final Label speedLabel;

    private int currentSpeed;

    public SpeedButton(final Simulation simulation) {
        this(simulation, getDrawable());
    }

    public SpeedButton(final Simulation simulation, final Drawable drawable) {
        increase = getButton(PLUS, drawable);
        increase.setPosition(Gdx.graphics.getWidth() - WIDTH, Gdx.graphics.getHeight() - HEIGHT);
        increase.addListener(interactionListener(simulation, 1));
        decrease = getButton(MINUS, drawable);
        decrease.setPosition(Gdx.graphics.getWidth() - WIDTH * 3, Gdx.graphics.getHeight() - HEIGHT);
        decrease.addListener(interactionListener(simulation, -1));

        speedLabel = getSpeedLabel();
        currentSpeed = simulation.getSpeed();
        speedLabel.setText(currentSpeed);

        addActor(speedLabel);
        addActor(increase);
        addActor(decrease);
    }

    private Label getSpeedLabel() {
        final Pixmap pixmap = new Pixmap(WIDTH * 2, HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fillRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
        final LabelStyle style = new LabelStyle(new BitmapFont(), Color.BLACK);
        style.background = new TextureRegionDrawable(new Texture(pixmap));
        final Label label = new Label("0", style);
        label.setPosition(Gdx.graphics.getWidth() - WIDTH * 2.5f, Gdx.graphics.getHeight() - HEIGHT);
        label.setAlignment(Align.center);
        return label;
    }

    private TextButton getButton(final String text, final Drawable drawable) {
        final TextButtonStyle style = new TextButtonStyle(drawable, drawable, drawable, new BitmapFont());
        final TextButton textButton = new TextButton(text, style);
        textButton.setSize(WIDTH, HEIGHT);
        textButton.getStyle().fontColor = Color.BLACK;
        textButton.setColor(Color.LIGHT_GRAY);
        return textButton;
    }

    private static Drawable getDrawable() {
        return new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal(TEXTURE)), 12, 12, 12, 12));
    }

    private EventListener interactionListener(final Simulation simulation, final int increase) {
        return new InputListener() {
            @Override
            public void touchUp(final InputEvent event, final float x, final float y, final int pointer, final int button) {
            }

            @Override
            public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer, final int button) {
                currentSpeed = simulation.increaseSpeed(increase);
                speedLabel.setText(currentSpeed);
                return true;
            }
        };
    }
}
