package org.orego.app.face3dActivity.model3D.model.object3DImplementation.versions;

import org.orego.app.face3dActivity.model3D.model.object3DImplementation.AbstractObject3D;

/**
 * Created by StarKRE on 07.04.2018.
 */

public final class Object3DV1 extends AbstractObject3D {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "void main() {" +
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 20.0;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    public Object3DV1() {
        super("V1", vertexShaderCode, fragmentShaderCode, "a_Position");
    }

    @Override
    protected boolean supportsColors() {
        return false;
    }
}