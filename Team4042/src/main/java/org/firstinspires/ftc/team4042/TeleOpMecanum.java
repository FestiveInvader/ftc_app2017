package org.firstinspires.ftc.team4042;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.ArrayList;

@TeleOp(name = "Mecanum", group = "Iterative Opmode")
public class TeleOpMecanum extends OpMode {

    private boolean aPushed = false;

    private double adjustedSpeed;

    private boolean aLeftBumper = false;
    private boolean aRightBumper = false;

    private boolean aUp = false;
    private boolean aDown = false;
    private boolean aLeft = false;
    private boolean aRight = false;

    private boolean aA = false;

    //Declare OpMode members.
    private MecanumDrive drive = new MecanumDrive(true);

    //private UltrasonicI2cRangeSensor sensor;
    private ArrayList<Integer> rangeData;

    private GlyphPlacementSystem glyph;

    /**
    GAMEPAD 1:
      Joystick 1 - movement
      Joystick 2 - rotation
      Bumpers - speed modes
      Triggers -
      Dpad -
      A - toggle verbose
      B -
      X -
      Y -

    GAMEPAD 2:
      Joystick 1 -
      Joystick 2 -
      Bumpers - run intakes backwards
      Triggers - run intakes forwards
      Dpad - placer
      A - place glyph
      B -
      X -
      Y -
     */

    @Override
    public void init() {
        /*try {
            sensor = hardwareMap.get(I2cRangeSensor.class, "MB1242-0");
        }catch (Exception x){
            telemetry.addLine("it broke");
        }
        sensor.startRanging();
        */
        drive.initialize(telemetry, hardwareMap);
        telemetry.update();

        adjustedSpeed = MecanumDrive.FULL_SPEED;
        glyph = new GlyphPlacementSystem();
    }
    
    @Override
    public void loop() {

        //rangeData = sensor.getRange();
        //telemetry.addData("range", rangeData.get(2));

        if (gamepad1.a && !aPushed) {
            drive.toggleVerbose();
        }
        aPushed = gamepad1.a;
        drive.drive(false, gamepad1, gamepad2, adjustedSpeed * MecanumDrive.FULL_SPEED);

        if (Drive.useGyro) {
            drive.useGyro();
        }
        telemetry.update();

        //If you push the left bumper, dials the speed down
        if (gamepad1.left_bumper && !aLeftBumper && (adjustedSpeed - 0.25) >= 0) {
            adjustedSpeed -= 0.25;
        }
        aLeftBumper = gamepad1.left_bumper;

        //Right bumper - dial speed up
        if (gamepad1.right_bumper && !aRightBumper && (adjustedSpeed + 0.25) <= MecanumDrive.FULL_SPEED)
        {
            adjustedSpeed += 0.25;
        }
        aRightBumper = gamepad1.right_bumper;

        //Glyph placement
        if (gamepad1.dpad_up && !aUp) { glyph.up(); }
        aUp = gamepad1.dpad_up;
        if (gamepad1.dpad_down && !aDown) { glyph.down(); }
        aDown = gamepad1.dpad_down;
        if (gamepad1.dpad_left && !aLeft) { glyph.left(); }
        aLeft = gamepad1.dpad_left;
        if (gamepad1.dpad_right && !aRight) { glyph.right(); }
        aRight = gamepad1.dpad_right;

        if (gamepad1.a) { glyph.place(); }
        aA = gamepad1.a;

        double bRightTrigger = drive.deadZone(gamepad2.right_trigger);
        if (bRightTrigger > 0) {
            //TODO: RUN RIGHT INTAKE FORWARD
        }
        double bLeftTrigger = drive.deadZone(gamepad2.left_trigger);
        if (bLeftTrigger > 0) {
            //TODO: RUN LEFT INTAKE FORWARD
        }
        if (gamepad2.right_bumper) {
            //TODO: RUN RIGHT INTAKE BACKWARD
        }
        if (gamepad2.left_bumper) {
            //TODO: RUN LEFT INTAKE BACKWARD
        }

        telemetryUpdate();
    }

    private void telemetryUpdate() {
        telemetry.addData("Speed mode", adjustedSpeed);
        telemetry.addData("Glyph", glyph.getPositionAsString());
    }

    /* CODE FROM HERE DOWN IS AN ATTEMPT TO IMPLEMENT DYLAN'S DRIVE ALGORITHM
    MecanumDrive drive;

    @Override
    public void init() {
        drive = new MecanumDrive(hardwareMap, telemetry);
    }

    @Override
    public void loop() {
        drive.drive(false, gamepad1, 1);
    }*/

}