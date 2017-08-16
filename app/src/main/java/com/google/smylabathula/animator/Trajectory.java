package com.google.smylabathula.animator;

import android.opengl.GLES20;

import java.util.Vector;

import static java.util.Collections.binarySearch;

/**
 * Created by amedhat on 8/9/17.
 */

public class Trajectory {
    Vector<double[]> position_xyz;
    Vector<double[]> orientation_rpy;
    Vector<Long> imu_index;
    int current_pose_index;

    int vertex_num;
    int vertex_buffer_index;

    public Trajectory() {
        current_pose_index = 0;
        vertex_num = 0;
        vertex_buffer_index = 0;
        position_xyz = new Vector<>();
        orientation_rpy = new Vector<>();
        imu_index =  new Vector<>();
    }
    public void SetBufferIndex(int buffer_index) {
        vertex_buffer_index = buffer_index;
    }
    public void AddPose(long index, double[] position, double[] orientation){
        imu_index.add(index);
        position_xyz.add(position);
        orientation_rpy.add(orientation);
    }
    public int getPose(long pose_imu_index, double[] position, double[] orientation) {
        if (position_xyz.isEmpty())
            return -1;
        int index = binarySearch(imu_index,pose_imu_index);
        if(index<0){
            // IMU index not found.
            if(index == -1) {
                // Before first element.
                position[0] = position_xyz.get(0)[0];
                position[1] = position_xyz.get(0)[1];
                position[2] = position_xyz.get(0)[2];
                orientation[0] = orientation_rpy.get(0)[0];
                orientation[1] = orientation_rpy.get(0)[1];
                orientation[2] = orientation_rpy.get(0)[2];
            } else if(index == -imu_index.size()-1){
                // After last element.
                position[0] = position_xyz.get(imu_index.size()-1)[0];
                position[1] = position_xyz.get(imu_index.size()-1)[1];
                position[2] = position_xyz.get(imu_index.size()-1)[2];
                orientation[0] = orientation_rpy.get(imu_index.size()-1)[0];
                orientation[1] = orientation_rpy.get(imu_index.size()-1)[1];
                orientation[2] = orientation_rpy.get(imu_index.size()-1)[2];
            } else {
                int lower_index =  -index - 2;
                int upper_index =  -index - 1;
                long imu_index1 = imu_index.get(lower_index);
                long imu_index2 = imu_index.get(upper_index);
                double alpha = ((double)(imu_index2-pose_imu_index))/((double)(imu_index2-imu_index1));
                double[] position1 = position_xyz.get(lower_index);
                double[] position2 = position_xyz.get(upper_index);
                double[] orientation1 = orientation_rpy.get(lower_index);
                double[] orientation2 = orientation_rpy.get(upper_index);
                position[0] = alpha * position1[0] + (1-alpha) * position2[0];
                position[1] = alpha * position1[1] + (1-alpha) * position2[1];
                position[2] = alpha * position1[2] + (1-alpha) * position2[2];
                orientation[0] = alpha * orientation1[0] + (1-alpha) * orientation2[0];
                orientation[1] = alpha * orientation1[1] + (1-alpha) * orientation2[1];
                orientation[2] = alpha * orientation1[2] + (1-alpha) * orientation2[2];
            }
        } else{
            // IMU index found.
            position[0] = position_xyz.get(index)[0];
            position[1] = position_xyz.get(index)[1];
            position[2] = position_xyz.get(index)[2];
            orientation[0] = orientation_rpy.get(index)[0];
            orientation[1] = orientation_rpy.get(index)[1];
            orientation[2] = orientation_rpy.get(index)[2];
        }
        return index;
    }
    public void UpdateVertexBuffer(float[] vertices){
        int index = vertex_buffer_index * 4;
        for(int i=0; i<vertex_num; i++) {
            vertices[index] = 0.0f; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 0.0f; index++;
            vertices[index] = 1.0f; index++;
        }
    }
    public void Draw(int gl_program_id){
        // Draw the floor.
        int mColorHandle = GLES20.glGetUniformLocation(gl_program_id, "vColor");
        int start_index = vertex_buffer_index;
        // Set color for drawing the triangle
        float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, start_index, vertex_num);
    }
    public void Update(double[] position){

    }
}
