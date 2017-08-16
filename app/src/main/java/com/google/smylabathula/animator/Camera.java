package com.google.smylabathula.animator;

import android.opengl.GLES20;
import android.opengl.Matrix;

import static com.google.smylabathula.animator.Camera.ViewType.BIRD_EYE_VIEW;
import static com.google.smylabathula.animator.Camera.ViewType.MODEL_VIEW;
import static com.google.smylabathula.animator.Camera.ViewType.SIDE_VIEW;
import static com.google.smylabathula.animator.Camera.ViewType.TOP_VIEW;

/**
 * Created by amedhat on 8/9/17.
 */



public class Camera {

    public enum ViewType {MODEL_VIEW, TOP_VIEW, SIDE_VIEW, BIRD_EYE_VIEW}
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] camera_position;
    private float[] looking_at;
    private float[] up_vector;
    private float[] mMVPMatrix;
    private ViewType view_type;
    private float zoom;
    private float ratio;
    private float left;
    private float right;
    private float bottom;
    private float top;
    private float near;
    private float far;

    // View Change Members
    private double[] prevPos = null;
    private double prevYawAngle = 0;
    private Room prevRoom = null;

    public Camera(){
        view_type = MODEL_VIEW;
        zoom = 1.0f;
        ratio = 1;
        left = -1;
        right = 1;
        bottom = -1;
        top = 1;
        near = 0.5f;
        far = 7.0f;
        camera_position = new float[]{1.0f, 1.0f, 1.0f};
        looking_at = new float[3];
        up_vector = new float[]{0.0f, 0.0f, 1.0f};
        mMVPMatrix = new float[16];
        CalculateViewProjectionMatrix();
    }
    private void CalculateViewProjectionMatrix() {
        float[] mProjectionMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        // Set the camera position (View matrix).
        Matrix.setLookAtM(mViewMatrix,0, camera_position[0], camera_position[1], camera_position[2],looking_at[0],looking_at[1],looking_at[2],up_vector[0],up_vector[1],up_vector[2]);
        // Set projection matrix.
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        // Calculate the projection and view transformation.
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
    }
    public void UpdateZoom(float zoom_factor) {
        zoom = 1 / zoom_factor;
        UpdateView();
    }
    public String SetToNextView() {
        if (prevPos == null || prevRoom == null)
            return "Cannot Change View Now";
        switch (view_type){
            case MODEL_VIEW:
                view_type = TOP_VIEW;
                break;
            case TOP_VIEW:
                view_type = SIDE_VIEW;
                break;
            case SIDE_VIEW:
                view_type = BIRD_EYE_VIEW;
                break;
            case BIRD_EYE_VIEW:
                view_type = MODEL_VIEW;
                break;
            default:
                view_type = MODEL_VIEW;
        }
        UpdateView();
        return view_type.toString();
    }
    private void UpdateModelView(double[] position, double yaw_angle) {
        // calculate cosine and sine of the angle
        double c3 = zoom * Math.cos(yaw_angle);
        double s3 = zoom * Math.sin(yaw_angle);
        camera_position[0] = (float) (position[0] + s3);
        camera_position[1] = (float) (position[1] + c3);
        camera_position[2] = 1.0f + zoom;
        looking_at[0] = (float) (position[0]);
        looking_at[1] = (float) (position[1]);
        looking_at[2] = 1.0f;
        up_vector[0] = 0.0f;
        up_vector[1] = 0.0f;
        up_vector[2] = 1.0f;
        left = -1;
        right = 1;
        bottom = -1;
        top = 1;
        near = 0.5f;
        far = 7.0f + zoom * 10;
    }
    private void UpdateTopView(Room room) {
        float x_half = 0.5f * (room.max_x - room.min_x);
        float y_half = 0.5f * (room.max_y - room.min_y);
        camera_position[0] = x_half + room.min_x;
        camera_position[1] = y_half + room.min_y;
        camera_position[2] = Math.max(1.732f * x_half, 1.732f* y_half);
        looking_at[0] = x_half + room.min_x;
        looking_at[1] = y_half + room.min_y;
        looking_at[2] = 0.0f;
        up_vector[0] = 0.0f;
        up_vector[1] = 1.0f;
        up_vector[2] = 0.0f;
        far = camera_position[2];
        near = Math.max(0.5f, far-3.0f);
        right = near * x_half / far;
        left = -right;
        top = near * y_half / far;
        bottom = - top;
    }
    private void UpdateSideView(double[] position, double yaw_angle, Room room) {
        // calculate cosine and sine of the angle
        double c3 = zoom * Math.cos(yaw_angle);
        double s3 = zoom * Math.sin(yaw_angle);
        camera_position[0] = (float) (position[0] + c3);
        camera_position[1] = (float) (position[1] + s3);
        camera_position[2] = 0.5f;
        looking_at[0] = (float) (position[0]);
        looking_at[1] = (float) (position[1]);
        looking_at[2] = 1.0f;
        up_vector[0] = 0.0f;
        up_vector[1] = 0.0f;
        up_vector[2] = 0.5f;
        far = 2.0f;
        near = 0.2f;
        right = 0.2f;
        left = -0.25f;
        top = 0.5f;
        bottom = -0.5f;
    }
    private void UpdateBirdEyeView(double[] position, double yaw_angle, Room room) {
        // calculate cosine and sine of the angle
        double c3 = zoom * Math.cos(yaw_angle);
        double s3 = zoom * Math.sin(yaw_angle);
        camera_position[0] = (float) (position[0] + c3);
        camera_position[1] = (float) (position[1] + s3);
        camera_position[2] = 1.0f;
        looking_at[0] = (float) (position[0]);
        looking_at[1] = (float) (position[1]);
        looking_at[2] = 1.0f;
    }

    public void UpdateView(double[] position, double yaw_angle, Room room){
        prevPos = position;
        prevRoom = room;
        prevYawAngle = yaw_angle;
        switch (view_type){
            case MODEL_VIEW:
                UpdateModelView(position, yaw_angle);
                break;
            case TOP_VIEW:
                UpdateTopView(room);
                break;
            case SIDE_VIEW:
                UpdateSideView(position, yaw_angle, room);
                break;
            case BIRD_EYE_VIEW:
                UpdateBirdEyeView(position, yaw_angle, room);
                break;
            default:
                UpdateModelView(position, yaw_angle);
        }
        CalculateViewProjectionMatrix();
    }
    public void UpdateView(){
        switch (view_type){
            case MODEL_VIEW:
                UpdateModelView(prevPos, prevYawAngle);
                break;
            case TOP_VIEW:
                UpdateTopView(prevRoom);
                break;
            case SIDE_VIEW:
                UpdateSideView(prevPos, prevYawAngle, prevRoom);
                break;
            case BIRD_EYE_VIEW:
                UpdateBirdEyeView(prevPos, prevYawAngle, prevRoom);
                break;
            default:
                UpdateModelView(prevPos, prevYawAngle);
        }
        CalculateViewProjectionMatrix();
    }
    public void ApplyView(int gl_program_id) {
        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(gl_program_id, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }
}