package com.google.smylabathula.animator;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.util.Vector;

/**
 * Created by amedhat on 8/10/17.
 */

public class Scene {
    AppCompatActivity main_activity;
    private ActionBar action_bar;
    private String current_action_str;
    private Camera camera;
    private Room room;
    private Trajectory trajectory;
    private Skeleton skeleton;
    public int vertex_num;
    public float[] vertex_buffer;

    public Scene(AppCompatActivity activity){
        main_activity = activity;
        action_bar = activity.getSupportActionBar();
        current_action_str = "";
        CreateScene();
    }
    public Trajectory getTrajectory(){
        return trajectory;
    }

    private void CreateScene() {
        camera = new Camera();

        vertex_num = 0;
        room = new Room();
        room.SetBufferIndex(vertex_num);
        vertex_num += room.vertex_num;

        skeleton = new Skeleton();
        skeleton.SetBufferIndex(vertex_num);
        vertex_num += skeleton.vertex_num;

        trajectory = new Trajectory();
        trajectory.SetBufferIndex(vertex_num);
        vertex_num += trajectory.vertex_num;

        vertex_buffer = new float[vertex_num * 4];

        // Initialize the scene.
        String action_type = "Stand Pose";
        int phase = 0;
        double[] position_xyz = new double[3];
        double[] orientation_rpy = new double[3];
        MotionModel stand_pose_model = new MotionModel();
        stand_pose_model.load(main_activity.getString(R.string.stand_pose));
        Vector<Double> joint_angles = stand_pose_model.channels.get(0);
        Vector<Vector<Double>> joints_xyz= stand_pose_model.GetXYZPoints(joint_angles, position_xyz, orientation_rpy);
        UpdateScene(action_type, phase, joints_xyz, position_xyz, orientation_rpy[2]);
    }
    public void UpdateScene(String action_type, int phase, Vector<Vector<Double>> joints_xyz, double[] position, double yaw_angle) {
        current_action_str = action_type + " - PHASE: " + phase;
        vertex_num = 0;
        room.UpdateRoomSize(position);
        room.SetBufferIndex(vertex_num);
        vertex_num += room.vertex_num;

        skeleton.UpdatePose(joints_xyz);
        skeleton.SetBufferIndex(vertex_num);
        vertex_num += skeleton.vertex_num;

        //trajectory.Update(position);
        camera.UpdateView(position, yaw_angle, room);
        UpdateVertexBuffer();
    }
    public void UpdateVertexBuffer(){
        vertex_buffer = new float[vertex_num * 4];
        room.UpdateVertexBuffer(vertex_buffer);
        skeleton.UpdateVertexBuffer(vertex_buffer);
    }
    public void Draw(int gl_program_id){
        // Write Action
        main_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {action_bar.setTitle(current_action_str);}
        });
        camera.ApplyView(gl_program_id);
        room.Draw(gl_program_id);
        //trajectory.Draw(gl_program_id);
        skeleton.Draw(gl_program_id);
    }
    public String ChangeCameraView() {
        return camera.SetToNextView();
    }
    public void ChangeCameraView(float zoom_factor) {
        camera.UpdateZoom(zoom_factor);
    }
}
