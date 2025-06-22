package com.example.mylibrary.positioning;

 class RotationMatrix {

    public double[][] getRotationMatrix(double pitch, double roll, double heading) {
        double[][] rotationMatrix = new double[3][3];

        double cosH = Math.cos(Math.toRadians(heading));
        double sinH = Math.sin(Math.toRadians(heading));
        double cosP = Math.cos(Math.toRadians(pitch));
        double sinP = Math.sin(Math.toRadians(pitch));
        double cosR = Math.cos(Math.toRadians(roll));
        double sinR = Math.sin(Math.toRadians(roll));

        rotationMatrix[0][0] = cosH * cosR - sinH * sinP * sinR;
        rotationMatrix[0][1] = -sinH * cosP;
        rotationMatrix[0][2] = cosH * sinR + sinH * sinP * cosR;
        rotationMatrix[1][0] = sinH * cosR + cosH * sinP * sinR;
        rotationMatrix[1][1] = cosH * cosP;
        rotationMatrix[1][2] = sinH * sinR - cosH * sinP * cosR;
        rotationMatrix[2][0] = -cosP * sinR;
        rotationMatrix[2][1] = sinP;
        rotationMatrix[2][2] = cosP * cosR;

        return rotationMatrix;
    }
}
