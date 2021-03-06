package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.Arrays;


@TeleOp(name = "JaredsEncoderTracking", group =  "Telep")
public class JaredsEncoderTracking extends LinearOpMode {

    //Motors
    DcMotor lw;
    DcMotor rw;
    DcMotor blw;
    DcMotor brw;
    // DcMotor in0;
    // DcMotor in1;
    //DcMotor shoot;

    //Servos
    //Servo flick;
    // CRServo in2;
    // Servo arm;
    // Servo fingers;
    // Servo rotate;
    //  Servo drop;
    //Sensors
    GyroSensor gyro;

    //Speed
    double speedAdjust = 7.5;

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

    // button variables used for button processing
    boolean bufferA = true;
    boolean toggleA = true;
    boolean bufferB = true;
    boolean toggleB = true;
    boolean bufferX = true;
    boolean toggleX = true;
    boolean bufferY = true;
    boolean toggleY = true;
    boolean bufferL = true;
    boolean toggleL = true;
    boolean bufferR = true;
    boolean toggleR = true;
    double timeA = 0;
    boolean timeAUpdate = true;
    double timeARunTime;
    double timeB = 0;
    boolean timeBUpdate = true;
    double timeBRunTime;

    // Encoder Tracking Variables
    int previousPositionLw = 0;
    int previousPositionRw = 0;
    int previousPositionBlw = 0;
    int previousPositionBrw = 0;

    int changeInLw;
    int changeInRw;
    int changeInBlw;
    int changeInBrw;

    /*int[] lwVector = new int[2];
    int[] rwVector = new int[2];
    int[] blwVector = new int[2];
    int[] brwVector = new int[2];*/
    double[] robotMovementVector = new double[2];
    double[] normalizedMovementVector = new double[2];
    double movementDirection;
    double movementMagnitude;
    public double[] currentPosition = new double[2];


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

    public void recordChangeInPosition(){

        changeInLw = lw.getCurrentPosition() - previousPositionLw;
        changeInRw = rw.getCurrentPosition() - previousPositionRw;
        changeInBlw = blw.getCurrentPosition() - previousPositionBlw;
        changeInBrw = brw.getCurrentPosition() - previousPositionBrw;

        // Amount robot moved in local cords (recent local displacement)
        robotMovementVector[0] = (Math.sqrt(2) * changeInLw) + (-Math.sqrt(2) * changeInRw) + (-Math.sqrt(2) * changeInBlw) + (Math.sqrt(2) * changeInBrw);
        robotMovementVector[1] = (-Math.sqrt(2) * changeInLw) + (-Math.sqrt(2) * changeInRw) + (-Math.sqrt(2) * changeInBlw) + (-Math.sqrt(2) * changeInBrw);

        // Convert to global
            // consider using encoders to get the rotation of the robot (actually this is very impractical)
        /*if (robotMovementVector[0] == 0){
            normalizedMovementVector[1] = 1;
            normalizedMovementVector[0] = 0;
        }
        if (robotMovementVector[1] == 0){
            normalizedMovementVector[0] = 1;
            normalizedMovementVector[1] = 0;
        }*/
        if (robotMovementVector[0] != 0 && robotMovementVector[1] != 0) {
            double squaredX = (robotMovementVector[0] * robotMovementVector[0]);
            double squaredY = (robotMovementVector[1] * robotMovementVector[1]);
            normalizedMovementVector[0] = Math.sqrt(squaredX / (squaredX + squaredY));
            if(robotMovementVector[0] < 0)
                normalizedMovementVector[0] = -normalizedMovementVector[0];
            normalizedMovementVector[1] = Math.sqrt(squaredY / (squaredX + squaredY));
            if(robotMovementVector[1] < 0)
                normalizedMovementVector[1] = -normalizedMovementVector[1];
        }
        if(robotMovementVector[0] == 0 && robotMovementVector[1] == 0) {
            // Did not move
            normalizedMovementVector[0] = 0;
            normalizedMovementVector[1] = 1;
            //movementMagnitude = 0;
        }
            // Note: I can use tangent to get the direction, but because I already have the normalized vector, I think it would be less efficient

        movementDirection = Math.acos(normalizedMovementVector[0]) + Math.toRadians(gyro.getHeading());

        if (normalizedMovementVector[0] != 0) {
            movementMagnitude = robotMovementVector[0] / normalizedMovementVector[0];
        }
        else{
            if (normalizedMovementVector[1] != 0) {
                movementMagnitude = robotMovementVector[1] / normalizedMovementVector[1];
            }
            else{
                movementMagnitude = 0;
            }
        }
        robotMovementVector[0] = Math.cos(movementDirection) * movementMagnitude;
        robotMovementVector[1] = Math.sin(movementDirection) * movementMagnitude;

        // Add global displacement to the current position
        currentPosition[0] += robotMovementVector[0];
        currentPosition[1] += robotMovementVector[1];

        previousPositionLw = lw.getCurrentPosition();
        previousPositionRw = rw.getCurrentPosition();
        previousPositionBlw = blw.getCurrentPosition();
        previousPositionBrw = brw.getCurrentPosition();
    }

