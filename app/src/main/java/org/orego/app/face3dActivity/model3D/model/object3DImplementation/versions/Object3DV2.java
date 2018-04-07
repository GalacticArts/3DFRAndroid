package org.orego.app.face3dActivity.model3D.model.object3DImplementation.versions;

import org.orego.app.face3dActivity.model3D.model.object3DImplementation.AbstractObject3D;

/**
 * Created by StarKRE on 07.04.2018.
 */

public final class Object3DV2 extends AbstractObject3D {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  vColor = a_Color;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public Object3DV2() {
        super("V2", vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }
}