package com.google.smylabathula.animator;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;
import android.widget.TextView;

import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ThreadFactory;

/**
 * Created by mylo on 7/26/17.
 */

public class Skeleton implements Runnable {

    /* Skeleton Rendering Globals */
    Thread listener, renderer;
    Socket socket;
    InputStream inputStream;
    float[][] skeleton;
    int prevType, prevPhase = 0;
    int action_type, action_phase, action_start, action_end = 0;
    String[] actions = {"JUMP", "LEFT START WALK", "LEFT STEP WALK", "LEFT STOP WALK", "RIGHT STEP WALK", "RIGHT STOP WALK", "SIT", "SIT POSE", "STAND", "STAND POSE", "WALK", "MOVE"};

    /* OpenGL Globals */
    private int _program;
    private int mMVPMatrixHandle;
    private final String vertexShaderSource = "" +
            "" +
            "attribute vec4 position;" +
            "uniform mat4 uMVPMatrix;" +
            "" +
            "void main()" +
            "{" +
            "    gl_Position = uMVPMatrix * position;" +
            "    gl_PointSize = 15.0;" +
            "}";
    private final String fragmentShaderSource = "" +
            "" +
            "" +
            "" +
            "void main()" +
            "{" +
            "    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
            "}";


    public Skeleton(Context context){
        // Load the motion capture motion models
        PoseEstimator.LoadMotionModels(context);

        // Initialize members
        skeleton = new float[31][3];
        listener = new Thread(this);
        renderer = new Thread(this);

        // Setup Sockets and Threads
        try {
            socket = new Socket("192.168.43.1", 12345);
            inputStream = socket.getInputStream();
            listener.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup Skeleton for OpenGL
        resetPoints();
        GLES20.glEnableVertexAttribArray(0);

        // Create and compile vertex shader
        int vertexShader = AnimationRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderSource);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);

