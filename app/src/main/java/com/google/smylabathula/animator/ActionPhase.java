package com.google.smylabathula.animator;

import java.util.Vector;

/**
 * Created by amedhat on 8/8/17.
 */

public class ActionPhase {
    int type;
    boolean status;
    int phase;
    long start_index;
    long end_index;

    public ActionPhase(){
        type = 0;
        status = false;
        phase = 0;
        start_index = 0;
        end_index = 0;
    }
}
