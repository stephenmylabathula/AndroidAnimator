package com.google.smylabathula.animator;

import java.util.Vector;

import jeigen.DenseMatrix;

/**
 * Created by smylabathula on 8/3/17.
 */

public class SkeletonStructure {

    private Vector<XYZNode> xyz_node_list;

    int length;
    int mass;
    String angle;
    String type;
    String documentation;
    String name;
    Vector<JointNode> tree;
    double model_height;
    int head_index;
    int left_foot_index;
    int right_foot_index;

    final double deg2rad = Math.PI / 180;

    public SkeletonStructure(){
        xyz_node_list = new Vector<>(31);
        for (int i = 0; i < 31; i++)
            xyz_node_list.add(i, new XYZNode());
        length = 0;
        mass = 0;
        angle = "";
        type = "";
        documentation = "";
        name = "";
        tree = new Vector<>();
        model_height = 0.0f;
        head_index = 0;
        left_foot_index = 0;
        right_foot_index = 0;
    }

    public Vector<Vector<Double>> ComputeSkeletonXYZPose(Vector<Double> channel){
        // calculate the joint rotation values
        Vector<Double> rotVal = (Vector<Double>) this.tree.get(0).orientation.clone();
        for (int i = 0; i < 3; i++) {
            int rind = this.tree.get(0).rotind[i];
            if (rind >= 0)
                rotVal.set(i, rotVal.get(i) + channel.get(rind));
        }

        // calculate the rotation matrix for the first joint
        DenseMatrix rotation_matrix = RotationMatrix(rotVal.get(0) * deg2rad, rotVal.get(1) * deg2rad, rotVal.get(2) * deg2rad, this.tree.get(0).axis_order);
        this.xyz_node_list.get(0).rot = rotation_matrix; // save the rotation matrix

        // calculate the position for the first joint
        for (int i = 0; i < 3; i++){
            this.xyz_node_list.get(0).xyz[i] = this.tree.get(0).offset[i];
            int pind = this.tree.get(0).posind[i];
            if (pind >= 0)
                this.xyz_node_list.get(0).xyz[i] = this.xyz_node_list.get(0).xyz[i] + channel.get(pind);
        }

        // recursively calculate the rotation and xyz for all joints
        for (int i = 0; i < this.tree.get(0).children.size(); i++){
            int ind = this.tree.get(0).children.get(i);
            GetChildXYZ(ind, channel);
        }

        // add all positions into the final vector
        Vector<Vector<Double>> xyz = new Vector<>(31); //31, std::vector<float>(3));
        for (int i = 0; i < 31; i++)
            xyz.add(new Vector<Double>(3));
        for (int i = 0; i < this.xyz_node_list.size(); i++){
            xyz.get(i).add(this.xyz_node_list.get(i).xyz[0]);
            xyz.get(i).add(this.xyz_node_list.get(i).xyz[1]);
            xyz.get(i).add(this.xyz_node_list.get(i).xyz[2]);
        }
        return xyz;
    }

