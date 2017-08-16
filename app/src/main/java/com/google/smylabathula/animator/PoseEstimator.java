package com.google.smylabathula.animator;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.Vector;

/**
 * Created by smylabathula on 8/3/17.
 */

public class PoseEstimator {
    public String[] action_type_str = {"JUMP", "SIT", "LEFT STOP WALK", "RIGHT STOP WALK", "LEFT STEP WALK", "RIGHT STEP WALK", "LEFT START WALK", "WALK", "STAND", "STAND POSE", "SIT POSE", "MOVE"};
    public enum ActionType {JUMP,
        SIT,
        LEFT_STOP_WALK,
        RIGHT_STOP_WALK,
        LEFT_STEP_WALK,
        RIGHT_STEP_WALK,
        LEFT_START_WALK,
        WALK,
        STAND,
        STAND_POSE,
        SIT_POSE,
        MOVE
    }
    public Vector<MotionModel> motion_models;
    public SkeletonStructure skeleton_structure;

    public void LoadMotionModels(Context context) {
        motion_models = new Vector<>();

        // Load Motion Models
        MotionModel jump_model = new MotionModel();
        jump_model.load(context.getString(R.string.jump));
        motion_models.add(jump_model);

        MotionModel sit_model = new MotionModel();
        sit_model.load(context.getString(R.string.sit));
        motion_models.add(sit_model);

        MotionModel left_stop_walk_model = new MotionModel();
        left_stop_walk_model.load(context.getString(R.string.left_stop_walk));
        motion_models.add(left_stop_walk_model);

        MotionModel right_stop_walk_model = new MotionModel();
        right_stop_walk_model.load(context.getString(R.string.right_stop_walk));
        motion_models.add(right_stop_walk_model);

        MotionModel left_step_walk_model = new MotionModel();
        left_step_walk_model.load(context.getString(R.string.left_step_walk));
        motion_models.add(left_step_walk_model);

        MotionModel right_step_walk_model = new MotionModel();
        right_step_walk_model.load(context.getString(R.string.right_step_walk));
        motion_models.add(right_step_walk_model);

        MotionModel left_start_walk_model = new MotionModel();
        left_start_walk_model.load(context.getString(R.string.left_start_walk));
        motion_models.add(left_start_walk_model);

        MotionModel walk_model = new MotionModel();
        walk_model.load(context.getString(R.string.walk));
        motion_models.add(walk_model);

        MotionModel stand_model = new MotionModel();
        stand_model.load(context.getString(R.string.stand));
        motion_models.add(stand_model);

        MotionModel stand_pose_model = new MotionModel();
        stand_pose_model.load(context.getString(R.string.stand_pose));
        motion_models.add(stand_pose_model);

        MotionModel sit_pose_model = new MotionModel();
        sit_pose_model.load(context.getString(R.string.sit_pose));
        motion_models.add(sit_pose_model);

        skeleton_structure = stand_pose_model.skeleton_structure;
    }

}
