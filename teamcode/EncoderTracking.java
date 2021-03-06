package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.GyroSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorMRRangeSensor;

import java.util.Arrays;


@TeleOp(name = "EncoderTracking", group =  "Telep")
public class EncoderTracking extends LinearOpMode {

    //Motors
    DcMotor lw;
    DcMotor rw;
    DcMotor blw;
    DcMotor brw;
    GyroSensor gyro;
    //Speed
    double speedAdjust = 7.5;

    int buttonPress = 0;
    boolean buttonA = true;

    int notdone = 0;

    // Gyro settings (works fine but can be improved
    double adjSpeed = 0.027;
    double minTurn = 0.007;
    int windowSize = 1;
    int targetDegree = 0;
    // piecewise graph function settings
    int piecewiseWindow = 30;
    double piecewiseSpeed = 0.007517647057771725;
    double piecewiseMinTurn = 0.004;

    // Line to make the power variables public
    public double leftPower, rightPower, backLeftPower, backRightPower;

    double timeARunTime;

    // Function to make invalid degrees valid
    public int degreeCalc(int degree){
        int returnDegree = degree;
        if(returnDegree < 0){
            returnDegree = returnDegree + 360;
        }
        if(returnDegree >= 360){
            returnDegree = returnDegree - 360;
        }
        return returnDegree;

    }

    // Turn function
    // when called to it sets the turn power variables to turn right the desired amount
    public void turnPower(double amount){
        // use a positive parameter to turn right (clockwise)
        // use a negative parameter to turn left (counterclockwise)
        leftPower += amount;
        rightPower -= amount;
        backLeftPower += amount;
        backRightPower -= amount;
    }

    public void toPosition(){
        int[] ticks = new int[4];
        double lwBasePower;
        double blwBasePower;
        double rwBasePower;
        double brwBasePower;

        //Set the target position and tell motors to go to it
        lw.setTargetPosition(0);
        blw.setTargetPosition(0);
        brw.setTargetPosition(0);
        rw.setTargetPosition(0);
        lw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        blw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        brw.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //While motors are going to target position, update the speed at different rates based on their ticks
        while(lw.isBusy() || blw.isBusy() || rw.isBusy() || brw.isBusy()){
            ticks[0] = Math.abs(lw.getCurrentPosition());
            ticks[1] = Math.abs(blw.getCurrentPosition());
            ticks[2] = Math.abs(brw.getCurrentPosition());
            ticks[3] = Math.abs(rw.getCurrentPosition());
            Arrays.sort(ticks);

            lwBasePower = -lw.getCurrentPosition() * 1.0 / ticks[3];
            blwBasePower = -blw.getCurrentPosition() * 1.0 / ticks[3];
            rwBasePower = -rw.getCurrentPosition() * 1.0 / ticks[3];
            brwBasePower = -brw.getCurrentPosition() * 1.0 / ticks[3];

            if(ticks[3] > 1000) {
                lw.setPower(lwBasePower / 3);
                blw.setPower(blwBasePower / 3);
                rw.setPower(rwBasePower / 3);
                brw.setPower(brwBasePower / 2);
            }
            else if(ticks[3] < 500){
                lw.setPower(lwBasePower / 8);
                blw.setPower(blwBasePower / 8);
                rw.setPower(rwBasePower / 8);
                brw.setPower(brwBasePower / 8);
            }
            else {
                lw.setPower(lwBasePower / 5);
                blw.setPower(blwBasePower / 5);
                rw.setPower(rwBasePower / 5);
                brw.setPower(brwBasePower / 5);
            }
            telemetry.addData("tics", lw.getCurrentPosition());
        }

        //After motors are done, set power to 0 to all of them.
        lw.setPower(0);
        blw.setPower(0);
        rw.setPower(0);
        brw.setPower(0);
        lw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        blw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        brw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


    }


    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        lw = hardwareMap.dcMotor.get("lw");
        rw = hardwareMap.dcMotor.get("rw");
        rw.setDirection(DcMotor.Direction.REVERSE);
        brw = hardwareMap.dcMotor.get("brw");
        brw.setDirection(DcMotor.Direction.REVERSE);
        blw = hardwareMap.dcMotor.get("blw");

        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();
        lw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        blw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        brw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        blw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        brw.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            int heading = gyro.getHeading();

