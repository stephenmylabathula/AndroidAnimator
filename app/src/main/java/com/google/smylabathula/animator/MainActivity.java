package com.google.smylabathula.animator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private AnimationSurfaceView animationSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animationSurfaceView = new AnimationSurfaceView(this);
        setContentView(animationSurfaceView);
    }

}
