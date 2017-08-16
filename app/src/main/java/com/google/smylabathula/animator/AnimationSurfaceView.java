package com.google.smylabathula.animator;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.*;
import android.view.MotionEvent;
import android.support.v4.view.MotionEventCompat;
import android.widget.Toast;

/**
 * Created by mylo on 7/25/17.
 */

public class AnimationSurfaceView extends GLSurfaceView {

    /* OpenGL Animation Globals */
    private final AnimationRenderer animation_renderer;  // Renderer

    private Scene animation_scene;
    private Context main_activity;
    private ScaleGestureDetector mScaleDetector;


    public AnimationSurfaceView(Activity mainActivity, Scene scene){
        super(mainActivity);
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Create the animation renderer.
        animation_renderer = new AnimationRenderer(scene);
        // Initialize member scene variable
        animation_scene = scene;
        main_activity = mainActivity;
        // Initialize Gesture Detector
        InitializeGestureDetector();
        mScaleDetector = new ScaleGestureDetector(mainActivity, new ScaleListener());
        // Set the Renderer for drawing on the GLSurfaceView.
        setRenderer(animation_renderer);
    }

    private void OnDoubleTap() {
        Toast.makeText(main_activity, animation_scene.ChangeCameraView().replace('_', ' '),
                Toast.LENGTH_SHORT).show();
    }

    private void InitializeGestureDetector() {
        this.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    OnDoubleTap();
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mScaleFactor = 1.f;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            animation_scene.ChangeCameraView(mScaleFactor);
            requestRender();
            return true;
        }
    }

}