    public void toPosition(){
        int lwPositon = lw.getCurrentPosition();
        int blwPositon = blw.getCurrentPosition();
        int rwPositon = rw.getCurrentPosition();
        int brwPositon = brw.getCurrentPosition();
        int[] ticks = {Math.abs(lwPositon), Math.abs(blwPositon), Math.abs(rwPositon), Math.abs(brwPositon)};
        Arrays.sort(ticks);
        lw.setTargetPosition(0);
        blw.setTargetPosition(0);
        brw.setTargetPosition(0);
        rw.setTargetPosition(0);

        if (ticks[3] != 0) {
            lw.setPower(-lwPositon / ticks[3]);
            blw.setPower(-blwPositon / ticks[3]);
            rw.setPower(-rwPositon / ticks[3]);
            brw.setPower(-brwPositon / ticks[3]);
        }
        lw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        blw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        brw.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        while(lw.isBusy()){
            telemetry.addData("tics", lw.getCurrentPosition());
        }
        lw.setPower(0);
        blw.setPower(0);
        rw.setPower(0);
        brw.setPower(0);

    }


    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        currentPosition[0] = 0;
        currentPosition[1] = 0;
        lw = hardwareMap.dcMotor.get("lw");
        rw = hardwareMap.dcMotor.get("rw");
        rw.setDirection(DcMotor.Direction.REVERSE);
        brw = hardwareMap.dcMotor.get("brw");
        brw.setDirection(DcMotor.Direction.REVERSE);
        blw = hardwareMap.dcMotor.get("blw");
        // in0 = hardwareMap.dcMotor.get("ina");
        // in1 = hardwareMap.dcMotor.get("inb");
        //in1.setDirection(DcMotor.Direction.REVERSE);
        // in2 = hardwareMap.crservo.get("inc");
        // in2.setDirection(CRServo.Direction.FORWARD);
        // shoot = hardwareMap.dcMotor.get("shoot");
        // flick = hardwareMap.servo.get("flick");
        // flick.setPosition(1);
        //  arm = hardwareMap.servo.get("arm");
        // fingers = hardwareMap.servo.get("fingers");
        // rotate = hardwareMap.servo.get("rotate");
        //  drop = hardwareMap.servo.get("drop");
        // fingers.setPosition(.5);
        // rotate.setPosition(.9);
        // arm.setPosition(0.15);
        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();
        lw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        blw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        brw.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        int backrn = blw.getCurrentPosition();
        boolean buttonA = false;
        while (opModeIsActive()) {

            int heading = gyro.getHeading();

            recordChangeInPosition();
            telemetry.addData("1. Heading: ", heading);

            // From testing it seems that the encoders ticks are at vastly different spacings and brw does not even work right (it just alternates between 1 and 0)
            telemetry.addData("lw ticks:", lw.getCurrentPosition());
            telemetry.addData("rw ticks:", rw.getCurrentPosition());
            telemetry.addData("blw ticks:", blw.getCurrentPosition());
            telemetry.addData("brw ticks:", brw.getCurrentPosition());
            //telemetry.addData("rightControlX", gamepad1.right_stick_x);
            //telemetry.addData("rightControlY", gamepad1.right_stick_y);
            //telemetry.addData("Time", time);
            //telemetry.addData("runtimeA", timeARunTime);

            if(gamepad1.left_stick_x > 0.5){
                telemetry.addData("Stick Working", true);
            }

            telemetry.addData("Change in Lw: ", changeInLw);
            telemetry.addData("Change in Rw: ", changeInRw);
            telemetry.addData("Change in Blw: ", changeInBlw);
            telemetry.addData("Change in Brw: ", changeInBrw);
            telemetry.addData("Movement Magnitude: ", movementMagnitude);

            telemetry.addData("Current PositionX:", currentPosition[0]);
            telemetry.addData("Current PositionY:", currentPosition[1]);
            telemetry.update();

            if (gamepad1.x) {
                if (buttonA) {
                    buttonA = false;
                    toPosition();
                }
            }
            else{
                buttonA = true;
            }

            /*double adjSpeed = 0.027;
            double minTurn = 0.007;
            int windowSize = 1;
            int targetDegree = 0;
            // piecewise graph function settings
            int piecewiseWindow = 15;
            double piecewiseSpeed = 0.015;
            double piecewiseMinTurn = 0.004;*/
            /*if(!toggleA) {
                telemetry.addData("adjSpeed", adjSpeed);

                adjSpeed += gamepad2.left_stick_y;
            }
            if(!toggleB){
                telemetry.addData("minTurn", minTurn);

                minTurn += gamepad2.left_stick_y;
            }
            if(!toggleX){
                telemetry.addData("piecewiseSpeed", piecewiseSpeed);

                piecewiseSpeed += gamepad2.left_stick_y;
            }
            if(!toggleY){
                telemetry.addData("piecewiseMinTurn", piecewiseMinTurn);

                piecewiseMinTurn += gamepad2.left_stick_y;
            }
            if(!toggleL){
                telemetry.addData("windowSize", windowSize);

                windowSize += gamepad2.left_stick_y;
            }
            if(!toggleR){
                telemetry.addData("piecewiseWindow", piecewiseWindow);

                piecewiseWindow += gamepad2.left_stick_y;
            }
            telemetry.update();*/


           /* if(gamepad2.a)
            {
                if(bufferA) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferA = false;
                    if (toggleA) {

                        toggleA = false;
                        toggleB = true;
                        toggleX = true;
                        toggleY = true;
                        toggleL = true;
                        toggleR = true;
                    } else {

                        toggleA = true;
                    }

                }
            } else {
                bufferA = true;
            }
            if(gamepad2.b)
            {
                if(bufferB) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferB = false;
                    if (toggleB) {

                        toggleB = false;
                        toggleA = true;
                        toggleX = true;
                        toggleY = true;
                        toggleL = true;
                        toggleR = true;
                    } else {

                        toggleB = true;
                    }

                }
            } else {
                bufferB = true;
            }

            if(gamepad2.x)
            {
                if(bufferX) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferX = false;
                    if (toggleX) {

                        toggleX = false;
                        toggleA = true;
                        toggleB = true;
                        toggleY = true;
                        toggleL = true;
                        toggleR = true;
                    } else {

                        toggleX = true;
                    }

                }
            } else {
                bufferX = true;
            }

            if(gamepad2.y)
            {
                if(bufferY) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferY = false;
                    if (toggleY) {

                        toggleY = false;
                        toggleA = true;
                        toggleB = true;
                        toggleX = true;
                        toggleL = true;
                        toggleR = true;
                    } else {

                        toggleY = true;
                    }

                }
            } else {
                bufferY = true;
            }

           if(gamepad2.left_trigger >= 0.7)
            {
                if(bufferL) {
                    // This activates only once when a is pressed (not constantly when its pressed) and one time again when its pressed again and so on
                    bufferL = false;
                    if (toggleL) {

                        toggleL = false;
                        toggleA = true;
                        toggleB = true;
                        toggleX = true;
                        toggleR = true;
                    } else {

                        toggleL = true;
                    }

                }
            } else {
                bufferL = true;
            }

            if(gamepad2.right_trigger >= 0.7)
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

            if (gamepad1.dpad_up){
                leftPower = 1;
                rightPower = 1;
                backLeftPower = 1;
                backRightPower = 1;
            }
            else {
                if (gamepad1.dpad_down){
                    leftPower = -1;
                    rightPower = -1;
                    backLeftPower = -1;
                    backRightPower = -1;
                }
                else {
                    leftPower = (-gamepad1.left_stick_y + gamepad1.left_stick_x) * (-speedAdjust / 10);
                    rightPower = (-gamepad1.left_stick_y - gamepad1.left_stick_x) * (-speedAdjust / 10);
                    backLeftPower = (-gamepad1.left_stick_y - gamepad1.left_stick_x) * (-speedAdjust / 10);
                    backRightPower = (-gamepad1.left_stick_y + gamepad1.left_stick_x) * (-speedAdjust / 10);
                }
            }


            // targetDegree is currently affected by the right stick and it changes the degrees the robot goes towards
            // targetDegree += gamepad1.right_stick_x;
            // targetDegree = degreeCalc(targetDegree);

            /* if (degreeCalc(gyro.getHeading() - targetDegree) > windowSize + piecewiseWindow && degreeCalc(gyro.getHeading() - targetDegree) <= 180) {
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

            lw.setPower(leftPower);
            rw.setPower(rightPower);
            blw.setPower(backLeftPower);
            brw.setPower(backRightPower);

            //double intakeSpeed = -.75;


            /*if(gamepad1.right_bumper){
                targetDegree = 344;
            }

            if(gamepad1.left_bumper){
                targetDegree = 16;
            }

            if(gamepad1.dpad_right){
                targetDegree = 0;
            }

            if(gamepad2.right_bumper){
                shoot.setPower(-.6);
            }

            if (gamepad1.a){
                shoot.setPower(0.55);
                //} else if (gamepad1.b){
                // shoot.setPower(.6);
            } else if (gamepad1.y){
                shoot.setPower(.625);
            }else if (gamepad1.x){
                shoot.setPower(0);
            }

            if(gamepad1.b){
                flick.setPosition(0.5);
                sleep(50);
                flick.setPosition(1);
                sleep(100);
            }


            if(gamepad1.left_trigger != 0){
                for(int i = 0; i < 3; i++)
                {
                    flick.setPosition(0.5);
                    double shootPower = shoot.getPower() + .05;
                    sleep(50);
                    flick.setPosition(1);
                    sleep(400);
                    shoot.setPower(shootPower);
                }
            }

            if (gamepad1.dpad_up == true) {
                in0.setPower(intakeSpeed);
                in1.setPower(-.675);
                in2.setPower(1);
            }
            if (gamepad1.dpad_down == true) {
                in0.setPower(0);
                in1.setPower(0);
                in2.setPower(0);
            }
            if (gamepad1.dpad_left)
            {
                in0.setPower(-intakeSpeed);
                in1.setPower(.6);
                in2.setPower(-1);
            }

            if(gamepad2.a){
                fingers.setPosition(.5);
            }
            if(gamepad2.b){
                fingers.setPosition(.94);
            }
            if(gamepad2.dpad_down){
                rotate.setPosition(.9);
                arm.setPosition(0.15);
            }
            if(gamepad2.dpad_up){
                rotate.setPosition(0.2);
                arm.setPosition(1);
            }*/
        }
    }
}