package com.google.smylabathula.animator;

import java.util.*;

import jeigen.DenseMatrix;

/**
 * Created by smylabathula on 8/3/17.
 */

public class JointNode {

    String name;
    int id;
    double[] offset;
    Vector<Double> orientation;
    double[] axis;
    String axis_order;
    DenseMatrix c;
    DenseMatrix c_inv;
    Vector<String> channels;
    double bodymass;
    double confmass;
    int parent;
    String order;
    int[] rotind;
    int[] posind;
    Vector<Integer> children;
    Vector<Vector<Double>> limits;

    public JointNode(){
        name = "";
        id = 0;
        offset = new double[3];
        orientation = new Vector<>();
        axis = new double[3];
        axis_order = "";
        c = new DenseMatrix("0 0 0; 0 0 0; 0 0 0");
        c_inv = new DenseMatrix("0 0 0; 0 0 0; 0 0 0");
        channels = new Vector<>();
        bodymass = 0.0f;
        confmass = 0.0f;
        parent = 0;
        order = "";
        rotind = new int[3];
        posind = new int[3];
        children = new Vector<>();
        limits = new Vector<>();
    }

}