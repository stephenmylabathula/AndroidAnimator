package com.google.smylabathula.animator;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by amedhat on 8/9/17.
 */

public class ActionTagReceiver implements Runnable {
    boolean run_online;
    Socket socket;
    InputStream inputStream;
    Thread action_phase_receiver;
    String test_data = "";
    float[][] action_phases;
    int max_queue_size;
    BlockingQueue<ActionPhase> action_phase_queue;

    // Offline constructor
    public ActionTagReceiver(Context context) {
        test_data = context.getString(R.string.test_data);
        run_online = false;
        // 1. Read the Json data stream file.
        try {
            JSONArray action_data_json = new JSONArray(test_data);
            action_phases = new float[31030][9];

            for (int i = 0; i < 31030; i++) {
                for (int j = 0; j < 9; j++) {
                    action_phases[i][j] = (float) action_data_json.getJSONArray(i).getDouble(j);
                }
            }
            Initialize();
        } catch (JSONException e) {
            // do nothing
        }

    }
    // Online constructor
    public ActionTagReceiver(){
        run_online = true;
        Initialize();
    }
    private void Initialize() {
        max_queue_size = 5000;
        action_phase_queue   = new LinkedBlockingQueue<ActionPhase>(max_queue_size);
        // Initialize members
        action_phase_receiver = new Thread(this);
    }
    public void Start(){
        action_phase_receiver.start();
    }
    public BlockingQueue<ActionPhase> getActionPhaseQueue(){
        return action_phase_queue;
    }

    private boolean EstablishOnlineConnection(){
        System.out.println("Establishing Action tag connection...");
        boolean connected = false;
        for(int trial = 0; trial<300; trial++) {
            // Setup Sockets and Threads
            try {
                System.out.println("Action tag connection trial:" + trial);
                socket = new Socket("192.168.43.1", 30000);
                inputStream = socket.getInputStream();
                connected = true;
            } catch (Exception e) {
                e.printStackTrace();
                connected = false;
            }
            if(connected) {
                System.out.println("Action tag connection succeeded: connection status = " + connected);
                return connected;
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    //
                }
            }
        }
        System.out.println("Action tag connection failed: connection status = " + connected);
        return connected;
    }

    public void run() {
        if(run_online) {
            if(EstablishOnlineConnection()){
                RunOnline();
            } else {
                System.exit(0);
            }
        } else {
            RunOffline();
        }
    }
    void RunOffline(){
        // Push phases into the queue.
        action_phases[0][0] = 1;
        for (int i = 0; i < action_phases.length; i++) {
            if (action_phases[i][0] == 1.0) {
                ActionPhase action_phase = new ActionPhase();
                action_phase.status = true;
                action_phase.type = (int) action_phases[i][1];
                action_phase.phase = (int) action_phases[i][2];
                action_phase.start_index = (long) action_phases[i][3];
                action_phase.end_index = (long) action_phases[i][4];
                try {
                    action_phase_queue.put(action_phase);
                } catch (Exception e) {
                    // Throws:
                    // InterruptedException - if interrupted while waiting
                    // ClassCastException - if the class of the specified element prevents it from being added to this queue
                    // NullPointerException - if the specified element is null
                    // IllegalArgumentException - if some property of the specified element prevents it from being added to this queue
                }
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
                //
            }
        }
    }

    private void RunOnline(){
        int prev_action_type = -1;
        int prev_action_phase = -1;
        while (true) {
            try {
                while (inputStream.available() > 0) {
                    byte[] inputBytes = new byte[1024];
                    inputStream.read(inputBytes);
                    String jsonData = new String(inputBytes);
                    try {
                        JSONArray action_phase_json = new JSONArray(jsonData);
                        ActionPhase action_phase = new ActionPhase();
                        action_phase.status = true;
                        action_phase.type = action_phase_json.getInt(0);
                        action_phase.phase = action_phase_json.getInt(1);
                        try {
                            if (action_phase.type != prev_action_type || action_phase.phase != prev_action_phase) {
                                action_phase.start_index = (long) action_phase_json.getInt(2);
                                action_phase.end_index = (long) action_phase_json.getInt(3);
                                action_phase_queue.put(action_phase);
                                prev_action_type = action_phase.type;
                                prev_action_phase = action_phase.phase;
                                System.out.println("Action type: " + action_phase.type + " phase : " + action_phase.phase);
                            }
                        } catch (Exception e) {
                            // Throws:
                            // InterruptedException - if interrupted while waiting
                            // ClassCastException - if the class of the specified element prevents it from being added to this queue
                            // NullPointerException - if the specified element is null
                            // IllegalArgumentException - if some property of the specified element prevents it from being added to this queue
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
