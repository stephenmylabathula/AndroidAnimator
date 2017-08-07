package com.google.smylabathula.animator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.MotionEvent.*;
import android.view.ScaleGestureDetector;

import java.io.InputStream;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * Created by mylo on 7/25/17.
 */

public class AnimationSurfaceView extends GLSurfaceView {

    /* OpenGL Animation Globals */
    private final AnimationRenderer animationRenderer;  // Renderer

    /* Touch Event Globals */
    private float mLastTouchX;  // records previous touch point-x
    private float mLastTouchY;  // records previous touch point-y
    private int mActivePointerId = INVALID_POINTER_ID;
    // Pinch Gesture Variables
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    /* Device Orientation Globals */
    private Sensor mAccelSensor;    // accelerometer sensor handle
    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];

    //private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    //private float mPreviousX;
    //private float mPreviousY;
    //private Sensor mMagSensor;
    //private final float[] mRotationMatrix = new float[9];
    //private final float[] mOrientationAngles = new float[3];

    public AnimationSurfaceView(Context context){
        super(context);
        animationRenderer = new AnimationRenderer(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setupAnimationRenderer();
    }

    private void setupAnimationRenderer(){
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(animationRenderer); // Set the Renderer for drawing on the GLSurfaceView
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Calculate the distance moved
                final float dx = (x - mLastTouchX) / 10;
                final float dy = (y - mLastTouchY) / 10;

               // animationRenderer.xPos += dx;
                animationRenderer.zPos += dy;
                System.out.println(animationRenderer.zPos);
                requestRender();

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 5.0f));

            animationRenderer.zoom = -1 * mScaleFactor + 5.5f;
            requestRender();
            return true;
        }
    }


}
