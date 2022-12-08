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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: RobotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forward, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backward for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This method assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="Robot: _17RightSideAuto", group="Robot")
public class _17RightSideAuto extends LinearOpMode {

    /* Declare OpMode members. */
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

    private ElapsedTime     runtime = new ElapsedTime();

    // Calculate the COUNTS_PER_INCH for your specific drive train.
    // Go to your motor vendor website to determine your motor's COUNTS_PER_MOTOR_REV
    // For external drive gearing, set DRIVE_GEAR_REDUCTION as needed.
    // For example, use a value of 2.0 for a 12-tooth spur gear driving a 24-tooth spur gear.
    // This is gearing DOWN for less speed and more torque.
    // For gearing UP, use a gear ratio less than 1.0. Note this will affect the direction of wheel rotation.
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     COUNTS_PER_MOTOR_SERVO    = 1440 ;    // eg: Servo Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                      (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.5;

    public void autoLift(double liftHeight){
        lift1.setMode(DcMotorEx.RunMode.RUN_TO_POSITION); //this order is needed
        lift1.setPower(0.5);
        lift1.setTargetPosition((int)(liftHeight * COUNTS_PER_INCH)); 
       
       
        lift2.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        lift2.setPower(0.5);
        lift2.setTargetPosition((int)(liftHeight * COUNTS_PER_INCH));
       // claw.setPosition(0.0); 
    }

    public void groundLevel(){ //TODO: FIND A WAY TO SET POSOTION 0 ON MOTOR
        lift1.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        lift1.setPower(0.5);
        lift1.setTargetPosition(0); //go to ground level lift
        lift2.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        lift2.setPower(0.5);
        lift2.setTargetPosition(0);

    }


    @Override
    public void runOpMode() {

        // Initialize the drive system variables.
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
        lift2 = hardwareMap.get(DcMotorEx.class, "lift2");

        claw = hardwareMap.get(Servo.class, "claw");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        lift1.setDirection(DcMotor.Direction.FORWARD);
        lift2.setDirection(DcMotor.Direction.FORWARD);
        claw.setDirection(Servo.Direction.FORWARD);

        // leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Send telemetry message to indicate successful Encoder reset
        // telemetry.addData("Starting at",  "%7d :%7d",
        //                   leftDrive.getCurrentPosition(),
        //                   rightDrive.getCurrentPosition());

        telemetry.addData("Front Left Position: %7d", frontLeft.getCurrentPosition());
        telemetry.addData("Back Left Position: %7d", backLeft.getCurrentPosition());
        telemetry.addData("Front Right Position: %7d", frontRight.getCurrentPosition());
        telemetry.addData("Back Right Position: %7d", backRight.getCurrentPosition());

        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        // encoderDrive(DRIVE_SPEED,  48,  48, 5.0);  // S1: Forward 47 Inches with 5 Sec timeout
        // encoderDrive(TURN_SPEED,   12, -12, 4.0);  // S2: Turn Right 12 Inches with 4 Sec timeout
        // encoderDrive(DRIVE_SPEED, -24, -24, 4.0);  // S3: Reverse 24 Inches with 4 Sec timeout

        claw.setPosition(0.1);

        //TODO: FIND TEH EXACT PLACEMENT IN INCHES THE ROBOT WILL BE PLACED


        //length 17 in
        //width 17 in
        //radius 9 inch
        //7.07 in for 45 degrees
        //35.35 in for 135 degrees
        //24 inch for one square
        //4.4 seconds for full revolution of 15 team, assume heavier
        //3.5 from square for center

        //2 inches ahead of the robot when holding cone


        //around 33 inches the circumfrence
        //14 inch end of wheel per wheel
        //10 inch beginning of wheel to wheel
        //width, metal to metal 13
        // width inner thing per inner thing 9
        //3.5 inches behind the robot to the edge of it for the claw thing when stationary
        

        //high level cone 
        encoderDrive(DRIVE_SPEED, 3.5, 3.5, 3.5, 3.5, 1.0); //drive up a little
        encoderDrive(DRIVE_SPEED, -24, 24, 24, -24, 4.0); //drive to the left inner close to substation
        encoderDrive(DRIVE_SPEED, 48, 48, 48, 48, 8.0); //drive up to the large height
        encoderDrive(TURN_SPEED, 7.07, 7.07, -7.07, -7.07, 1.0); //turn 45 degrees toward large thing
        autoLift(LIFT_HIGH);  
        encoderDrive(DRIVE_SPEED, 4.1, 4.1, 4.1, 4.1, 1.0); //drives twoard the high level
        claw.setPosition(0.0); //let go of the cone

        encoderDrive(DRIVE_SPEED,-4.1, -4.1, -4.1, -4.1, 1.0); //move backwards
        autoLift(0.0); //preparing to lift toward cone height

        encoderDrive(TURN_SPEED, 7.07, 7.07, -7.07, -7.07, 1.0); //move 45 degrees again toward the right direction
        encoderDrive(DRIVE_SPEED, 49.5, 49.5, 49.5, 49.5, 8.0); //
        autoLift(0.0); //TODO: FIND POSITION USING STATS, lower into cone stack
        claw.setPosition(0.1); //grabs cone


        //medium level cone
        autoLift(LIFT_MEDIUM);// back to medium height
        encoderDrive(DRIVE_SPEED, -25.5, -25.5, -25.5, -25.5, 4.0); //goes toward medium height thing
        encoderDrive(TURN_SPEED, 35.35, 35.35, -35.35, -35.35, 3.0); //rotate 135 degrees
        encoderDrive(DRIVE_SPEED, 3.5, 3.5, 3.5, 3.5, 1.0); //move toward thing
        claw.setPosition(0.0); //drop

        encoderDrive(DRIVE_SPEED, -3.5, -3.5, -3.5, -3.5, 1.0); //move back
        autoLift(0.0); //cone height
        encoderDrive(TURN_SPEED, -35.35, -35.35, 35.35, 35.35, 3.0); //turn 135 degrees tworad the cone stack
        encoderDrive(DRIVE_SPEED, 25.5, 25.5, 25.5, 25.5, 4.0); //going to cone stack

        //low level cone
        autoLift(0.0); //lower to cone stack
        claw.setPosition(0.1);
        autoLift(LIFT_LOW)); //lift cone
        encoderDrive(DRIVE_SPEED, -49.5, -49.5, -49.5, -49.5, 8.0); //go back to thing
        encoderDrive(TURN_SPEED, -7.07, -7.07, 7.07, 7.07, 1.0); //rotate 45 degrees left
        encoderDrive(DRIVE_SPEED, -48, -48, -48, -48, 8.0); //go back to near starting thing
        encoderDrive(DRIVE_SPEED, 12, -12, 12, -12, 3.0); //go to low cone position

