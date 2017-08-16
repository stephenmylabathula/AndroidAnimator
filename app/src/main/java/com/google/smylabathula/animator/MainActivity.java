package com.google.smylabathula.animator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private AnimationSurfaceView animation_viewer;
    private ActionTagReceiver action_tag_receiver;
    private PoseReceiver pose_receiver;
    private SceneGenerator scene_generator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an online receiver.
        action_tag_receiver = new ActionTagReceiver();
        // Create an offline receiver.
        //action_tag_receiver = new ActionTagReceiver(this);
        // Create the scene generator.
        scene_generator = new SceneGenerator(this, action_tag_receiver.getActionPhaseQueue());

        Scene animated_scene = scene_generator.getScene();
        // Create the online pose receiver.
        pose_receiver = new PoseReceiver(animated_scene.getTrajectory());
        // Create the offline pose receiver.
        //pose_receiver = new PoseReceiver(this, animated_scene.getTrajectory());

        // Create the animation viewer.
        animation_viewer = new AnimationSurfaceView(this, animated_scene);
        this.setContentView(animation_viewer);

        // Start operation.
        pose_receiver.Start();
        action_tag_receiver.Start();
        scene_generator.Start();
    }
}
