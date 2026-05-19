package com.steel.silent.ui.renderers;

import com.badlogic.gdx.math.Vector2;
import com.steel.silent.math.Vector;
import com.steel.silent.model.body.Satellite;
import com.steel.silent.ui.renderers.viewProxies.MapScale;

// public class RenderProjection {
//     private final MapScale mapScale;

//     public RenderProjection(MapScale mapScale) {
//         this.mapScale = mapScale;
//     }

//     public Vector2 toRenderPosition(Vector worldMeters) {
//         return new Vector2(
//                 (float) mapScale.x(worldMeters.x()),
//                 (float) mapScale.y(worldMeters.y()));
//     }

//     public Vector2 toRenderPosition(Satellite satellite) {
//         return new Vector2(
//                 (float) mapScale.x(satellite),
//                 (float) mapScale.y(satellite));
//     }

//     public float toRenderBodyRadius(double radiusMeters) {
//         return (float) mapScale.radius(radiusMeters);
//     }

//     public float toRenderDistance(double meters) {
//         return (float) (meters / mapScale.getMetersPerMapUnit());
//     }
// }