            telemetry.addData("lw ticks:", lw.getCurrentPosition());
            telemetry.update();

            if (gamepad1.a) {
                if (buttonA) {
                    buttonA = false;
                    telemetry.update();
                    toPosition();
                    buttonPress++;
                }
            }
            else{

                buttonA = true;
            }


            /* if(gamepad2.right_trigger >= 0.7)
            {
                if(bufferR) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferR = false;
                    if (toggleR) {

                        toggleR = false;
                        toggleA = true;
                        toggleB = true;
                        toggleX = true;
                        toggleL = true;
                    } else {

                        toggleR = true;
                    }

                }
            } else {
                bufferR = true;
            }*/



            // Inputs to the motors
            leftPower = (-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * (-2.5 / 10);
            rightPower = (-gamepad1.left_stick_y - gamepad1.left_stick_x- gamepad1.right_stick_x) * (-2.5 / 10);
            backLeftPower = (-gamepad1.left_stick_y - gamepad1.left_stick_x+ gamepad1.right_stick_x) * (-2.5 / 10);
            backRightPower = (-gamepad1.left_stick_y + gamepad1.left_stick_x- gamepad1.right_stick_x) * (-2.5 / 10);
            telemetry.addData("leftPower", leftPower);
            telemetry.addData("rightPower", rightPower);
            telemetry.addData("backLeftPower", backLeftPower);
            telemetry.addData("backRightPower", backRightPower);
            telemetry.update();
            lw.setPower(leftPower);
            rw.setPower(rightPower);
            blw.setPower(backLeftPower);
            brw.setPower(backRightPower);

            // targetDegree is currently affected by the right stick and it changes the degrees the robot goes towards
           /*targetDegree += gamepad1.right_stick_x;
           targetDegree = degreeCalc(targetDegree);

            if (degreeCalc(gyro.getHeading() - targetDegree) > windowSize + piecewiseWindow && degreeCalc(gyro.getHeading() - targetDegree) <= 180) {
                if ((Math.pow(degreeCalc(gyro.getHeading() - targetDegree) * adjSpeed, 2)) >= minTurn) {
                    turnPower(-( Math.pow(degreeCalc(gyro.getHeading() - targetDegree) * adjSpeed, 2)));
                }
                else{
                    turnPower(-minTurn);
                }
            }

            if (degreeCalc(gyro.getHeading() - targetDegree) < 360 - windowSize - piecewiseWindow && degreeCalc(gyro.getHeading() - targetDegree) > 180) {
                if ((Math.pow((360 - degreeCalc(gyro.getHeading() - targetDegree)) * adjSpeed, 2) >= minTurn)) {
                    turnPower(Math.pow((360 - degreeCalc(gyro.getHeading() - targetDegree)) * adjSpeed, 2));
                }
                else {
                    turnPower(minTurn);
                }
            }
            // Second graph function (piecewise) the one that is closer to 0 degrees
            if (degreeCalc(gyro.getHeading() - targetDegree) > windowSize && degreeCalc(gyro.getHeading() - targetDegree) <= piecewiseWindow + windowSize) {
                if ((Math.sqrt(degreeCalc(gyro.getHeading() - targetDegree) * piecewiseSpeed)) >= minTurn) {
                    turnPower(-(Math.sqrt(degreeCalc(gyro.getHeading() - targetDegree) * piecewiseSpeed)));
                }
                else {
                    turnPower(-minTurn);
                }
            }

            if (degreeCalc(gyro.getHeading() - targetDegree) < 360 - windowSize && degreeCalc(gyro.getHeading() - targetDegree) > 360 - piecewiseWindow - windowSize) {
                if (Math.sqrt((360 - degreeCalc(gyro.getHeading() - targetDegree)) * piecewiseSpeed) >= minTurn) {
                    turnPower(Math.sqrt((360 - degreeCalc(gyro.getHeading() - targetDegree)) * piecewiseSpeed) + piecewiseMinTurn);
                }
                else {
                    turnPower(minTurn);
                }
            }*/




            /*if(gamepad1.right_bumper){
                targetDegree = 344;
            }

            if(gamepad1.left_bumper){
                targetDegree = 16;
            }

            if(gamepad1.dpad_right){
                targetDegree = 0;
            } */
        }
    }
}