/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.robotcontroller.external.samples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="Basic: Mecanum Drive", group="Linear Opmode")

public class Mecanum_BasicOpMode_Linear extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeft= null;
    private DcMotor frontRight = null;
    private DcMotor backLeft = null;
    private DcMotor backRight = null;
    public static final double NEW_P = 2.5;
    public static final double NEW_I = 0.1;
    public static final double NEW_D = 0.2;
    public static final double NEW_F = 0.5;

    private DcMotorEx lift1;
    private DcMotorEx lift2;
    private Servo claw;

    private final int LIFT_LOW = 0; //TODO: find actual values
    private final int LIFT_MEDIUM = 1000; //TODO: find actual values
    private final int LIFT_HIGH = 2000; //TODO: find actual values

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
        lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
        claw = hardwareMap.get(Servo.class, "claw");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        // leftDrive.setDirection(DcMotor.Direction.REVERSE);
        // rightDrive.setDirection(DcMotor.Direction.FORWARD);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        lift1.setDirection(DcMotor.Direction.FORWARD);
        lift2.setDirection(DcMotor.Direction.FORWARD);
        claw.setDirection(Servo.Direction.FORWARD);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // variables for automatic lift control using DPAD
        boolean dpadDown = false; // button to move lift to low position    
        boolean dpadRight = false; // button to move lift to medium position
        boolean dpadUp = false; // button to move lift to high position


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            //variable for canceling auto lift
            boolean dpadLeft = gamepad1.dpad_left; // button to cancel automatic lift movement


            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);



            if (gamepad1.dpad_down && !(dpadLeft || dpadRight || dpadUp)) {
                dpadDown = true;
            } else if (gamepad1.dpad_right && !(dpadLeft || dpadDown || dpadUp)) {
                dpadRight = true;
            } else if (gamepad1.dpad_up && !(dpadLeft || dpadRight || dpadDown)) {
                dpadUp = true;
            }

  // logic for automatic lift control
            if (dpadLeft) {
                dpadDown = false;
                dpadRight = false;
                dpadUp = false;
            } else if (dpadDown) {
                lift1.setTargetPosition(LIFT_LOW);
                lift1.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift1.setPower(0.5);
                if (Math.abs(lift1.getCurrentPosition()-LIFT_LOW) < 10) {
                    dpadDown = false;
                }
                lift2.setTargetPosition(LIFT_LOW);
                lift2.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift2.setPower(0.5);
                if (Math.abs(lift2.getCurrentPosition()-LIFT_LOW) < 10) {
                    dpadDown = false;
                }
            } else if (dpadRight) {
                lift1.setTargetPosition(LIFT_MEDIUM);
                lift1.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift1.setPower(0.5);
                if (Math.abs(lift1.getCurrentPosition()-LIFT_MEDIUM) < 10) {
                    dpadRight = false;
                }
                lift2.setTargetPosition(LIFT_MEDIUM);
                lift2.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift2.setPower(0.5);
                if (Math.abs(lift2.getCurrentPosition()-LIFT_MEDIUM) < 10) {
                    dpadRight = false;
                }
            } else if (dpadUp) {
                lift1.setTargetPosition(LIFT_HIGH);
                lift1.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift1.setPower(0.5);
                if (Math.abs(lift1.getCurrentPosition()-LIFT_HIGH) < 10) {
                    dpadUp = false;
                }   
                lift2.setTargetPosition(LIFT_HIGH);
                lift2.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                lift2.setPower(0.5);
                if (Math.abs(lift2.getCurrentPosition()-LIFT_HIGH) < 10) {
                    dpadUp = false;
                }
            } else {
                lift1.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift1.setPower(0);
                lift2.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift2.setPower(0);
            }

            // variables for manual lift control
            double triggerLeft = gamepad1.left_trigger;
            double triggerRight = gamepad1.right_trigger;

            // Logic for manual lift controls (left trigger lowers, right trigger raises)
            if (triggerLeft > 0.05) {
                dpadDown = false;
                dpadRight = false;
                dpadUp = false;
                lift1.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift1.setPower(-triggerLeft);
                lift2.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift2.setPower(-triggerLeft);
            } else if (triggerRight > 0.05) {
                dpadDown = false;
                dpadRight = false;
                dpadUp = false;
                lift1.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift1.setPower(triggerRight);
                lift2.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
                lift2.setPower(triggerRight);
            } else if (lift1.getMode() == DcMotorEx.RunMode.RUN_WITHOUT_ENCODER 
            && lift2.getMode() == DcMotorEx.RunMode.RUN_WITHOUT_ENCODER) {
                lift1.setPower(0);
                lift2.setPower(0);
            }

            // variables for claw control
            boolean bumperLeft = gamepad1.left_bumper;
            boolean bumperRight = gamepad1.right_bumper;

            // Logic for claw controls (A opens, B closes)
            if (bumperLeft) {
                claw.setPosition(0.0); //TODO: find actual values
            } else if (bumperRight) {
                claw.setPosition(0.1); //TODO: find actual values
            }
            



            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("LeftMotors", "frontLeft (%.2f), backLeft (%.2f)", frontLeftPower, backLeftPower);
            telemetry.addData("RightMotors", "frontRight (%.2f), backRight (%.2f)", frontRightPower, backRightPower);
            //Expiremental adding telemtnary to figure out autonomous
            telemetry.addData("Lift Claw", "lift (%.2f), claw (%.2f)", lift1.getCurrentPosition(), claw.getPosition());
            
            
            telemetry.update();
        }
    }
}
