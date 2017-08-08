package com.google.smylabathula.animator;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private AnimationSurfaceView animationSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        /*TextView mTextView = new TextView(this);
        mTextView.setText("YourText");
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;*/
        animationSurfaceView = new AnimationSurfaceView(this, actionBar);
        setContentView(animationSurfaceView);
        //addContentView(mTextView, params);
    }

}
