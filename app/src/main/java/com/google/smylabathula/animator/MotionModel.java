package com.google.smylabathula.animator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import com.google.gson.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by smylabathula on 8/3/17.
 */

public class MotionModel {


    private double frame_length;
    private String name;

    Vector<Vector<Double>> channels;
    SkeletonStructure skeleton_structure;
    MotionPhases motion_phases;

    public MotionModel(){
        frame_length = 0.0f;
        name = "";
        channels = new Vector<>();
        skeleton_structure = new SkeletonStructure();
        motion_phases = new MotionPhases();
    }

    // load data from CSV
    public void load(String json_data){
        // Parse JSON
        try {
            // Get Motion Model JSON Object
            JSONObject json_document = new JSONObject(json_data);

            // Parse out the frame length value
            this.frame_length = json_document.getDouble("frame_length");

            // Parse out the skeleton
            JSONObject json_skeleton = json_document.getJSONObject("skeleton_structure");

            this.skeleton_structure.length = json_skeleton.getInt("length");
            this.skeleton_structure.mass = json_skeleton.getInt("mass");
            this.skeleton_structure.angle = json_skeleton.getString("angle");
            this.skeleton_structure.type = json_skeleton.getString("type");
            this.skeleton_structure.documentation = json_skeleton.getString("documentation");
            this.skeleton_structure.name = json_skeleton.getString("name");
            if (json_skeleton.has("model_height")) {
                this.skeleton_structure.model_height = json_skeleton.getDouble("model_height");
            } else {
                this.skeleton_structure.model_height = 1.75;
            }
            // subtract one for zero basis for index values
            if (json_skeleton.has("head_index")) {
                this.skeleton_structure.head_index = json_skeleton.getInt("head_index") - 1;
            } else {
                this.skeleton_structure.head_index = 0;
            }
            if (json_skeleton.has("left_foot_index")) {
                this.skeleton_structure.left_foot_index = json_skeleton.getInt("left_foot_index") - 1;
            } else {
                this.skeleton_structure.left_foot_index = 0;
            }
            if (json_skeleton.has("right_foot_index")) {
                this.skeleton_structure.right_foot_index = json_skeleton.getInt("right_foot_index") - 1;
            } else {
                this.skeleton_structure.right_foot_index = 0;
            }


            // Parse out the skeleton tree
            JSONArray json_tree = json_skeleton.getJSONArray("tree");
            for (int i = 0; i < json_tree.length(); i++){
                JointNode current_joint_node = new JointNode();

                current_joint_node.name = json_tree.getJSONObject(i).getString("name");

                current_joint_node.id = json_tree.getJSONObject(i).getInt("id");

                current_joint_node.offset[0] = json_tree.getJSONObject(i).getJSONArray("offset").getDouble(0);
                current_joint_node.offset[1] = json_tree.getJSONObject(i).getJSONArray("offset").getDouble(1);
                current_joint_node.offset[2] = json_tree.getJSONObject(i).getJSONArray("offset").getDouble(2);

                for (int j = 0; j < json_tree.getJSONObject(i).getJSONArray("orientation").length(); j++){
                    current_joint_node.orientation.add(json_tree.getJSONObject(i).getJSONArray("orientation").getDouble(j));
                }

                current_joint_node.axis[0] = json_tree.getJSONObject(i).getJSONArray("axis").getDouble(0);
                current_joint_node.axis[1] = json_tree.getJSONObject(i).getJSONArray("axis").getDouble(1);
                current_joint_node.axis[2] = json_tree.getJSONObject(i).getJSONArray("axis").getDouble(2);

                current_joint_node.axis_order = json_tree.getJSONObject(i).getString("axisOrder");

                current_joint_node.c.set(0,0,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(0).getDouble(0));
                current_joint_node.c.set(0,1,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(0).getDouble(1));
                current_joint_node.c.set(0,2,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(0).getDouble(2));
                current_joint_node.c.set(1,0,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(1).getDouble(0));
                current_joint_node.c.set(1,1,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(1).getDouble(1));
                current_joint_node.c.set(1,2,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(1).getDouble(2));
                current_joint_node.c.set(2,0,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(2).getDouble(0));
                current_joint_node.c.set(2,1,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(2).getDouble(1));
                current_joint_node.c.set(2,2,json_tree.getJSONObject(i).getJSONArray("C").getJSONArray(2).getDouble(2));

                current_joint_node.c_inv.set(0,0,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(0).getDouble(0));
                current_joint_node.c_inv.set(0,1,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(0).getDouble(1));
                current_joint_node.c_inv.set(0,2,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(0).getDouble(2));
                current_joint_node.c_inv.set(1,0,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(1).getDouble(0));
                current_joint_node.c_inv.set(1,1,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(1).getDouble(1));
                current_joint_node.c_inv.set(1,2,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(1).getDouble(2));
                current_joint_node.c_inv.set(2,0,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(2).getDouble(0));
                current_joint_node.c_inv.set(2,1,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(2).getDouble(1));
                current_joint_node.c_inv.set(2,2,json_tree.getJSONObject(i).getJSONArray("Cinv").getJSONArray(2).getDouble(2));

                for (int j = 0; j < json_tree.getJSONObject(i).getJSONArray("channels").length(); j++) {
                    current_joint_node.channels.add(json_tree.getJSONObject(i).getJSONArray("channels").getString(j));
                }

                if (json_tree.getJSONObject(i).get("bodymass") instanceof Double) {
                    current_joint_node.bodymass = json_tree.getJSONObject(i).getDouble("bodymass");
                } else {
                    current_joint_node.bodymass = 0.0f;
                }

                if (json_tree.getJSONObject(i).get("confmass") instanceof Double) {
                    current_joint_node.confmass = json_tree.getJSONObject(i).getDouble("confmass");
                } else {
                    current_joint_node.confmass = 0.0f;
                }

                current_joint_node.parent = json_tree.getJSONObject(i).getInt("parent") - 1;    // subtract one for zero basis

                if (json_tree.getJSONObject(i).get("order") instanceof String) {
                    current_joint_node.order = json_tree.getJSONObject(i).getString("order");
                } else {
                    current_joint_node.order = "";
                }

                // subtract one for zero basis on index values
                current_joint_node.rotind[0] = json_tree.getJSONObject(i).getJSONArray("rotInd").getInt(0) - 1;
                current_joint_node.rotind[1] = json_tree.getJSONObject(i).getJSONArray("rotInd").getInt(1) - 1;
                current_joint_node.rotind[2] = json_tree.getJSONObject(i).getJSONArray("rotInd").getInt(2) - 1;
                current_joint_node.posind[0] = json_tree.getJSONObject(i).getJSONArray("posInd").getInt(0) - 1;
                current_joint_node.posind[1] = json_tree.getJSONObject(i).getJSONArray("posInd").getInt(1) - 1;
                current_joint_node.posind[2] = json_tree.getJSONObject(i).getJSONArray("posInd").getInt(2) - 1;

                if (json_tree.getJSONObject(i).get("children") instanceof JSONArray) {
                    for (int j = 0; j < json_tree.getJSONObject(i).getJSONArray("children").length(); j++) {
                        current_joint_node.children.add(json_tree.getJSONObject(i).getJSONArray("children").getInt(j) - 1);
                    }
                } else {
                    current_joint_node.children.add(json_tree.getJSONObject(i).getInt("children") - 1);
                }

                Vector<Double> temp_for_rows = new Vector<>();
                for (int j = 0; j < json_tree.getJSONObject(i).getJSONArray("limits").length(); j++) {
                    if (json_tree.getJSONObject(i).getJSONArray("limits").get(0) instanceof JSONArray) {
                        for (int k = 0; k < json_tree.getJSONObject(i).getJSONArray("limits").getJSONArray(j).length(); k++) {
                            temp_for_rows.add(json_tree.getJSONObject(i).getJSONArray("limits").getJSONArray(j).getDouble(k));
                        }
                    } else {
                        temp_for_rows.add(json_tree.getJSONObject(i).getJSONArray("limits").getDouble(j));
                    }
                    current_joint_node.limits.add(temp_for_rows);
                    temp_for_rows.clear();
                }

                skeleton_structure.tree.add(current_joint_node);
            }

            // Parse out the channels matrix
            JSONArray json_channels = json_document.getJSONArray("channels");
            Vector<Double> temp_for_rows = new Vector<>();
            for (int i = 0; i < json_channels.length(); i++) {
                for (int j = 0; j < json_channels.getJSONArray(i).length(); j++) {
                    temp_for_rows.add(json_channels.getJSONArray(i).getDouble(j));
                }
                channels.add((Vector<Double>) temp_for_rows.clone());
                temp_for_rows.clear();
            }

            // Parse out the action phases
            JSONObject json_phases = json_document.getJSONObject("phases");
            motion_phases.start_index = json_phases.getInt("start_index") - 1;
            motion_phases.end_index = json_phases.getInt("end_index") - 1;
            for (int i = 0; i < json_phases.getJSONArray("titles").length(); i++) {
                motion_phases.titles.add(json_phases.getJSONArray("titles").getString(i));
            }
            for (int i = 0; i < json_phases.getJSONArray("indices").length(); i++) {
                motion_phases.indices.add(json_phases.getJSONArray("indices").getInt(i) - 1);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Vector<Vector<Double>> GetXYZPoints(Vector<Double> channel){
        return this.skeleton_structure.ComputeSkeletonXYZPose(channel);
    }


}
