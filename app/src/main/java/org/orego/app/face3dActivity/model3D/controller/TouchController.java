package org.orego.app.face3dActivity.model3D.controller;

import org.orego.app.face3dActivity.model3D.view.ModelRenderer;
import org.orego.app.face3dActivity.model3D.view.ModelSurfaceView;

import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Handle touching:
 */

public final class TouchController {

    private static final String TAG = TouchController.class.getName();

    private final ModelSurfaceView view;

    private final ModelRenderer mRenderer;

    private float x1 = Float.MIN_VALUE;
    private float y1 = Float.MIN_VALUE;
    private float x2 = Float.MIN_VALUE;
    private float y2 = Float.MIN_VALUE;
    private float dx1 = Float.MIN_VALUE;
    private float dy1 = Float.MIN_VALUE;

    private float length = Float.MIN_VALUE;
    private float previousLength = Float.MIN_VALUE;
    private float currentPress1 = Float.MIN_VALUE;

    private boolean fingersAreClosing = false;
    private boolean isRotating = false;

    private boolean gestureChanged = false;
    private int touchDelay = -2;

    private float previousX1;
    private float previousY1;
    private float previousX2;
    private float previousY2;
    private float[] previousVector = new float[4];
    private float[] vector = new float[4];
    private float[] rotationVector = new float[4];

    public TouchController(final ModelSurfaceView view, final ModelRenderer renderer) {
        super();
        this.view = view;
        this.mRenderer = renderer;
    }

    /**
     * Handle touching
     *
     * @param motionEvent - touching event
     * @return always return true
     */

    public final synchronized boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                this.touchDelay++;
                break;
            default:
                Log.w(TAG, "Unknown state: " + motionEvent.getAction());
                this.gestureChanged = true;
        }

        final int pointerCount = motionEvent.getPointerCount();

        if (pointerCount == 1) {
            this.x1 = motionEvent.getX();
            this.y1 = motionEvent.getY();
            if (gestureChanged) {
                Log.d(TAG, "x:" + x1 + ",y:" + y1);
                previousX1 = x1;
                previousY1 = y1;
            }
            this.dx1 = x1 - previousX1;
            this.dy1 = y1 - previousY1;
        } else if (pointerCount == 2) {
            this.x1 = motionEvent.getX(0);
            this.y1 = motionEvent.getY(0);
            this.x2 = motionEvent.getX(1);
            this.y2 = motionEvent.getY(1);
            this.vector[0] = x2 - x1;
            this.vector[1] = y2 - y1;
            this.vector[2] = 0;
            this.vector[3] = 1;
            float len = Matrix.length(vector[0], vector[1], vector[2]);
            this.vector[0] /= len;
            this.vector[1] /= len;
            if (gestureChanged) {
                this.previousX1 = x1;
                this.previousY1 = y1;
                this.previousX2 = x2;
                this.previousY2 = y2;
                System.arraycopy(vector, 0, previousVector, 0, vector.length);
            }
            this.dx1 = x1 - previousX1;
            this.dy1 = y1 - previousY1;
            final float dx2 = x2 - previousX2;
            final float dy2 = y2 - previousY2;

            this.rotationVector[0] = (previousVector[1] * vector[2]) - (previousVector[2] * vector[1]);
            this.rotationVector[1] = (previousVector[2] * vector[0]) - (previousVector[0] * vector[2]);
            this.rotationVector[2] = (previousVector[0] * vector[1]) - (previousVector[1] * vector[0]);
            len = Matrix.length(rotationVector[0], rotationVector[1], rotationVector[2]);
            this.rotationVector[0] /= len;
            this.rotationVector[1] /= len;
            this.rotationVector[2] /= len;

            this.previousLength = (float) Math
                    .sqrt(Math.pow(previousX2 - previousX1, 2) + Math.pow(previousY2 - previousY1, 2));
            this.length = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

            this.currentPress1 = motionEvent.getPressure(0);

            // gesture detection:
            final boolean isOneFixedAndOneMoving = ((dx1 + dy1) == 0) != (((dx2 + dy2) == 0));
            this.fingersAreClosing = !isOneFixedAndOneMoving && (Math.abs(dx1 + dx2) < 10 && Math.abs(dy1 + dy2) < 10);
            this.isRotating = !isOneFixedAndOneMoving && (dx1 != 0 && dy1 != 0 && dx2 != 0 && dy2 != 0)
                    && rotationVector[2] != 0;
        }

        final int max = Math.max(mRenderer.getWidth(), mRenderer.getHeight());
        if (touchDelay > 1) {
            if (pointerCount != 1 || currentPress1 <= 4.0f) {
                if (pointerCount == 1) {
                    this.dx1 = (float) (dx1 / max * Math.PI * 2);
                    this.dy1 = (float) (dy1 / max * Math.PI * 2);
                    this.mRenderer.getCamera().translateCamera(dx1, dy1);
                } else if (pointerCount == 2) {
                    if (fingersAreClosing) {
                        float zoomFactor = (length - previousLength) / max * mRenderer.getFar();
                        Log.i(TAG, "Zooming '" + zoomFactor + "'...");
                        mRenderer.getCamera().MoveCameraZ(zoomFactor);
                    }
                    if (isRotating) {
                        Log.i(TAG, "Rotating camera '" + Math.signum(rotationVector[2]) + "'...");
                        mRenderer.getCamera().Rotate((float) (Math.signum(rotationVector[2]) / Math.PI) / 4);
                    }
                }
            }
        }
        this.previousX1 = x1;
        this.previousY1 = y1;
        this.previousX2 = x2;
        this.previousY2 = y2;
        System.arraycopy(vector, 0, previousVector, 0, vector.length);
        if (gestureChanged && touchDelay > 1) {
            this.gestureChanged = false;
            Log.v(TAG, "Fin");
        }
        this.view.requestRender();
        return true;
    }
}