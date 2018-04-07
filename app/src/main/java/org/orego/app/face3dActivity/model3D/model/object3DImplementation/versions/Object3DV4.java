package org.orego.app.face3dActivity.model3D.model.object3DImplementation.versions;

import org.orego.app.face3dActivity.model3D.model.object3DImplementation.AbstractObject3D;

/**
 * Created by StarKRE on 07.04.2018.
 */

public final class Object3DV4 extends AbstractObject3D {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;" +
                    "varying vec4 vColor;" +
                    "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  vColor = a_Color;" +
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";

    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);" +
                    "}";

    public Object3DV4() {
        super("V4", vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color", "a_TexCoordinate");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }

}