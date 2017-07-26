package com.google.smylabathula.animator;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by mylo on 7/26/17.
 */

public class Utilities {

    private static String csvFileX = "joints_x.csv";
    private static String csvFileY = "joints_y.csv";
    private static String csvFileZ = "joints_z.csv";

    private static int numJoints = 31;
    private static int numSamples = 24970;
    private static int numCoordinates = 4;

    public static float[][][] getData() {
        float[][][] data = new float[numSamples][numJoints][numCoordinates];

        String[][] xData = new String[numSamples][numJoints];
        String[][] yData = new String[numSamples][numJoints];
        String[][] zData = new String[numSamples][numJoints];

        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(AnimationSurfaceView.isX));
            for(int i = 0; i < numSamples; i++) {
                line = br.readLine();
                String[] jointData = line.split(",");
                xData[i] = jointData;
            }

            br = new BufferedReader(new InputStreamReader(AnimationSurfaceView.isY));
            for(int i = 0; i < numSamples; i++) {
                line = br.readLine();
                String[] jointData = line.split(",");
                yData[i] = jointData;
            }

            br = new BufferedReader(new InputStreamReader(AnimationSurfaceView.isZ));
            for(int i = 0; i < numSamples; i++) {
                line = br.readLine();
                String[] jointData = line.split(",");
                zData[i] = jointData;
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for(int i = 0; i < numSamples; i++){
            for(int j = 0; j < numJoints; j++){
                data[i][j][0] = Float.parseFloat(xData[i][j]);
                data[i][j][1] = Float.parseFloat(yData[i][j]);
                data[i][j][2] = Float.parseFloat(zData[i][j]);
                data[i][j][3] = 1f;
            }
        }

        return data;
    }


}
