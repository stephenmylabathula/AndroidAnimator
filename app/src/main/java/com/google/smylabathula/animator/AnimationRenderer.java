package com.google.smylabathula.animator;


import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mylo on 7/25/17.
 */

public class AnimationRenderer implements GLSurfaceView.Renderer {
    private Scene scene;
    // OpenGL parameters
    private int gl_program_id;
    private final String vertexShaderSource = "" +
            "" +
            "attribute vec4 position;" +
            "uniform mat4 uMVPMatrix;" +
            "" +
            "void main()" +
            "{" +
            "    gl_Position = uMVPMatrix * position;" +
            "    gl_PointSize = 20.0;" +
            "}";
    private final String fragmentShaderSource = "" +
            "" +
            "uniform vec4 vColor;" +
            "" +
            "void main()" +
            "{" +
            "    gl_FragColor = vColor;" +
            "}";


    public AnimationRenderer(Scene _scene) {
        super();
        scene = _scene;
    }
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLInitialize();
    }
    private void GLInitialize(){
        // Create and compile vertex shader
        int vertexShader = LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);
        // Create and compile fragment shader
        int fragmentShader = LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);
        // Create, link, and use the create shaders as _program
        gl_program_id = GLES20.glCreateProgram();
        // get handle to fragment shader's vColor member
        GLES20.glAttachShader(gl_program_id, vertexShader);
        GLES20.glAttachShader(gl_program_id, fragmentShader);
        GLES20.glBindAttribLocation(gl_program_id, 0, "position");
        GLES20.glLinkProgram(gl_program_id);
        String programLinkLog = GLES20.glGetProgramInfoLog(gl_program_id);
        GLES20.glUseProgram(gl_program_id);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glLineWidth(20f);
    }

    public int LoadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void SetVertexBufferToDraw(float[] vertex_buffer) {
        // convert float array to float buffer
        ByteBuffer byte_vertex_buffer = ByteBuffer.allocateDirect(vertex_buffer.length * 4);
        byte_vertex_buffer.order(ByteOrder.nativeOrder());  // use machine endianness
        FloatBuffer float_vertex_buffer = byte_vertex_buffer.asFloatBuffer();
        float_vertex_buffer.put(vertex_buffer);
        float_vertex_buffer.rewind();    //rewind buffer so OpenGL can use
        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 4 * 4, float_vertex_buffer);
    }
    public void onDrawFrame(GL10 unused) {
        // Draw the points
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        SetVertexBufferToDraw(scene.vertex_buffer);
        scene.Draw(gl_program_id);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
