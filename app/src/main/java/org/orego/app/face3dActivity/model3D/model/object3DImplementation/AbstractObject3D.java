package org.orego.app.face3dActivity.model3D.model.object3DImplementation;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.model.Object3D;
import org.orego.app.face3dActivity.model3D.model.Object3DData;
import org.orego.app.face3dActivity.model3D.util.GLUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;


public abstract class AbstractObject3D implements Object3D {
    // Transformations
    private final float[] mMatrix = new float[16];
    // mvp matrix
    private final float[] mvMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    // OpenGL data
    private final int mProgram;
    private double shift = -1d;

    public AbstractObject3D(String id, String vertexShaderCode, String fragmentShaderCode, String... variables) {
        // prepare shaders and OpenGL program
        int vertexShader = GLUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLUtil.createAndLinkProgram(vertexShader, fragmentShader, variables);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int textureId, float[] lightPos) {
        this.draw(obj, pMatrix, vMatrix, obj.getDrawMode(), obj.getDrawSize(), textureId, lightPos);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int drawMode, int drawSize, int textureId,
                     float[] lightPos) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        float[] mMatrix = getMMatrix(obj);
        float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
        float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

        setMvpMatrix(mvpMatrix);

        int mPositionHandle = setPosition(obj);

        int mColorHandle = -1;
        if (supportsColors()) {
            mColorHandle = setColors(obj);
        } else {
            setColor(obj);
        }

        if (textureId != -1 && supportsTextures()) {
            setTexture(obj, textureId);
        }

        int mNormalHandle = -1;
        if (supportsNormals()) {
            mNormalHandle = setNormals(obj);
        }

        if (supportsMvMatrix()) {
            setMvMatrix(mvMatrix);
        }

        if (lightPos != null && supportsLighting()) {
            setLightPos(lightPos);
        }

        drawShape(obj, drawMode, drawSize);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        if (mColorHandle != -1) {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }

