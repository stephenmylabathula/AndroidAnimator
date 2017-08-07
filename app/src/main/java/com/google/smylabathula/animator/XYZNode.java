package com.google.smylabathula.animator;

import jeigen.DenseMatrix;

/**
 * Created by smylabathula on 8/3/17.
 */

public class XYZNode {
    DenseMatrix rot;
    double[] xyz;

    public XYZNode(){
        rot = new DenseMatrix("0 0 0; 0 0 0; 0 0 0");
        xyz = new double[3];
    }
}