        // Create and compile fragment shader
        int fragmentShader = AnimationRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderSource);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);

        // Create, link, and use the create shaders as _program
        _program = GLES20.glCreateProgram();
        GLES20.glAttachShader(_program, vertexShader);
        GLES20.glAttachShader(_program, fragmentShader);
        GLES20.glBindAttribLocation(_program, 0, "position");
        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        GLES20.glUseProgram(_program);
    }

    @Override
    public void run() {
        if (Thread.currentThread() == listener && inputStream != null) {
            listenAndUpdatePoints();
        }
        if (Thread.currentThread() == renderer) {
            GeneratePose();
        }
    }

    @WorkerThread
    private void listenAndUpdatePoints(){
        while (true) {
            try {
                while (inputStream.available() > 0) {
                    byte[] inputBytes = new byte[1024];
                    inputStream.read(inputBytes);
                    String jsonData = new String(inputBytes);
                    try {
                        JSONArray action_phase_json = new JSONArray(jsonData);
                        action_type = action_phase_json.getInt(0);
                        action_phase = action_phase_json.getInt(1);
                        action_start = action_phase_json.getInt(2);
                        action_end = action_phase_json.getInt(3);
                        renderer.start();
                    } catch (JSONException e) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates Animation for Action Phase Tag
     */
    private void GeneratePose() {
        int type = action_type;
        int phase = action_phase;
        int start_index = action_start;
        int end_index = action_end;
        AnimationRenderer.currAction = actions[type] + " - PHASE: " + phase;

        // Check that we have a new action and phase
        if (prevType == type && prevPhase == phase)
            return;

        // Record this new action as the previous action for next time
        prevType = type;
        prevPhase = phase;

        // Get motion model for current action
        MotionModel current_model = PoseEstimator.motion_models.get(type);

        int model_start_index = current_model.motion_phases.indices.get(Math.max(phase - 1, 0)); //should be phase - 1 when fixed
        int model_end_index = current_model.motion_phases.indices.get(Math.min(phase, current_model.motion_phases.indices.size() - 1));
        int motion_start_index = start_index;
        int motion_end_index = end_index;

        int model_to_motion_time_ratio;

        if (motion_end_index - motion_start_index <= 0)
            model_to_motion_time_ratio = 10;
        else
            model_to_motion_time_ratio = Math.max((Math.round((motion_end_index - motion_start_index) / (model_end_index - model_start_index)) * 5), 1);

        for (int i = model_start_index; i <= model_end_index; i++) {
            Vector<Vector<Double>> currXYZ = current_model.GetXYZPoints(current_model.channels.get(i));
            for (int j = 0; j < 31; j++){
                skeleton[j][0] = currXYZ.get(j).get(0).floatValue();
                skeleton[j][2] = currXYZ.get(j).get(1).floatValue();
                skeleton[j][1] = currXYZ.get(j).get(2).floatValue();
            }
            resetPoints();
            try {
                Thread.sleep((long) model_to_motion_time_ratio);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates Points to Render Lines for Skeleton
     * @return
     */
    private float[][] getLines(){
        float[][] skeletonWithLines = new float[68][3];

        skeletonWithLines[0] = skeleton[0];
        skeletonWithLines[1] = skeleton[1];
        skeletonWithLines[2] = skeleton[0];
        skeletonWithLines[3] = skeleton[6];
        skeletonWithLines[4] = skeleton[0];
        skeletonWithLines[5] = skeleton[11];
        skeletonWithLines[6] = skeleton[1];
        skeletonWithLines[7] = skeleton[2];
        skeletonWithLines[8] = skeleton[2];
        skeletonWithLines[9] = skeleton[3];
        skeletonWithLines[10] = skeleton[3];
        skeletonWithLines[11] = skeleton[4];
        skeletonWithLines[12] = skeleton[4];
        skeletonWithLines[13] = skeleton[5];
        skeletonWithLines[14] = skeleton[6];
        skeletonWithLines[15] = skeleton[7];
        skeletonWithLines[16] = skeleton[7];
        skeletonWithLines[17] = skeleton[8];
        skeletonWithLines[18] = skeleton[8];
        skeletonWithLines[19] = skeleton[9];
        skeletonWithLines[20] = skeleton[9];
        skeletonWithLines[21] = skeleton[10];
        skeletonWithLines[22] = skeleton[11];
        skeletonWithLines[23] = skeleton[12];
        skeletonWithLines[24] = skeleton[12];
        skeletonWithLines[25] = skeleton[13];
        skeletonWithLines[26] = skeleton[13];
        skeletonWithLines[27] = skeleton[14];
        skeletonWithLines[28] = skeleton[13];
        skeletonWithLines[29] = skeleton[17];
        skeletonWithLines[30] = skeleton[13];
        skeletonWithLines[31] = skeleton[24];
        skeletonWithLines[32] = skeleton[14];
        skeletonWithLines[33] = skeleton[15];
        skeletonWithLines[34] = skeleton[15];
        skeletonWithLines[35] = skeleton[16];
        skeletonWithLines[36] = skeleton[17];
        skeletonWithLines[37] = skeleton[18];
        skeletonWithLines[38] = skeleton[18];
        skeletonWithLines[39] = skeleton[19];
        skeletonWithLines[40] = skeleton[19];
        skeletonWithLines[41] = skeleton[20];
        skeletonWithLines[42] = skeleton[20];
        skeletonWithLines[43] = skeleton[21];
        skeletonWithLines[44] = skeleton[20];
        skeletonWithLines[45] = skeleton[23];
        skeletonWithLines[46] = skeleton[21];
        skeletonWithLines[47] = skeleton[22];
        skeletonWithLines[48] = skeleton[24];
        skeletonWithLines[49] = skeleton[25];
        skeletonWithLines[50] = skeleton[25];
        skeletonWithLines[51] = skeleton[26];
        skeletonWithLines[52] = skeleton[26];
        skeletonWithLines[53] = skeleton[27];
        skeletonWithLines[54] = skeleton[27];
        skeletonWithLines[55] = skeleton[28];
        skeletonWithLines[56] = skeleton[27];
        skeletonWithLines[57] = skeleton[30];
        skeletonWithLines[58] = skeleton[28];
        skeletonWithLines[59] = skeleton[29];

        // Define Floor
        skeletonWithLines[60] = new float[]{1.0f, 1.0f, 0.0f};
        skeletonWithLines[61] = new float[]{1.0f, -1.0f, 0.0f};
        skeletonWithLines[62] = new float[]{1.0f, 1.0f, 0.0f};
        skeletonWithLines[63] = new float[]{-1.0f, 1.0f, 0.0f};
        skeletonWithLines[64] = new float[]{-1.0f, 1.0f, 0.0f};
        skeletonWithLines[65] = new float[]{-1.0f, -1.0f, 0.0f};
        skeletonWithLines[66] = new float[]{-1.0f, -1.0f, 0.0f};
        skeletonWithLines[67] = new float[]{1.0f, -1.0f, 0.0f};

        return skeletonWithLines;
    }

    /**
     * Converts Skeleton Points to OpenGL Point Array
     */
    private void resetPoints() {
        // add lines to skeleton points and place in 1D array
        float[][] linedSkeleton = getLines();
        float[] geometry = new float[linedSkeleton.length * 4];
        int index = 0;
        for(int i = 0; i < linedSkeleton.length; i++){
            geometry[index++] = linedSkeleton[i][0];
            geometry[index++] = linedSkeleton[i][1];
            geometry[index++] = linedSkeleton[i][2];
            geometry[index++] = 1;
        }

        // convert float array to float buffer
        ByteBuffer geometryByteBuffer = ByteBuffer.allocateDirect(geometry.length * 4);
        geometryByteBuffer.order(ByteOrder.nativeOrder());  // use machine endianness
        FloatBuffer geometryBuffer = geometryByteBuffer.asFloatBuffer();
        geometryBuffer.put(geometry);
        geometryBuffer.rewind();    //rewind buffer so OpenGL can use

        // setup OpenGL params
        // arg1: 0 is the program ID
        // arg2: 4 is the data size (float)
        // arg5: 4*4=16 is the row stride size
        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 4 * 4, geometryBuffer);
    }

    /**
     * Redraws OpenGL Surface
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        resetPoints();

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(_program, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // arg2: 0 is the program ID
        // arg3: 60 is the number of points to draw
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 68);
    }

}