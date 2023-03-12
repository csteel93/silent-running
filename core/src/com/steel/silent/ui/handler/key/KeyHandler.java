package com.steel.silent.ui.handler.key;

public interface KeyHandler {

    int getKeycode();

    boolean canHold();

    void handleKey();
}
