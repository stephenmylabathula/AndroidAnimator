package com.google.smylabathula.animator;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;

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
    ArrayList<Byte> incomingData;
    float[][] skeleton;
    int prevType, prevPhase = 0;

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
                        GeneratePose(action_phase_json.getInt(0), action_phase_json.getInt(1), action_phase_json.getInt(2), action_phase_json.getInt(3));
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


    @Override
    public void run() {
        //if (Thread.currentThread() == listener && inputStream != null) {
          //  listenAndUpdatePoints();
        //}



        if(Thread.currentThread() == listener && inputStream != null) {
            while(true) {
                try {
                    while (inputStream.available() > 0) {
                        byte[] b = new byte[1024];
                        inputStream.read(b);
                        String maybeJson = new String(b);
                        for (int i = 0; i < 1024; i++)
                            incomingData.add(b[i]);
                    }
                    if(incomingData.size() > 0) {
                        byte[] newBytes = new byte[incomingData.size()];
                        for (int i = 0; i < newBytes.length; i++)
                            newBytes[i] = incomingData.get(i);
                        String maybeJson = new String(newBytes);
                        try {
                            final JSONArray jsonArray = new JSONArray(maybeJson);
                            //System.out.println(jsonArray.getInt(0));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                for (int j = 0; j < 3; j++) {
                                    skeleton[i][j] = (float) jsonArray.getJSONArray(i).getDouble(j);
                                }
                            }
                            resetPoints();

                            //GeneratePose(jsonArray.getInt(0), jsonArray.getInt(1), jsonArray.getInt(2), jsonArray.getInt(3));
                            incomingData.clear();
                        } catch (JSONException e) {
                            System.out.println("BAD JSON");
                        }
                    }
                } catch (Exception e) {

                }
                try {
                    Thread.sleep(10);
                } catch (Exception e){

                }
            }
        }
    }

    private void GeneratePose(int type, int phase, int start_index, int end_index) {

        if (prevType == type && prevPhase == phase)
            return;

        prevType = type;
        prevPhase = phase;
        MotionModel current_model = PoseEstimator.motion_models.get(type);

        int model_start_index = current_model.motion_phases.indices.get(phase - 1);
        int model_end_index = current_model.motion_phases.indices.get(phase);
        //int motion_start_index = start_index;
        //int motion_end_index = end_index;

        //float model_to_motion_time_ratio = (motion_end_index - motion_start_index) / (model_end_index - model_start_index);

        for (int i = model_start_index; i <= model_end_index; i++) {
            Vector<Vector<Double>> currXYZ = current_model.GetXYZPoints(current_model.channels.get(i));
            for (int j = 0; j < 31; j++){
                for (int k = 0; k < 3; k++)
                    skeleton[j][k] = currXYZ.get(j).get(k).floatValue();
            }
            resetPoints();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Skeleton(Context context){
        // load motion capture motion models
        PoseEstimator.LoadMotionModels(context);

        // initialize members
        skeleton = new float[31][3];
        incomingData = new ArrayList<>();

        try {
            socket = new Socket("192.168.43.1", 12345);
            inputStream = socket.getInputStream();
            listener = new Thread(this);
            //renderer = new Thread(this);
            listener.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup Skeleton for OpenGL
        resetPoints();
        GLES20.glEnableVertexAttribArray(0);

        // create and compile vertex shader
        int vertexShader = AnimationRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderSource);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);

        // create and compile fragment shader
        int fragmentShader = AnimationRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderSource);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);

        // create, link, and use the create shaders as _program
        _program = GLES20.glCreateProgram();
        GLES20.glAttachShader(_program, vertexShader);
        GLES20.glAttachShader(_program, fragmentShader);
        GLES20.glBindAttribLocation(_program, 0, "position");
        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        GLES20.glUseProgram(_program);
    }

    private float[][] getLines(){
        float[][] skeletonWithLines = new float[60][3];

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

        return skeletonWithLines;
    }

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

    public void draw(float[] mvpMatrix) {
        resetPoints();

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(_program, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // arg2: 0 is the program ID
        // arg3: 60 is the number of points to draw
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 60);
    }

}