package com.steel.silent.ui.renderers;

import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;

public interface EntityRenderer {

    void render(final ProjectedBodyState body, final Matrix4 projection);

}