    private void GetChildXYZ(int ind, Vector<Double> channel){
        // get this joint's (denoted by ind) parent
        int parent = this.tree.get(ind).parent;
        Vector<Integer> children = (Vector<Integer>) this.tree.get(ind).children.clone(); // get children
        double rotVal[] = {0, 0, 0};

        // get joint rotation values
        for (int i = 0; i < 3; i++){
            int rind = this.tree.get(ind).rotind[i];
            if(rind >= 0)
                rotVal[i] = channel.get(rind);
            else
                rotVal[i] = 0;
        }

        // create the rotation matrices
        DenseMatrix tdof = RotationMatrix(rotVal[0] * deg2rad, rotVal[1] * deg2rad, rotVal[2] * deg2rad, this.tree.get(ind).order);
        DenseMatrix torient = RotationMatrix(this.tree.get(ind).axis[0] * deg2rad, this.tree.get(ind).axis[1] * deg2rad, this.tree.get(ind).axis[2] * deg2rad, this.tree.get(ind).axis_order);
        DenseMatrix torientInv = torient.t();

        // apply the rotations
        this.xyz_node_list.get(ind).rot = mmult(mmult(mmult(torientInv, tdof), torient), this.xyz_node_list.get(parent).rot);
        DenseMatrix offset_as_vector;
        offset_as_vector = new DenseMatrix(new double[][]{{this.tree.get(ind).offset[0], this.tree.get(ind).offset[1], this.tree.get(ind).offset[2]}});
        DenseMatrix xyz_as_vector;
        DenseMatrix mult = mmult(offset_as_vector, this.xyz_node_list.get(ind).rot);

        // add this calculated xyz to our list of XYZ nodes
        this.xyz_node_list.get(ind).xyz[0] = (float) (this.xyz_node_list.get(parent).xyz[0] + mult.get(0,0));
        this.xyz_node_list.get(ind).xyz[1] = (float) (this.xyz_node_list.get(parent).xyz[1] + mult.get(0,1));
        this.xyz_node_list.get(ind).xyz[2] = (float) (this.xyz_node_list.get(parent).xyz[2] + mult.get(0,2));

        // recurse
        for (int i = 0; i < children.size(); i++){
            int cind = children.get(i);
            GetChildXYZ(cind, channel);
        }
    }

    private DenseMatrix RotationMatrix(double xangle, double yangle, double zangle, String order){
        // if no order set the default order
        if(order.isEmpty())
            order = "zxy";

        // calculate cosine and sine of each angle
        double c1 = Math.cos(xangle);
        double c2 = Math.cos(yangle);
        double c3 = Math.cos(zangle);
        double s1 = Math.sin(xangle);
        double s2 = Math.sin(yangle);
        double s3 = Math.sin(zangle);

        DenseMatrix rotmat;   // hold the rotation matrix

        if(order.equals("zxy"))
            // set rotation matrix for 'zxy'
            rotmat = new DenseMatrix(new double[][]{{c2*c3-s1*s2*s3, c2*s3+s1*s2*c3, -s2*c1}, {-c1*s3, c1*c3, s1}, {s2*c3+c2*s1*s3, s2*s3-c2*s1*c3, c2*c1}});
        else {
            rotmat = DenseMatrix.eye(3);
            DenseMatrix temp;
            for (int i = 0; i < order.length(); i++)
            if(order.charAt(i) == 'x'){
                temp = new DenseMatrix(new double[][]{{1, 0, 0}, {0, c1, s1}, {0, -s1, c1}});
                rotmat = mmult(temp, rotmat);
            } else if(order.charAt(i) == 'y'){
                temp = new DenseMatrix(new double[][]{{c2, 0, -s2}, {0, 1, 0}, {s2, 0, c2}});
                rotmat = mmult(temp, rotmat);
            } else if(order.charAt(i) == 'z'){
                temp = new DenseMatrix(new double[][]{{c3, s3, 0}, {-s3, c3, 0}, {0, 0, 1}});
                double[] t = temp.getValues();
                rotmat = mmult(temp, rotmat);
            }
        }
        return rotmat;
    }

    // return c = a * b
    public static DenseMatrix mmult(DenseMatrix a, DenseMatrix b) {
        int m1 = a.rows;
        int n1 = a.cols;
        int m2 = b.rows;
        int n2 = b.cols;
        if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] c = new double[m1][n2];
        for (int i = 0; i < m1; i++)
            for (int j = 0; j < n2; j++)
                for (int k = 0; k < n1; k++)
                    c[i][j] += a.get(i,k) * b.get(k,j);
        return new DenseMatrix(c);
    }

    // matrix-vector multiplication (y = A * x)
    public static double[] multiply(double[][] a, double[] x) {
        int m = a.length;
        int n = a[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += a[i][j] * x[j];
        return y;
    }
}




