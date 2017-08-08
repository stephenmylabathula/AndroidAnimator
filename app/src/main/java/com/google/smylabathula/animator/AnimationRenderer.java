package com.google.smylabathula.animator;


import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mylo on 7/25/17.
 */

public class AnimationRenderer implements GLSurfaceView.Renderer {

    private Skeleton skeleton;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public volatile float zoom = 1.0f;  //0.5 -> 4.5
    public volatile float xPos = 0.0f;
    public volatile float yPos = 0.0f;
    public volatile float zPos = 0.0f;

    private Activity mainActivity;
    private ActionBar actionBar;

    public static String currAction = "";

    public AnimationRenderer(Activity activity, android.support.v7.app.ActionBar mTextView) {
        super();
        mainActivity = activity;
        actionBar = mTextView;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        skeleton = new Skeleton(mainActivity);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, zoom+xPos, zoom+yPos, zoom+zPos, 0f, 0f, 0f, -1.0f, -1.0f, 2.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        // Write Action
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionBar.setTitle(currAction);
            }
        });
        // Draw the points
        skeleton.draw(mMVPMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // projection matrix is applied to object coordinates in onDrawFrame()
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 7.0f);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
