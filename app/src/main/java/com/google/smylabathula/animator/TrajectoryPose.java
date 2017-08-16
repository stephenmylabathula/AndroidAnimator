package com.google.smylabathula.animator;

/**
 * Created by amedhat on 8/14/17.
 */

public class TrajectoryPose {
    long imu_index;
    double[] position_xyz;
    double[] orientation_rpy;
    public TrajectoryPose(){
        imu_index = 0;
        position_xyz = new double[3];
        orientation_rpy = new double[3];
    }
    public TrajectoryPose(long index, double[] position, double[] orientation){
        imu_index = index;
        position_xyz = position;
        orientation_rpy = orientation;
    }
}
