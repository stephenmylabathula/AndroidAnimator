package com.google.smylabathula.animator;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by mylo on 7/25/17.
 */

public class Triangle {

    private final int _program;
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

    private final float[] geometry = {
            ///-0.5f, -0.5f, 0.0f, 1.0f,
            ///0.5f, -0.5f, 0.0f, 1.0f,
            ///0.0f, 0.5f, 0.0f, 1.0f
            0.5f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.5f, 1.0f
    };


    public Triangle() {
        // convert float array to float buffer
        ByteBuffer geometryByteBuffer = ByteBuffer.allocateDirect(geometry.length * 4);
        geometryByteBuffer.order(ByteOrder.nativeOrder());  // use machine endianness
        FloatBuffer geometryBuffer = geometryByteBuffer.asFloatBuffer();
        geometryBuffer.put(geometry);
        geometryBuffer.rewind();    //rewind buffer so OpenGL can use

        // setup OpenGL params
        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 4 * 4, geometryBuffer);
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

    public void draw(float[] mvpMatrix) {
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(_program, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

}
