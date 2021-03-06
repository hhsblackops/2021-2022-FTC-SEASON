package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvPipeline;

public class ObjectDetector extends OpenCvPipeline {
    WebcamName logiCam;
    OpenCvCamera camera;
    Telemetry telemetry;
    Mat mat = new Mat();

    // NEED TO CHANGE THESE VARIABLES AT SOME POINT
    public enum Location {
        LEFT,
        RIGHT,
        MIDDLE,
    }
    private Location location;

    static final Rect LEFT_ROI = new Rect(
            new Point(60, 35),
            new Point(120, 75));
    static final Rect MIDDLE_ROI = new Rect(
            new Point(120, 35),
            new Point ( 140, 75));
    static final Rect RIGHT_ROI = new Rect(
            new Point(140, 35),
            new Point(200, 75));
    static double PERCENT_COLOR_THRESHOLD = 0.4;
    public ObjectDetector(Telemetry t) {
        telemetry = t;
    }

    @Override
    public Mat processFrame(Mat input) {
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV);
        Scalar lowHSV = new Scalar(23,50,70);
        Scalar highHSV = new Scalar(32,255,255);

        Core.inRange(mat, lowHSV, highHSV, mat);

        Mat left = mat.submat(LEFT_ROI);
        Mat right = mat.submat(RIGHT_ROI);
        Mat middle = mat.submat(MIDDLE_ROI);

        double leftValue = Core.sumElems(left).val[0] / LEFT_ROI.area() / 255;
        double rightValue = Core.sumElems(right).val[0] / RIGHT_ROI.area() / 255;
        double middleValue = Core.sumElems(middle).val[0] / MIDDLE_ROI.area() / 255;

        left.release();
        right.release();
        middle.release();

        telemetry.addData("Left raw value", (int) Core.sumElems(left).val[0]);
        telemetry.addData("Right raw value", (int) Core.sumElems(right).val[0]);
        telemetry.addData("Middle raw value", (int) Core.sumElems(middle).val[0]);
        telemetry.addData("Left percentage", Math.round(leftValue * 100));
        telemetry.addData("Right percentage", Math.round(rightValue * 100));
        telemetry.addData("Middle percentage", Math.round(middleValue * 100));

        boolean stoneLeft = leftValue > PERCENT_COLOR_THRESHOLD;
        boolean stoneMiddle = middleValue > PERCENT_COLOR_THRESHOLD;
        boolean stoneRight = rightValue > PERCENT_COLOR_THRESHOLD;

        if (stoneMiddle) {
            // it is in the middle
            location = Location.MIDDLE;
            telemetry.addData("Object Location", "middle");
        }
        else if (stoneLeft) {
            // it is on the left
            location = Location.LEFT;
            telemetry.addData("Object Location", "left");
        }
        else {
            // it is on the right
            location = Location.RIGHT;
            telemetry.addData("Object Location", "right");
        }
        telemetry.update();

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB);

        Scalar colorStone = new Scalar(255, 0, 0);
        Scalar colorSkystone = new Scalar(0,255,0);

        Imgproc.rectangle(mat, LEFT_ROI, location == Location.LEFT? colorSkystone:colorStone);
        Imgproc.rectangle(mat, RIGHT_ROI, location == Location.RIGHT? colorSkystone:colorStone);
        Imgproc.rectangle(mat, MIDDLE_ROI, location == Location.MIDDLE? colorSkystone:colorStone);

        return mat;
    }

    public Location getLocation() {
        return location;
    }
}
