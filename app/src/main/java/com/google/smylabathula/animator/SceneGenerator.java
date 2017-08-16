package com.google.smylabathula.animator;

import android.support.v7.app.AppCompatActivity;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import static com.google.smylabathula.animator.PoseEstimator.ActionType.SIT_POSE;
import static com.google.smylabathula.animator.PoseEstimator.ActionType.STAND_POSE;

/**
 * Created by amedhat on 8/10/17.
 */

public class SceneGenerator implements Runnable {

    private PoseEstimator pose_estimator;
    private Scene scene;
    private Thread scene_generator;
    private BlockingQueue<ActionPhase> action_phase_queue;
    double[] last_position;
    double[] last_orientation;

    public SceneGenerator(AppCompatActivity main_activity, BlockingQueue<ActionPhase> action_queue){
        action_phase_queue = action_queue;
        pose_estimator = new PoseEstimator();
        pose_estimator.LoadMotionModels(main_activity);
        scene = new Scene(main_activity);
        scene_generator = new Thread(this);
        last_position = new double[3];
        last_orientation = new double[3];
    }
    public void Start(){
        scene_generator.start();
    }
    public Scene getScene() {
        return scene;
    }

    public void run(){
        while (true) {
            try {
                ActionPhase action_phase = action_phase_queue.take();
                GenerateSceneSequence(action_phase);
            } catch (InterruptedException ie) {
                // InterruptedException - if interrupted while waiting for action.
            }
        }
    }

    // Generates Animation for Action Phase Tag.
    private void GenerateSceneSequence(ActionPhase action_phase) {
        /*while(pose_queue not empty) {
            //read pose from queue
            pose = pose_queue.pop();
            scene.getTrajectory().AddPose(pose);
        }*/
        // Get motion model for current action.
        MotionModel current_model = pose_estimator.motion_models.get(action_phase.type);

        String action_type_str = pose_estimator.action_type_str[action_phase.type];
        int action_phase_value = action_phase.phase;
        int model_start_index = current_model.motion_phases.indices.get(Math.max(action_phase.phase - 1, 0)); //should be phase - 1 when fixed
        int model_end_index = current_model.motion_phases.indices.get(Math.min(action_phase.phase, current_model.motion_phases.indices.size() - 1));
        long motion_start_index = action_phase.start_index;
        long motion_end_index = action_phase.end_index;

        int model_to_motion_time_ratio;
        if (model_end_index - model_start_index <= 0)
            model_to_motion_time_ratio = 10;
        else
            model_to_motion_time_ratio = Math.max((Math.round((motion_end_index - motion_start_index) / (model_end_index - model_start_index)) * 5), 1);

        double[] position_xyz = new double[3];
        double[] orientation_rpy = new double[3];
        //int motion_duration = (int) (motion_end_index - motion_start_index +1);
        for (int i = model_start_index; i <= model_end_index; i++) {
            //int model_channel_index = Math.round(((float) model_start_index) + model_to_motion_time_ratio * ((float)i));
            double alpha = ((double) (model_end_index - i))/((double)(model_end_index - model_start_index));
            long imu_index = Math.round(alpha * motion_start_index + (1-alpha) * motion_end_index);
            Vector<Double> joint_angles = current_model.channels.get(i);
            joint_angles.set(0,0.0);
            joint_angles.set(1,0.0);
            joint_angles.set(2,0.0);
            scene.getTrajectory().getPose(imu_index, position_xyz, orientation_rpy);
            if((action_phase.type ==  STAND_POSE.ordinal()) || (action_phase.type == SIT_POSE.ordinal())) {
                position_xyz = last_position;
                orientation_rpy = last_orientation;
            } else {
                last_position = position_xyz;
                last_orientation = orientation_rpy;
            }
            Vector<Vector<Double>> joints_xyz = current_model.GetXYZPoints(joint_angles, position_xyz, orientation_rpy);
            scene.UpdateScene(action_type_str, action_phase_value, joints_xyz, position_xyz, orientation_rpy[2]);
            try {
                Thread.sleep(model_to_motion_time_ratio);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
