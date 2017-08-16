package com.google.smylabathula.animator;

import android.opengl.GLES20;
import java.util.*;

public class Skeleton {
    float[][] joints_xyz;
    float[][] connected_joints_xyz;
    int vertex_num;
    int vertex_buffer_index;

    public Skeleton(){
        vertex_num = 60;
        vertex_buffer_index = 0;
        joints_xyz = new float[31][3];
        connected_joints_xyz = new float[60][3];
    }
    public void UpdatePose(Vector<Vector<Double>> _joints_xyz) {
        for (int j = 0; j < 31; j++){
            joints_xyz[j][0] = _joints_xyz.get(j).get(0).floatValue();
            joints_xyz[j][1] = _joints_xyz.get(j).get(1).floatValue();
            joints_xyz[j][2] = _joints_xyz.get(j).get(2).floatValue();
        }
        UpdateConnectedJointsXYZ();
    }

    private void UpdateConnectedJointsXYZ(){
        connected_joints_xyz[0] = joints_xyz[0];
        connected_joints_xyz[1] = joints_xyz[1];
        connected_joints_xyz[2] = joints_xyz[0];
        connected_joints_xyz[3] = joints_xyz[6];
        connected_joints_xyz[4] = joints_xyz[0];
        connected_joints_xyz[5] = joints_xyz[11];
        connected_joints_xyz[6] = joints_xyz[1];
        connected_joints_xyz[7] = joints_xyz[2];
        connected_joints_xyz[8] = joints_xyz[2];
        connected_joints_xyz[9] = joints_xyz[3];
        connected_joints_xyz[10] = joints_xyz[3];
        connected_joints_xyz[11] = joints_xyz[4];
        connected_joints_xyz[12] = joints_xyz[4];
        connected_joints_xyz[13] = joints_xyz[5];
        connected_joints_xyz[14] = joints_xyz[6];
        connected_joints_xyz[15] = joints_xyz[7];
        connected_joints_xyz[16] = joints_xyz[7];
        connected_joints_xyz[17] = joints_xyz[8];
        connected_joints_xyz[18] = joints_xyz[8];
        connected_joints_xyz[19] = joints_xyz[9];
        connected_joints_xyz[20] = joints_xyz[9];
        connected_joints_xyz[21] = joints_xyz[10];
        connected_joints_xyz[22] = joints_xyz[11];
        connected_joints_xyz[23] = joints_xyz[12];
        connected_joints_xyz[24] = joints_xyz[12];
        connected_joints_xyz[25] = joints_xyz[13];
        connected_joints_xyz[26] = joints_xyz[13];
        connected_joints_xyz[27] = joints_xyz[14];
        connected_joints_xyz[28] = joints_xyz[13];
        connected_joints_xyz[29] = joints_xyz[17];
        connected_joints_xyz[30] = joints_xyz[13];
        connected_joints_xyz[31] = joints_xyz[24];
        connected_joints_xyz[32] = joints_xyz[14];
        connected_joints_xyz[33] = joints_xyz[15];
        connected_joints_xyz[34] = joints_xyz[15];
        connected_joints_xyz[35] = joints_xyz[16];
        connected_joints_xyz[36] = joints_xyz[17];
        connected_joints_xyz[37] = joints_xyz[18];
        connected_joints_xyz[38] = joints_xyz[18];
        connected_joints_xyz[39] = joints_xyz[19];
        connected_joints_xyz[40] = joints_xyz[19];
        connected_joints_xyz[41] = joints_xyz[20];
        connected_joints_xyz[42] = joints_xyz[20];
        connected_joints_xyz[43] = joints_xyz[21];
        connected_joints_xyz[44] = joints_xyz[20];
        connected_joints_xyz[45] = joints_xyz[23];
        connected_joints_xyz[46] = joints_xyz[21];
        connected_joints_xyz[47] = joints_xyz[22];
        connected_joints_xyz[48] = joints_xyz[24];
        connected_joints_xyz[49] = joints_xyz[25];
        connected_joints_xyz[50] = joints_xyz[25];
        connected_joints_xyz[51] = joints_xyz[26];
        connected_joints_xyz[52] = joints_xyz[26];
        connected_joints_xyz[53] = joints_xyz[27];
        connected_joints_xyz[54] = joints_xyz[27];
        connected_joints_xyz[55] = joints_xyz[28];
        connected_joints_xyz[56] = joints_xyz[27];
        connected_joints_xyz[57] = joints_xyz[30];
        connected_joints_xyz[58] = joints_xyz[28];
        connected_joints_xyz[59] = joints_xyz[29];
    }

    public void SetBufferIndex(int buffer_index) {
        vertex_buffer_index = buffer_index;
    }
    public void UpdateVertexBuffer(float[] vertices){
        // add lines to skeleton points and place in 1D array
        int index = vertex_buffer_index * 4;
        for(int i = 0; i < connected_joints_xyz.length; i++){
            vertices[index++] = connected_joints_xyz[i][0];
            vertices[index++] = connected_joints_xyz[i][1];
            vertices[index++] = connected_joints_xyz[i][2];
            vertices[index++] = 1;
        }
    }
    public void Draw(int gl_program_id){
        // get handle to fragment shader's vColor member
        float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        GLES20.glLineWidth(50f);
        int mColorHandle = GLES20.glGetUniformLocation(gl_program_id, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, vertex_buffer_index, vertex_num);
    }
}