        if (mNormalHandle != -1) {
            GLES20.glDisableVertexAttribArray(mNormalHandle);
        }
    }

    public float[] getMMatrix(Object3DData obj) {

        // calculate object transformation
        Matrix.setIdentityM(mMatrix, 0);
        if (obj.getRotation() != null) {
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[0], 1f, 0f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[1], 0, 1f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotationZ(), 0, 0, 1f);
        }
        if (obj.getScale() != null) {
            Matrix.scaleM(mMatrix, 0, obj.getScaleX(), obj.getScaleY(), obj.getScaleZ());
        }
        if (obj.getPosition() != null) {
            Matrix.translateM(mMatrix, 0, obj.getPositionX(), obj.getPositionY(), obj.getPositionZ());
        }
        return mMatrix;
    }

    public float[] getMvMatrix(float[] mMatrix, float[] vMatrix) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0);
        return mvMatrix;
    }

    private float[] getMvpMatrix(float[] mvMatrix, float[] pMatrix) {
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);
        return mvpMatrix;
    }

    private void setMvpMatrix(float[] mvpMatrix) {

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");


        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

    }

    protected boolean supportsColors() {
        return false;
    }

    protected void setColor(Object3DData obj) {
        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        float[] color;
        color = obj.getColor() != null ? obj.getColor() : DEFAULT_COLOR;
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

    }

    private int setColors(Object3DData obj) {

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");


        // Pass in the color information
        GLES20.glEnableVertexAttribArray(mColorHandle);


        obj.getVertexColorsArrayBuffer().position(0);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, obj.getVertexColorsArrayBuffer());


        return mColorHandle;
    }

    private int setPosition(Object3DData obj) {

        // get handle to vertex shader's a_Position member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");


        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);


        FloatBuffer vertexBuffer = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE,
                vertexBuffer);
        return mPositionHandle;
    }

    protected boolean supportsNormals() {
        return false;
    }

    private int setNormals(Object3DData obj) {
        int mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");


        GLES20.glEnableVertexAttribArray(mNormalHandle);


        // Pass in the normal information
        FloatBuffer buffer = obj.getVertexNormalsArrayBuffer() != null ? obj.getVertexNormalsArrayBuffer() : obj.getNormals();
        buffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, buffer);

        return mNormalHandle;
    }

    protected boolean supportsLighting() {
        return false;
    }

    private void setLightPos(float[] lightPosInEyeSpace) {
        int mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);
    }

    protected boolean supportsMvMatrix() {
        return false;
    }

    private void setMvMatrix(float[] mvMatrix) {
        int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");


        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

    }

    protected boolean supportsTextures() {
        return false;
    }

    private void setTexture(Object3DData obj, int textureId) {
        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");


        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);


        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);


        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");


        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        // Prepare the triangle coordinate data
        obj.getTextureCoordsArrayBuffer().position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0,
                obj.getTextureCoordsArrayBuffer());


    }

    private void drawShape(Object3DData obj, int drawMode, int drawSize) {
        FloatBuffer vertexBuffer = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer.position(0);
        List<int[]> drawModeList = obj.getDrawModeList();
        IntBuffer drawOrderBuffer = obj.getDrawOrder();


        if (obj.isDrawUsingArrays()) {
            drawOrderBuffer = null;
        }

        if (drawModeList != null) {
            if (drawOrderBuffer == null) {
                Log.d(obj.getId(), "Drawing single polygons using arrays...");
                for (int j = 0; j < drawModeList.size(); j++) {
                    int[] polygon = drawModeList.get(j);
                    if (drawMode == GLES20.GL_LINE_LOOP && polygon[2] > 3) {
                        // is this wireframe?
                        // Log.v("Object3DImpl","Drawing wireframe for '" + obj.getId() + "' (" + drawSizePolygon + ")...");
                        for (int i = 0; i < polygon[2] - 2; i++) {
                            // Log.v("Object3DImpl","Drawing wireframe triangle '" + i + "' for '" + obj.getId() + "'...");
                            GLES20.glDrawArrays(drawMode, polygon[1] + i, 3);
                        }
                    } else {
                        GLES20.glDrawArrays(drawMode, polygon[1], polygon[2]);
                    }
                }
            } else {
                // Log.d(obj.getId(),"Drawing single polygons using elements...");
                for (int i = 0; i < drawModeList.size(); i++) {
                    int[] drawPart = drawModeList.get(i);
                    int drawModePolygon = drawPart[0];
                    int vertexPos = drawPart[1];
                    int drawSizePolygon = drawPart[2];
                    drawOrderBuffer.position(vertexPos);
                    GLES20.glDrawElements(drawModePolygon, drawSizePolygon, GLES20.GL_UNSIGNED_INT, drawOrderBuffer);
                }
            }
        } else {
            if (drawOrderBuffer != null) {
                if (drawSize <= 0) {
                    // String mode = drawMode == GLES20.GL_POINTS ? "Points" : drawMode == GLES20.GL_LINES? "Lines": "Triangles?";
                    // Log.d(obj.getId(),"Drawing all elements with mode '"+mode+"'...");
                    drawOrderBuffer.position(0);
                    GLES20.glDrawElements(drawMode, drawOrderBuffer.capacity(), GLES20.GL_UNSIGNED_INT,
                            drawOrderBuffer);
                } else {
                    //Log.d(obj.getId(),"Drawing single elements of size '"+drawSize+"'...");
                    for (int i = 0; i < drawOrderBuffer.capacity(); i += drawSize) {
                        drawOrderBuffer.position(i);
                        GLES20.glDrawElements(drawMode, drawSize, GLES20.GL_UNSIGNED_INT, drawOrderBuffer);
                    }
                }
            } else {
                if (drawSize <= 0) {
                    int drawCount = vertexBuffer.capacity() / COORDS_PER_VERTEX;

                    // if we want to animate, initialize counter=0 at variable declaration
                    if (this.shift >= 0) {
                        double rotation = ((SystemClock.uptimeMillis() % 10000) / 10000f) * (Math.PI * 2);

                        if (this.shift == 0d) {
                            this.shift = rotation;
                        }
                        drawCount = (int) ((Math.sin(rotation - this.shift + Math.PI / 2 * 3) + 1) / 2f * drawCount);
                    }
                    // Log.d(obj.getId(),"Drawing all triangles using arrays... counter("+drawCount+")");
                    GLES20.glDrawArrays(drawMode, 0, drawCount);
                } else {
                    //Log.d(obj.getId(),"Drawing single triangles using arrays...");
                    for (int i = 0; i < vertexBuffer.capacity() / COORDS_PER_VERTEX; i += drawSize) {
                        GLES20.glDrawArrays(drawMode, i, drawSize);
                    }
                }
            }
        }
    }

}