        encoderDrive(DRIVE_SPEED, 3.5, 3.5, 3.5, 3.5, 1.0); //move toward low cone position
        claw.setPosition(0.0);//drop
        encoderDrive(DRIVE_SPEED, -3.5, -3.5, -3.5, -3.5, 1.0); //move backwards
        encoderDrive(DRIVE_SPEED, 24, -24, 24, -24, 4.0); //parking back into the terminal
        autoLift(0.0);
        claw.setPosition(0.0);

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);  // pause to display final telemetry message.
    }

    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDrive(double speed,
                             double frontLeftInches, double backLeftInches, double frontRightInches,
                             double backRightInches,
                             double timeoutS) {
        int newFrontLeftTarget;
        int newBackLeftTarget;
        int newFrontRightTarget;
        int newBackRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newFrontLeftTarget = frontLeft.getCurrentPosition() + (int)(frontLeftInches * COUNTS_PER_INCH);
            newBackLeftTarget = backLeft.getCurrentPosition() + (int)(backLeftInches * COUNTS_PER_INCH);
            newFrontRightTarget = frontRight.getCurrentPosition() + (int)(frontRightInches * COUNTS_PER_INCH);
            newBackRightTarget = backRight.getCurrentPosition() + (int)(backRightInches * COUNTS_PER_INCH);
            frontLeft.setTargetPosition(newFrontLeftTarget);
            backLeft.setTargetPosition(newBackLeftTarget);
            frontRight.setTargetPosition(newFrontRightTarget);
            backRight.setTargetPosition(newBackRightTarget);

            // Turn On RUN_TO_POSITION
            frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            frontLeft.setPower(Math.abs(speed));
            backLeft.setPower(Math.abs(speed));
            frontRight.setPower(Math.abs(speed));
            backRight.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                   (runtime.seconds() < timeoutS) &&
                   (frontLeft.isBusy() && backLeft.isBusy() && frontRight.isBusy() && backRight.isBusy())) { 

                // Display it for the driver.
                telemetry.addData("Running to",  " %7d :%7d :%7d :%7d", newFrontLeftTarget,  newBackLeftTarget, newFrontRightTarget, newFrontLeftTarget);
                telemetry.addData("Currently at",  " at %7d :%7d :%7d :%7d",
                                            frontLeft.getCurrentPosition(), backLeft.getCurrentPosition(), frontRight.getCurrentPosition(), frontLeft.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            frontLeft.setPower(0);
            backLeft.setPower(0);
            frontRight.setPower(0);
            backRight.setPower(0);

            // Turn off RUN_TO_POSITION
            frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);   // optional pause after each move.
        }
    }
}
