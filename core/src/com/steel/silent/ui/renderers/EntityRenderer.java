package com.steel.silent.ui.renderers;

import com.badlogic.gdx.math.Matrix4;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBody;

public interface EntityRenderer {

    void render(final ProjectedBody body, final Matrix4 projection);

}
