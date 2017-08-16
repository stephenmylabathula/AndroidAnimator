package com.google.smylabathula.animator;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by amedhat on 8/11/17.
 */

public class PoseReceiver implements Runnable {
    boolean run_online;
    Trajectory trajectory;
    Thread receiver;
    // Online receiver members.
    Socket socket;
    InputStream inputStream;
    // offline receiver members.
    String offline_data_string = "";
    double[][] offline_poses;

    int max_queue_size;
    BlockingQueue<TrajectoryPose> pose_queue;

    public PoseReceiver(Context context, Trajectory _trajectory) {
        offline_data_string = context.getString(R.string.test_data);
        run_online = false;
        // 1. Read the Json data stream file.
        try {
            JSONArray action_data_json = new JSONArray(offline_data_string);
            offline_poses = new double[31030][6];
            for (int i = 0; i < 31030; i++) {
                // Read position X-Y-Z.
                offline_poses[i][0] = action_data_json.getJSONArray(i).getDouble(5);
                offline_poses[i][1] = action_data_json.getJSONArray(i).getDouble(6);
                offline_poses[i][2] = action_data_json.getJSONArray(i).getDouble(7);
                // Read orientation Roll-Pitch-Yaw.
                offline_poses[i][3] = 0.0f;
                offline_poses[i][4] = 0.0f;
                offline_poses[i][5] = action_data_json.getJSONArray(i).getDouble(8) * Math.PI / 180.0;
            }
            Initialize(_trajectory);
        } catch (JSONException e) {
            // do nothing
        }
    }
    public PoseReceiver(Trajectory _trajectory){
        run_online = true;
        Initialize(_trajectory);
    }
    private void Initialize(Trajectory _trajectory) {
        max_queue_size = 5000;
        pose_queue = new LinkedBlockingQueue<TrajectoryPose>(max_queue_size);
        // Initialize members
        trajectory = _trajectory;
        receiver = new Thread(this);
    }
    public BlockingQueue<TrajectoryPose> getPoseQueue() {
        return pose_queue;
    }
    public void Start(){
        receiver.start();
    }
    private boolean EstablishOnlineConnection(){
        System.out.println("Establishing pose connection...");
        boolean connected = false;
        for(int trial = 0; trial<300; trial++) {
            // Setup Sockets and Threads
            try {
                System.out.println("Pose connection trial:" + trial);
                socket = new Socket("192.168.43.1", 20000);
                inputStream = socket.getInputStream();
                connected = true;
            } catch (Exception e) {
                e.printStackTrace();
                connected = false;
            }
            if(connected) {
                System.out.println("Pose connection succeeded: connection status = " + connected);
                return connected;
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    //
                }
            }
        }
        System.out.println("Pose connection failed: connection status = " + connected);
        return connected;
    }
    public void run(){
        if(run_online) {
            if(EstablishOnlineConnection()) {
                RunOnline();
            } else {
                System.exit(0);
            }
        } else {
            RunOffline();
        }
    }
    void RunOffline(){
        long  imu_index = -1;
        double[] current_position_xyz = new double[3];
        double[] current_orientation_rpy = new double[3];
        // Push phases into the queue.
        for (int i = 0; i < offline_poses.length; i++) {
            imu_index = (long) i;
            current_position_xyz[0] = offline_poses[i][0];
            current_position_xyz[1] = offline_poses[i][1];
            current_position_xyz[2] = offline_poses[i][2];
            current_orientation_rpy[0] = offline_poses[i][3];
            current_orientation_rpy[1] = offline_poses[i][4];
            current_orientation_rpy[2] = offline_poses[i][5];
            trajectory.AddPose(imu_index, current_position_xyz, current_orientation_rpy);
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
                //
            }
        }
    }
    private void RunOnline(){
        long prev_imu_index = -1;
        double[] current_position_xyz = new double[3];
        double[] current_orientation_rpy = new double[3];
        while (true) {
            try {
                while (inputStream.available() > 0) {
                    byte[] inputBytes = new byte[1024];
                    inputStream.read(inputBytes);
                    String jsonData = new String(inputBytes);
                    try {
                        JSONArray pose_json = new JSONArray(jsonData);
                        long imu_index = pose_json.getLong(0);
                        if(prev_imu_index < imu_index){
                            current_position_xyz[0] = pose_json.getDouble(1);
                            current_position_xyz[1] = pose_json.getDouble(2);
                            current_position_xyz[2] = pose_json.getDouble(3);
                            current_orientation_rpy[0] = pose_json.getDouble(4);
                            current_orientation_rpy[1] = pose_json.getDouble(5);
                            current_orientation_rpy[2] = pose_json.getDouble(6);
                            prev_imu_index = imu_index;
                            trajectory.AddPose(imu_index, current_position_xyz, current_orientation_rpy);
                            System.out.println("VIO pose: " + imu_index + " position (xyz): " + current_position_xyz[0] + " , " + current_position_xyz[1] + " , " + current_position_xyz[2]);
                        }
                    } catch (JSONException e) {
                        //
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
