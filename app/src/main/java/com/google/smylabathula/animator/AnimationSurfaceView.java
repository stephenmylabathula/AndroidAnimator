package com.google.smylabathula.animator;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.io.InputStream;

/**
 * Created by mylo on 7/25/17.
 */

public class AnimationSurfaceView extends GLSurfaceView {

    private final AnimationRenderer animationRenderer;

    public static  InputStream isX;
    public static  InputStream isY;
    public static  InputStream isZ;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;


    public AnimationSurfaceView(Context context){
        super(context);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        isX = getResources().openRawResource(R.raw.joints_x);
        isY = getResources().openRawResource(R.raw.joints_y);
        isZ = getResources().openRawResource(R.raw.joints_z);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // Set the Renderer for drawing on the GLSurfaceView
        animationRenderer = new AnimationRenderer();
        setRenderer(animationRenderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);
        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 5.0f));

            animationRenderer.zoom = -1 * mScaleFactor + 5.5f;
            requestRender();
            System.out.println(animationRenderer.zoom);
            return true;
        }
    }


}
