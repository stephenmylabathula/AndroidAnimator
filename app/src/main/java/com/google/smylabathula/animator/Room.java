package com.google.smylabathula.animator;

import android.opengl.GLES20;

/**
 * Created by amedhat on 8/9/17.
 */

public class Room {
    int vertex_num;
    int vertex_buffer_index;
    float min_x;
    float min_y;
    float min_z;
    float max_x;
    float max_y;
    float max_z;
    float grid_size;
    int x_grid_num;
    int y_grid_num;

    public Room(){
        vertex_buffer_index = 0;
        grid_size = 0.5f;
        min_x = -1;
        min_y = -1;
        min_z = 0;
        max_x = 1;
        max_y = 1;
        max_z = 0;
        x_grid_num = (int) ((max_x - min_x)/grid_size + 1);
        y_grid_num = (int) ((max_y - min_y)/grid_size + 1);
        vertex_num = 10 + 2 * x_grid_num + 2 * y_grid_num;
    }
    public void UpdateRoomSize(double[] position) {
        if((position[0]-1) < min_x) {
            min_x = Math.round(position[0] - 1);
        }
        if((position[0]+1) > max_x) {
            max_x = Math.round(position[0] + 1);
        }
        if((position[1]-1) < min_y) {
            min_y = Math.round(position[1] - 1);
        }
        if((position[1]+1) > max_y) {
            max_y = Math.round(position[1] + 1);
        }
        x_grid_num = (int) ((max_x - min_x)/grid_size + 1);
        y_grid_num = (int) ((max_y - min_y)/grid_size + 1);
        vertex_num = 10 + 2 * x_grid_num + 2 * y_grid_num;
    }
    public void SetBufferIndex(int buffer_index) {
        vertex_buffer_index = buffer_index;
    }
    public void UpdateVertexBuffer(float[] vertices){
        int index = vertex_buffer_index * 4;
        // Define Floor
        vertices[index] = max_x; index++;
        vertices[index] = max_y; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = max_x; index++;
        vertices[index] = min_y; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = min_x; index++;
        vertices[index] = max_y; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = min_x; index++;
        vertices[index] = min_y; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;

        for(int i=0; i<x_grid_num; i++) {
            float x = min_x + ((float) i) * grid_size;
            vertices[index] = x; index++;
            vertices[index] = max_y; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 1.0f; index++;
            vertices[index] = x; index++;
            vertices[index] = min_y; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 1.0f; index++;
        }
        for(int i=0; i<y_grid_num; i++) {
            float y = min_y + ((float) i) * grid_size;
            vertices[index] = max_x; index++;
            vertices[index] = y; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 1.0f; index++;
            vertices[index] = min_x; index++;
            vertices[index] = y; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 1.0f; index++;
        }

        // Define X axis
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        // Define Y axis
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        // Define Z axis
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 0.0f; index++;
        vertices[index] = 1.0f; index++;
        vertices[index] = 1.0f; index++;

    }

    public void Draw(int gl_program_id){
        // Draw the floor.
        int mColorHandle = GLES20.glGetUniformLocation(gl_program_id, "vColor");
        int start_index = vertex_buffer_index;
        // Set color for drawing the triangle
        float color[] = { 0.9f, 0.9f, 0.9f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, start_index, 4);

        GLES20.glLineWidth(2f);
        start_index += 4;
        // Draw X-Lines grid
        color = new float[]{ 1.0f, 1.0f, 1.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, start_index, 2*x_grid_num);
        // Draw Y-Lines grid
        start_index += (2*x_grid_num);
        color = new float[]{ 1.0f, 1.0f, 1.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, start_index, 2*y_grid_num);
        start_index += (2*y_grid_num);
        // Draw Global X-Y-Z axis.
        GLES20.glLineWidth(10f);
        color = new float[]{ 1.0f, 0.0f, 0.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, start_index, 2);
        start_index += 2;
        color = new float[]{ 0.0f, 1.0f, 0.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, start_index, 2);
        start_index += 2;
        color = new float[]{ 0.0f, 0.0f, 1.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, start_index, 2);
        start_index += 2;
    }
}
