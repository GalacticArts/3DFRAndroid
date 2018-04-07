package org.orego.app.face3dActivity.model3D.model.object3DImplementation.versions;

import org.orego.app.face3dActivity.model3D.model.object3DImplementation.AbstractObject3D;

/**
 * Created by StarKRE on 07.04.2018.
 */

public final class Object3DV7 extends AbstractObject3D {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    // color
                    "uniform vec4 vColor;\n" +
                    // light variables
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec3 a_Normal;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n          " +
                    // Get a lighting direction vector from the light to the vertex.
                    "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n    " +
                    // Transform the normal's orientation into eye space.
                    "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n " +
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n   " +
                    // Attenuate the light based on distance.
                    "   float distance = length(u_LightPos - modelViewVertex);\n         " +
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.05 * distance * distance)));\n" +
                    //  Add ambient lighting
                    "  diffuse = diffuse + 0.5;" +
                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    "   v_Color = vColor * diffuse;\n" +
                    "   v_Color[3] = vColor[3];" + // correct alpha
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = v_Color;\n" +
                    "}";

    public Object3DV7() {
        super("V7", vertexShaderCode, fragmentShaderCode, "a_Position", "a_Normal");
    }

    @Override
    protected boolean supportsColors() {
        return false;
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsLighting() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}