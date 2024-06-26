package com.steel.silent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.steel.silent.entity.FocalPoint;
import com.steel.silent.entity.Satellite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TestObjects {

    public static FocalPoint getFocalPoint(float scaled_width, float scaled_height) {
        final FocalPoint sol = new FocalPoint(
            bd(36),
            bd(scaled_width).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(scaled_height).divide(bd(2), RoundingMode.UNNECESSARY),
            bd(Duration.ofMinutes(2).toMillis()));
        sol.getCharacteristics().setColor(Color.YELLOW.toString());
        sol.getCharacteristics().setName("Sol");
        sol.getCharacteristics().setClassification("STAR");
        return sol;
    }

    public static List<Satellite> getSatellites(FocalPoint sol) {
        final Satellite argon = new Satellite(sol, bd(10), bd(400), bd(Duration.ofMinutes(10).toMillis()));
        argon.getCharacteristics().setColor(Color.BLUE.toString());
        argon.getCharacteristics().setName("Argon Prime");
        final Satellite argonOne = new Satellite(argon, bd(3), bd(80), bd(Duration.ofMinutes(5).toMillis()));
        argonOne.getCharacteristics().setColor(Color.LIGHT_GRAY.toString());
        argonOne.getCharacteristics().setName("Argon One");
        final Satellite argonTwo = new Satellite(argon, bd(4), bd(110), bd(Duration.ofMinutes(5).toMillis()));
        argonTwo.getCharacteristics().setColor(Color.TEAL.toString());
        argonTwo.getCharacteristics().setName("Argon Two");

        final Satellite boron = new Satellite(sol, bd(10), bd(800), bd(Duration.ofMinutes(15).toMillis()));
        boron.getCharacteristics().setColor(Color.PURPLE.toString());
        boron.getCharacteristics().setName("Boron Prime");
        final Satellite boronOne = new Satellite(boron, bd(3), bd(80), bd(Duration.ofMinutes(10).toMillis()));
        boronOne.getCharacteristics().setColor(Color.GREEN.toString());
        boronOne.getCharacteristics().setName("Boron One");
        final Satellite boronTwo = new Satellite(boron, bd(4), bd(110), bd(Duration.ofMinutes(5).toMillis()));
        boronTwo.getCharacteristics().setColor(Color.CORAL.toString());
        boronTwo.getCharacteristics().setName("Boron Two");

        return Arrays.asList(argon, argonOne, argonTwo, boron, boronOne, boronTwo);
    }

    private static BigDecimal bd(final double val) {
        return BigDecimal.valueOf(val);
    }
}
