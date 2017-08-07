package com.google.smylabathula.animator;

import java.util.Vector;

/**
 * Created by smylabathula on 8/3/17.
 */

public class MotionPhases {

    Vector<String> titles;
    Vector<Integer> indices;
    int start_index;
    int end_index;

    public MotionPhases(){
        titles = new Vector<>();
        indices = new Vector<>();
        start_index = 0;
        end_index = 0;
    }

}
