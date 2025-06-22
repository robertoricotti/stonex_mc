package gui.draw_class;

public class DisplayCoordCalc {
    float[] output=new float[4];
    double mQ;
    double mM;
   boolean left,up,right,down;

    public DisplayCoordCalc(double angle, float origineX, float origineY, float getWidth, float getHeight) {
        mM = Math.tan(Math.toRadians(angle));
        mQ = origineY - mM * origineX;
        double leftIntersectY =  (mM * 0 + mQ);
        double rightIntersectY =  (mM * getWidth + mQ);
        double topIntersectX =  ((0 - mQ) / mM);
        double bottomIntersectX =  ((getHeight - mQ) / mM);
        output[0] = (float) leftIntersectY;//sinistra (0,y)
        output[1] = (float) rightIntersectY;//destra (getwidth,y)
        output[2] = (float) topIntersectX;//top (x,0)
        output[3] = (float) bottomIntersectX;//bottom (x,getheight)
        up=topIntersectX >= 0 && topIntersectX <= getWidth;
        right=rightIntersectY >= 0 && rightIntersectY <= getHeight;
        down= bottomIntersectX >= 0 && bottomIntersectX <= getWidth;
        left=leftIntersectY >= 0 && leftIntersectY <= getHeight;
    }

    public float[] getOutput() {
        return output;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isLeft() {
        return left;
    }
}
