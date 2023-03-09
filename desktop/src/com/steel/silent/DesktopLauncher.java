package com.steel.silent;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.steel.silent.SilentRunning;
import org.apache.log4j.BasicConfigurator;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

    static {
        GdxNativesLoader.load();
    }

    private static final int UNIVERSE_WIDTH = 1000;
    private static final int UNIVERSE_HEIGHT = 1000;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Silent Running");
        config.setForegroundFPS(60);
        config.setWindowedMode(UNIVERSE_WIDTH, UNIVERSE_HEIGHT);
        BasicConfigurator.configure();
        new Lwjgl3Application(new SilentRunning(UNIVERSE_WIDTH, UNIVERSE_HEIGHT), config);
    }
}
