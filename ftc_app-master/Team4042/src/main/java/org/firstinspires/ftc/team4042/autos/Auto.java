package org.firstinspires.ftc.team4042.autos;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.team4042.drive.Cryptobox;
import org.firstinspires.ftc.team4042.drive.Direction;
import org.firstinspires.ftc.team4042.drive.Drive;
import org.firstinspires.ftc.team4042.drive.GlyphPlacementSystem;
import org.firstinspires.ftc.team4042.drive.MecanumDrive;
import org.firstinspires.ftc.team4042.sensor.AnalogSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.lasarobotics.vision.opmode.LinearVisionOpMode;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Parses a file to figure out which instructions to run. CAN NOT ACTUALLY RUN INSTRUCTIONS.
 */
@Autonomous(name="Abstract Auto", group="autos")
public abstract class Auto extends LinearVisionOpMode {

    MecanumDrive drive = new MecanumDrive(true);
    private VuMarkIdentifier vuMarkIdentifier = new VuMarkIdentifier();
    private RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.UNKNOWN;

    private double PROPORTIONAL_ROTATE = C.get().getDouble("PropRot");
    private double DERIV_ROTATE = C.get().getDouble("DerivRot");

    private Telemetry.Log log;

    File file;

    private ArrayList<AutoInstruction> instructions = new ArrayList<>();

    private double startRoll;
    private double startPitch;
    private ElapsedTime intakeTimer = new ElapsedTime();
    private int intakeCount = 0;

    private boolean readMark = false;

    public void setUp(MecanumDrive drive, String filePath) {
        this.drive = drive;

        log = telemetry.log();

        drive.initialize(telemetry, hardwareMap);

        drive.targetY = GlyphPlacementSystem.Position.TOP;
        drive.targetX = GlyphPlacementSystem.HorizPos.LEFT;
        drive.stage = GlyphPlacementSystem.Stage.HOME;
        drive.glyph.setHomeTarget();
        drive.cryptobox.clear();
        drive.cryptobox.writeFile();

        //drive.glyph = new GlyphPlacementSystem(1, 0, hardwareMap, drive, false);

        //drive.setUseGyro(true);
        //telemetry.addData("glyph", drive.glyph.getTargetPositionAsString());

        vuMarkIdentifier.initialize(telemetry, hardwareMap);

        log.add("Reading file " + filePath);
        file = new File("./storage/emulated/0/bluetooth/" + filePath);

        loadFile();

        /*this.setCamera(Cameras.PRIMARY);
        this.setFrameSize(new Size(900, 900));
        //enableExtension(Extensions.BEACON);
        enableExtension(Extensions.ROTATION);
        enableExtension(Extensions.CAMERA_CONTROL);
        rotation.setIsUsingSecondaryCamera(false);
        rotation.disableAutoRotate();
        rotation.setActivityOrientationFixed(ScreenOrientation.LANDSCAPE);
        cameraControl.setColorTemperature(CameraControlExtension.ColorTemperature.AUTO);
        cameraControl.setAutoExposureCompensation();*/
    }

    /**
     * Reads from the file and puts the information into instructions
     */
    private void loadFile() {
        if (file == null) { return; } //Can't load a null file

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //Reads the lines from the file in order
                if (line.length() > 0) {
                    if (line.charAt(0) != '#') { //Use a # for a comment
                        HashMap<String, String> parameters = new HashMap<>();

                        //x:3 --> k = x, v = 3
                        String[] inputParameters = line.split(" ");
                        StringBuilder para = new StringBuilder("Parameter: ");
                        int i = 0;
                        while (i < inputParameters.length) {
                            String parameter = inputParameters[i];
                            String[] kv = parameter.split(":", 2);
                            parameters.put(kv[0],  kv[1]);
                            //Gets the next parameter and adds it to the list
                            para.append(kv[0]).append(":").append(kv[1]).append(" ");
                            i++;
                        }

                        log.add(para.toString());

                        //Stores those values as an instruction
                        AutoInstruction instruction = new AutoInstruction(parameters);
                        instructions.add(instruction);

                        telemetry.update();
                    }
                }
            }
            fileReader.close();
        } catch (Exception ex) {
            telemetry.addData("Error", "trying to load file");
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            telemetry.addData("error", sw.toString());
        }
    }

    /**
     * Tries to initialize the gyro until it works
     */
    public void gyro() {
        drive.initializeGyro(telemetry, hardwareMap);

        do {
            drive.gyro.updateAngles();
            startRoll = drive.gyro.getRoll();
            startPitch = drive.gyro.getPitch();
        } while (startRoll == 0 && startPitch == 0 && opModeIsActive());
    }

    public void runAuto(boolean useSensors) {
        if (useSensors) runAuto();
        //vuMarkIdentifier.initialize(telemetry, hardwareMap);
        //telemetry.addData("vuMarkhere", "ststs");

        //vuMark = vuMarkIdentifier.getMark();
        //telemetry.addData("vuMarkhere", "after");


        while(true) {
            //Mat x = vuMarkIdentifier.getFrame();
            //Log.d("TOMMY", x.toString());

            RelicRecoveryVuMark j = vuMarkIdentifier.getMark();
            //String j = getBallColor(x);
            Log.d("TOMMY", j.toString());
        }
    }

    /**
     * Runs the list of instructions
     */
    public void runAuto() {
        //vuMarkIdentifier.initialize(telemetry, hardwareMap);

        gyro();

        try {
            drive.jewelUp();
        } catch (NullPointerException ex) { }

        drive.resetEncoders();
        drive.setEncoders(true);
        drive.setVerbose(false);

        //Reads each instruction and acts accordingly
        Iterator<AutoInstruction> instructionsIter = instructions.iterator();
        while (instructionsIter.hasNext() && opModeIsActive()) {
            AutoInstruction instruction = instructionsIter.next();
            String functionName = instruction.getFunctionName();
            HashMap<String, String> parameters = instruction.getParameters();
            log.add("function: " + functionName);
            switch (functionName) {
                case "drive":
                    autoDrive(parameters);
                    break;
                case "doff":
                    autoDriveOff(parameters);
                    break;
                case "rot":
                    autoRotate(parameters);
                    break;
                case "sdrive":
                    autoSensorDrive(parameters);
                    break;
                case "s2drive":
                    autoTwoSensorDrive(parameters);
                    break;
                case "up":
                    jewelUp(parameters);
                    break;
                case "down":
                    drive.jewelDown();
                    break;
                case "knockr":
                    knockRedJewel(parameters);
                    break;
                case "knockb":
                    knockBlueJewel(parameters);
                    break;
                case "jleft":
                    knockLeftJewel(parameters);
                    break;
                case "jright":
                    knockRightJewel(parameters);
                    break;
                case "getmark":
                    getVuMark(parameters);
                    break;
                case "place":
                    placeGlyph(parameters);
                    break;
                case "align":
                    alignHorizontally(parameters);
                    break;
                case "glyph":
                    grabGlyph(parameters);
                    break;
                case "open":
                    openIntakes(parameters);
                    break;
                case "brace":
                    drive.openWinch();
                    break;
                case "wait":
                    wait(parameters);
                    break;
                case "break":
                    drive.toggleExtendo();
                    break;
                default:
                    System.err.println("Unknown function called from file " + file);
                    break;
            }
        }

        //autoDrive(new Direction(1, .5), Drive.FULL_SPEED, 1000);

        //autoSensorDrive(Direction.Forward, Drive.FULL_SPEED / 4, 7, drive.ir);

        //check sensor sums
        //robot starts facing right
        //scan vision patter
        //go to front of jewels
        //cv scan
        //knock off other jewel
        //head right
        //whisker sensor hits cryptobox
        //back up
        //repeat ^ until whisker disengages
        //move right until we see -^-^-| from ultrasonic
        //place block
        //detach and extend robot towards glyph
    }

    public void getVuMark(HashMap<String, String> parameters) {
        readMark = true;
        vuMarkIdentifier.prepareMark();
        //vuMark = vuMarkIdentifier.getMark();
        //log.add("vuMark: " + vuMark);
    }

    /*
    public void placeGlyph(HashMap<String, String> parameters) {
        //The vumark placement system starts at (1, 0), which is the bottom of the center column
        if (vuMark.equals(RelicRecoveryVuMark.LEFT)) {
            drive.glyph.left();
        } else if (vuMark.equals(RelicRecoveryVuMark.RIGHT)) {
            drive.glyph.right();
        }

        if (!vuMark.equals(RelicRecoveryVuMark.UNKNOWN)) {
            drive.glyph.place();
        }

        telemetry.addData("glyph", drive.glyph.getTargetPositionAsString());
        telemetry.update();
    }
    */

    /**
     * Gets a required double parameter, or throws an exception if it's not found
     * @param parameters A hashmap of all the parameters
     * @param key The parameter key to look for
     * @return The value for the key
     * @throws NoSuchFieldError If the key isn't mapped
     */
    private double getParam(HashMap<String, String> parameters, String key) throws NoSuchFieldError {
        try {
            return Double.parseDouble(parameters.get(key));
        } catch (Exception ex) {
            throw new NoSuchFieldError("Could not find " + key);
        }
    }

    /**
     * Gets an optional double parameter
     * @param parameters A hashmap of all the parameters
     * @param key The parameter key to look for
     * @param defaultVal The value to return if the optional parameter is not included
     * @return The value for the key, or the default value if the parameter doesn't exist
     */
    private double getParam(HashMap<String, String> parameters, String key, double defaultVal) {
        return parameters.containsKey(key) ? Double.parseDouble(parameters.get(key)) : defaultVal;
    }

    /**
     * Gets an optional integer parameter
     * @param parameters A hashmap of all the parameters
     * @param key The parameter key to look for
     * @param defaultVal The value to return if the optional parameter is not included
     * @return The value for the key, or the default value if the parameter doesn't exist
     */
    private int getParam(HashMap<String, String> parameters, String key, int defaultVal) {
        return parameters.containsKey(key) ? Integer.parseInt(parameters.get(key)) : defaultVal;
    }

    public void wait(HashMap<String, String> parameters) {
        double seconds = getParam(parameters, "sec");

        ElapsedTime waitTimer = new ElapsedTime();
        while (waitTimer.seconds() < seconds);
    }

    public void grabGlyph(HashMap<String, String> parameters) {
        drive.readSensorsSetUp();

        while (opModeIsActive() && !drive.collectGlyphStep()) {  }
    }

    public void alignHorizontally(HashMap<String, String> parameters) {
        double prevMiddle = drive.shortIr[0].getCmAvg();
        double currMiddle;
        do {
            currMiddle = drive.shortIr[0].getCmAvg();
            if (Math.abs(prevMiddle - currMiddle) > 2) { //Moved too far left
                drive.driveXYR(.5, 1, 0, 0, false); //Move back right
            } else {
                drive.driveXYR(.5, -1, 0, 0, false); //Move left
            }
        } while (drive.shortIr[1].getCmAvg() > 15 && drive.shortIr[2].getCmAvg() > 15 && opModeIsActive());

        //When they're both non-infinite readings, stop
        drive.stopMotors();
    }

    public String getBallColor(Mat frame){
        //log.add(frame.height() + " x " + frame.width());
        //Imgproc.resize(frame, frame, new Size(960, 720));
        telemetry.update();
        //Rect left_crop = new Rect(new Point(215,585), new Point(380, 719));
        //Rect right_crop = new Rect(new Point(460,585), new Point(620, 719));
        Rect left_crop = new Rect(new Point(464,688), new Point(617, 719));
        Rect right_crop = new Rect(new Point(771,672), new Point(942, 719));

        //Log.d("A", this.getFrameSize().width + " x " + this.getFrameSize().height);
        Mat right = new Mat(frame, right_crop);
        Mat left = new Mat(frame, left_crop);


        String result = "unspecified";
        Scalar left_colors  = Core.sumElems(left);
        Scalar right_colors = Core.sumElems(right);

        if(left_colors.val[0] >= left_colors.val[2]){
            result = "red";
        } else {
            result = "blue";
        }

        if(right_colors.val[0] >= right_colors.val[2]){
            result = result.concat(", red");
        } else {
            result = result.concat(", blue");
    }

        return result;
    }

    public void placeGlyph(HashMap<String, String> parameters) {

        drive.setVerticalDriveMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        drive.setVerticalDriveMode(DcMotor.RunMode.RUN_TO_POSITION);
        if (vuMark == RelicRecoveryVuMark.UNKNOWN){
            vuMark = RelicRecoveryVuMark.CENTER;
        }

        int column;
        switch (vuMark) {
            case RIGHT:
                column = 0;
                break;
            case CENTER:
                column = 1;
                break;
            case LEFT:
                column = 2;
                break;
            default:
                column = 1;
                break;
        }

        Cryptobox.GlyphColor newGlyph = drive.getGlyphColor();

        drive.cryptobox.cipherFirstGlyph(newGlyph, column);

        //Place the first glyph to match the vumark
        drive.cryptobox.driveGlyphPlacer(newGlyph, 0, column);

        drive.stage = GlyphPlacementSystem.Stage.HOME;

        boolean done;
        do {
            drive.uTrackUpdate();
            if (!drive.stage.equals(GlyphPlacementSystem.Stage.RETURN2) && !drive.stage.equals(GlyphPlacementSystem.Stage.RESET)) {
                drive.glyph.runToPosition(0);
            }
            done = drive.uTrack();
        } while (opModeIsActive() && !done);
    }

    public void jewelUp(HashMap<String, String> parameters) {
        drive.jewelUp();
    }

    public void knockLeftJewel(HashMap<String, String> parameters) {
        jewelLeft();
    }

    public void knockRightJewel(HashMap<String, String> parameters) {
        jewelRight();
    }

    public void knockRedJewel(HashMap<String, String> parameters) {
        try {
            //String balls = getBallColor(vuMarkIdentifier.getFrameAsMat());
            //String balls = getBallColor(getFrameRgba());
            String balls = getBallColor(vuMarkIdentifier.getFrame());
            //String balls = getBallColor(vuMarkIdentifier.getJewel());
            //String balls = "red, blue";
            telemetry.addData("ball orientation", balls);
            switch (balls) {
                case "red":
                    jewelLeft();
                    break;
                case "blue":
                    jewelRight();
                    break;
                case "red, blue":
                    jewelLeft();
                    break;
                case "blue, red":
                    jewelRight();
                    break;
                case ", blue":
                    jewelLeft();
                    break;
                case ", red":
                    jewelRight();
                    break;
            }
        } catch (CvException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            telemetry.addData("CvException", sw.toString());
        }
    }

    public void knockBlueJewel(HashMap<String, String> parameters) {
        log.add("blue jewel");
        //Mat mat = getFrameRgba();
        //String balls = getBallColor(getFrameRgba());
        String balls = getBallColor(vuMarkIdentifier.getFrame());
        //String balls = "red, blue";
        log.add("ball orientation: " + balls);
        switch (balls) {
            case "red":
                jewelRight();
                break;
            case "blue":
                jewelLeft();
                break;
            case "red, blue":
                jewelRight();
                break;
            case "blue, red":
                jewelLeft();
                break;
            case ", blue":
                jewelRight();
                break;
            case ", red":
                jewelLeft();
                break;
        }
    }

    public void autoDrive(HashMap<String, String> parameters) {
        Direction direction = new Direction(getParam(parameters, "x"), -getParam(parameters, "y"));
        double speed = getParam(parameters, "speed");
        double targetTicks = getParam(parameters, "target");
        double time = getParam(parameters, "time", -1);
        boolean useGyro = parameters.containsKey("gyro");
        double targetGyro = useGyro ? Double.parseDouble(parameters.get("gyro")) : 0;

        autoDrive(direction, speed, targetTicks, time, useGyro, targetGyro);
    }

    /**
     * Drives in the given Direction at the given speed until targetTicks is reached
     * @param direction The direction to head in
     * @param speed The speed to move at
     * @param targetTicks The final distance to have travelled, in encoder ticks
     */
    private void autoDrive(Direction direction, double speed, double targetTicks, double time, boolean useGyro, double targetGyro) {
        //log.add("autoDrive invoked with direction " + direction + " speed " + speed + " targetTicks " + targetTicks);
        boolean done = false;
        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        while (opModeIsActive() && !done && (timer.seconds() <= time || time <= 0)) {
            //Keep going if (you're not done and the seconds are less than the target) or (you're not waiting for the timer and you're not done)
            done = drive.driveWithEncoders(direction, speed, targetTicks, useGyro, targetGyro, 1);
            //telemetry.addData("targetTime", time);
            //telemetry.addData("time", timer.seconds());
            //telemetry.addData("DONE", done);
            //telemetry.update();
            //telemetry.update();
        }
        drive.resetEncoders();
        drive.runWithEncoders();
    }

    private void autoDriveOff(HashMap<String, String> parameters) {
        Direction direction = new Direction(getParam(parameters, "x"),
                -getParam(parameters, "y"));
        double speed = getParam(parameters, "speed");
        double gyro = getParam(parameters, "gyro");
        int sonarId = parameters.containsKey("sonar") ? Integer.parseInt(parameters.get("sonar")) : -1;
        double dist = getParam(parameters, "dist", 0);

        //Drive in the direction indicated
        if (sonarId >= 0) {
            AnalogSensor xSonar = drive.sonar[sonarId];

            boolean done;
            for (int i = 0; i < AnalogSensor.NUM_OF_READINGS && opModeIsActive(); i++) {
                xSonar.addReading();
            }

            do {
                drive.updateRates();

                //Get the distances and derivative terms
                xSonar.addReading();

                //Proportional/derivative controller
                double xFactor = getSensorFactor(AnalogSensor.Type.SONAR, sonarId, xSonar.getCmAvg(), dist);
                Direction newDir = new Direction(Range.clip(direction.getX() + xFactor, -1, 1), direction.getY());

                done = drive.driveWithEncoders(newDir, speed, 750, true, gyro, 1);
            } while (opModeIsActive() && !done);

            drive.resetEncoders();
            drive.runWithEncoders();
        } else {
            autoDrive(direction, speed, 750, -1, true, gyro);
        }

        double roll;
        double pitch;

        do {
            drive.gyro.updateAngles();
            roll = drive.gyro.getRoll();
            pitch = drive.gyro.getPitch();
            log.add("roll: " + roll + " pitch: " + pitch);
            //log.add("" + opModeIsActive());
            autoDrive(direction, speed, 100, -1, true, gyro);
        }
        while ((Math.abs(roll - startRoll) >= 3) ||
                (Math.abs(pitch - startPitch) >= 3) && opModeIsActive());
            //If you're too tipped forward/backwards or left/right, then keep driving
            //This effectively drives until you're off the balancing stone

        drive.resetEncoders();
        drive.runWithEncoders();
    }

    public void autoRotate(HashMap<String, String> parameters) {
        double realR = getParam(parameters, "r");

        double speed = getParam(parameters, "speed");

        autoRotate(realR, speed);

        drive.resetEncoders();
        drive.runWithEncoders();
    }

    /**
     * Drives in the given Rotation at the given speed until targetTicks is reached
     * @param realR The degree to rotate to
     * @param speed The speed to rotate at
     */
    private void autoRotate(double realR, double speed) {
        log.add("Got to rotate:");
        log.add("" + opModeIsActive());
        double gyro;
        do {
            gyro = drive.gyro.updateHeading();
            double diff = realR - gyro;
            telemetry.addData("gyro",gyro + " realR: " + realR + " diff: " + diff);
            if (diff > 270) { diff -= 360; }
            if (diff < -270) { diff += 360; }
            if (Math.abs(diff) > 2) {
                drive.driveXYR(speed, 0, 0, DERIV_ROTATE * diff, false, PROPORTIONAL_ROTATE);
            }
            log.add("" + opModeIsActive());
        } while (Math.abs(gyro - realR) > 2 && opModeIsActive());

        drive.stopMotors();
        drive.resetEncoders();
        drive.runWithEncoders();
        telemetry.update();
    }

    public void autoTwoSensorDrive(HashMap<String, String> parameters) {
        double speed = Double.parseDouble(parameters.get("speed"));

        double yTargetDistance = Double.parseDouble(parameters.get("ydistance"));
        int yIr = Integer.parseInt(parameters.get("yir"));
        boolean yLongIr = Boolean.parseBoolean(parameters.get("ylong"));
        double targetGyro = Double.parseDouble(parameters.get("gyro"));

        log.add("" + parameters.containsKey("xdistance"));
        if (parameters.containsKey("xdistance")) {
            double xTargetDistance = Double.parseDouble(parameters.get("xdistance"));
            int xIr = Integer.parseInt(parameters.get("xir"));
            boolean xLongIr = Boolean.parseBoolean(parameters.get("xlong"));
            autoTwoSensorDrive(speed, xTargetDistance, xIr, xLongIr, true, yTargetDistance, yIr, yLongIr, targetGyro);
        }
        else {
            autoTwoSensorDrive(speed, 0, 0, false, false, yTargetDistance, yIr, yLongIr, targetGyro);
        }
    }

    /**
     * Drives until two sensors returns a target value, one for the x positioning and one for the y
     * @param speed The speed to move at
     * @param xTargetDistance The final distance for the x sensor to return
     * @param xIrId The sensor to read an x distance from
     * @param xIsLongRange Whether the x sensor is long-range or not
     * @param yTargetDistance The final distance for the y sensor to return
     * @param yIrId The sensor to read an y distance from
     * @param yIsLongRange Whether the y sensor is long-range or not
     */
    //35, 33 diagonal
    private void autoTwoSensorDrive(double speed, double xTargetDistance, int xIrId, boolean xIsLongRange, boolean useX,
                                    double yTargetDistance, int yIrId, boolean yIsLongRange, double targetGyro) {

        //autoDrive(direction, speed, targetTicks, -1, false, targetGyro);

        AnalogSensor xIr = xIsLongRange ? drive.longIr[xIrId] : drive.shortIr[xIrId];
        AnalogSensor yIr = yIsLongRange ? drive.longIr[yIrId] : drive.shortIr[yIrId];

        telemetry.addData("xIr", xIr + " yIr " + yIr);

        double xCurrDistance;
        double yCurrDistance;
        int i = 0;
        while (i < AnalogSensor.NUM_OF_READINGS && opModeIsActive()) {
            //read the IRs just to set them up
            xIr.addReading();
            yIr.addReading();
            telemetry.addData("xIr cm", xIr.getCmAvg());
            telemetry.addData("yIr cm", yIr.getCmAvg());
            telemetry.update();
            i++;
        }

        ElapsedTime timeout = new ElapsedTime();
        timeout.reset();

        do {
            double speedFactor = speed;

            drive.updateRates();

            double r = drive.useGyro(targetGyro) * .75 + 5 * drive.gyroRate;
            r = r < .05 && r > 0 ? 0 : r;
            r = r > -.05 && r < 0 ? 0 : r;

            //Get the distances and derivative terms
            xCurrDistance = xIr.getCmAvg();
            yCurrDistance = yIr.getCmAvg();
            double xDerivValue = xIsLongRange ? drive.longIrRates[xIrId] : drive.shortIrRates[xIrId];
            double yDerivValue = yIsLongRange ? drive.longIrRates[yIrId] : drive.shortIrRates[yIrId];

            //Set up the derivative and proportional terms
            double xDeriv = xDerivValue * -10;
            double xProportional = (xCurrDistance - xTargetDistance) * .025;

            double yDeriv = yDerivValue * -10;
            double yProportional = (yCurrDistance - yTargetDistance) * .025;

            //Apply the controller
            double xFactor = (xProportional - xDeriv);
            double yFactor = (yProportional - yDeriv);
            telemetry.addData("xIr cm", xCurrDistance);
            telemetry.addData("yIr cm", yCurrDistance);
            telemetry.addData("x", xFactor);
            telemetry.addData("y", yFactor);
            telemetry.addData("r", r);
            telemetry.update();

            xFactor = Range.clip(xFactor, -1, 1);
            yFactor = Range.clip(yFactor, -1, 1);
            r = Range.clip(r, -1, 1);

            //Actually drives
            if (useX) {
                //drive.driveXYR(speedFactor, xFactor/2, -yFactor/2, r, false);
                drive.runWithoutEncoders();
                drive.driveXYR(1, xFactor * 4.5, -yFactor / 2, r, false);
            } else {
                //drive.driveXYR(speedFactor, 0, -yFactor/2, r, false);
                drive.driveXYR(1, 0, -yFactor / 2, r, false);
            }
        } while (((Math.abs(xTargetDistance - xCurrDistance) > 2)) && timeout.seconds() < 5 && opModeIsActive());

        //If you're off your target distance by 2 cm or less, that's good enough : exit the while loop
        drive.stopMotors();
        drive.runWithEncoders();
    }

    public void autoSensorDrive(HashMap<String, String> parameters) {
        double targetGyro = getParam(parameters, "gyro");

        boolean useX = parameters.containsKey("xdistance");
        boolean useY = parameters.containsKey("ydistance");

        double yTargetDistance = useY ? Double.parseDouble(parameters.get("ydistance")) : 0;
        int yIr = useY ? Integer.parseInt(parameters.get("yir")) : 0;

        double offset = getParam(parameters, "offset", 0);

        double speed = getParam(parameters, "speed", 1);

        AnalogSensor.Type yType = AnalogSensor.Type.SHORT_RANGE;
        if (useY) {
            switch (parameters.get("ytype")) {
                case "short":
                    yType = AnalogSensor.Type.SHORT_RANGE;
                    break;
                case "long":
                    yType = AnalogSensor.Type.LONG_RANGE;
                    break;
                case "sonar":
                    yType = AnalogSensor.Type.SONAR;
                    break;
                default:
                    yType = AnalogSensor.Type.SHORT_RANGE;
                    break;
            }
        }

        log.add("" + parameters.containsKey("offset"));

        double xTargetDistance = useX ? Double.parseDouble(parameters.get("xdistance")) : 0;
        int xIr = useX ? Integer.parseInt(parameters.get("xir")) : 0;

        AnalogSensor.Type xType = AnalogSensor.Type.SHORT_RANGE;
        if (useX) {
            switch (parameters.get("xtype")) {
                case "short":
                    xType = AnalogSensor.Type.SHORT_RANGE;
                    break;
                case "long":
                    xType = AnalogSensor.Type.LONG_RANGE;
                    break;
                case "sonar":
                    xType = AnalogSensor.Type.SONAR;
                    break;
                default:
                    xType = AnalogSensor.Type.SHORT_RANGE;
                    break;
            }
        }

        boolean testMode = parameters.containsKey("test");

        double time = getParam(parameters, "time", 2.5);

        double prop = getParam(parameters, "prop");

        boolean remove = parameters.containsKey("remove") && Boolean.parseBoolean(parameters.get("remove"));

        autoSensorDrive(xTargetDistance, xIr, xType, useX, yTargetDistance, yIr, yType, useY, targetGyro, offset, speed, testMode, time, prop, remove);
    }

    private boolean bang = false;

    /**
     * Drives until two sensors returns a target value, one for the x positioning and one for the y
     * @param xTargetDistance The final distance for the x sensor to return
     * @param xIrId The sensor to read an x distance from
     * @param xType Whether the x sensor is long-range or not
     * @param yTargetDistance The final distance for the y sensor to return
     * @param yIrId The sensor to read an y distance from
     * @param yType Whether the y sensor is long-range or not
     */
    private void autoSensorDrive(double xTargetDistance, int xIrId, AnalogSensor.Type xType, boolean useX,
                                 double yTargetDistance, int yIrId, AnalogSensor.Type yType, boolean useY,
                                 double targetGyro, double offset, double speed, boolean testMode, double time, double prop, boolean remove) {
        if (useX) {
            AnalogSensor xIr = null;
            switch (xType) {
                case SHORT_RANGE:
                    xIr = drive.shortIr[xIrId];
                    break;
                case LONG_RANGE:
                    xIr = drive.longIr[xIrId];
                    break;
                case SONAR:
                    xIr = drive.sonar[xIrId];
                    break;
            }

            double xCurrDistance;
            int i = 0;
            while (i < AnalogSensor.NUM_OF_READINGS && opModeIsActive()) {
                //read the IRs just to set them up
                xIr.addReading();
                i++;
            }

            ElapsedTime timeout = new ElapsedTime();
            timeout.reset();

            bangBangTimer.reset();

            do {
                xIr.addReading();

                drive.updateRates(offset);
                double r = getSensorR(targetGyro);

                double wallAdjust = false ? 0 : .75;

                drive.driveXYR(1, 0, wallAdjust, r * 3 / 2, false);

                if (bangBangTimer.seconds() > C.get().getDouble("BangTimer")) {
                    //Get the distances and derivative terms
                    xIr.addReading(remove);

                    xCurrDistance = xIr.getCmAvg(100, offset);

                    double xPower = (xCurrDistance - xTargetDistance) / Math.abs(xCurrDistance - xTargetDistance) * -speed;

                    log.add("xPower: " + xPower);
                    log.add("diff: " + (xCurrDistance - xTargetDistance));
                    drive.resetEncoders();
                    drive.runWithEncoders();

                    boolean done = false;
                    while (opModeIsActive() && !done) {
                        Direction dir = new Direction(xPower, wallAdjust);

                        double targetTicks = Math.abs(xCurrDistance - xTargetDistance) > 6 ? 300 : Math.abs(xCurrDistance - xTargetDistance) * prop;
                        log.add(xCurrDistance + "");

                        if (Math.abs(xCurrDistance - xTargetDistance) < 0.1) { break; }

                        done = drive.driveWithEncoders(dir, 1, targetTicks, true, targetGyro, C.get().getDouble("mulch"));

                    }

                    drive.resetEncoders();
                    drive.runWithEncoders();
                    bangBangTimer.reset();
                }
            }
            while (opModeIsActive() && ((timeout.seconds() < C.get().getDouble("SensorTimeout") && !testMode) || (testMode)));

            //If you're off your target distance by 2 cm or less, that's good enough : exit the while loop
            drive.stopMotors();
            drive.runWithEncoders();
        }
    }

    private ElapsedTime bangBangTimer = new ElapsedTime();

    private double getSensorFactor(AnalogSensor.Type type, int irId, double currDistance, double targetDistance) {
        double derivValue = 0;
        switch (type) {
            case SHORT_RANGE:
                derivValue = drive.shortIrRates[irId];
                break;
            case LONG_RANGE:
                derivValue = drive.longIrRates[irId];
                break;
            case SONAR:
                derivValue = drive.sonarRates[irId];
                break;
        }

        //Set up the derivative and proportional terms
        double deriv = derivValue * C.get().getDouble("SensorDeriv");
        double proportional = (currDistance - targetDistance) * C.get().getDouble("SensorProp");

        telemetry.addData("derivative", deriv);
        telemetry.addData("proportional", proportional);

        //Apply the controller
        double factor = (proportional + deriv);

        //factor = Range.clip(factor, -1, 1);
        return factor;
    }
    private double getSensorR(double targetGyro) {
        double r = drive.useGyro(targetGyro) * .75 + 5 * drive.gyroRate;
        r = r < .05 && r > 0 ? 0 : r;
        r = r > -.05 && r < 0 ? 0 : r;
        r = Range.clip(r, -1, 1);
        return r;
    }

    private void autoSensorDrive(double targetDistance) {
        telemetry.addData("ir", drive.shortIr[0]);
        telemetry.update();
        autoSensorDrive(0, 0, AnalogSensor.Type.SHORT_RANGE, false, targetDistance, 0, AnalogSensor.Type.SHORT_RANGE, true, 0, 0, 1, false, 2.5, 40, false);
    }

    public void jewelLeft() {
        try {
            drive.resetEncoders();
            drive.runWithEncoders();

            //drive.intakeLeft(1);
            //drive.intakeRight(1);
            log.add("running intakes in");

            ElapsedTime timer = new ElapsedTime();

            timer.reset();
            drive.jewelDown();

            //while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {}
            //timer.reset();

            //Moves the robot left
            autoRotate(14, Drive.FULL_SPEED);

            //drive.intakeLeft(-1);

            drive.jewelUp();
            //timer.reset();
            //while (timer.seconds() < 1) {}

            //drive.intakeLeft(1);

            autoRotate(0, Drive.FULL_SPEED);

            /*for (int i = 0; i < 2; i++) {
                drive.intakeLeft(-1);
                while (timer.seconds() < C.get().getDouble("intakeBackTime")) {
                }
                timer.reset();

                drive.intakeLeft(1);
                while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {
                }
                timer.reset();
            }

            drive.intakeRight(0);
            drive.intakeLeft(0);*/

            //autoRotate(0, Drive.FULL_SPEED/4);
        } catch (NullPointerException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            telemetry.addData("NullPointerException", sw.toString());
        }
    }

    public void jewelRight() {
        try {
            drive.resetEncoders();
            drive.runWithEncoders();

            //drive.intakeLeft(1);
            //drive.intakeRight(1);

            ElapsedTime timer = new ElapsedTime();

            timer.reset();
            drive.jewelDown();

            //while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {}
            //timer.reset();

            autoRotate(-14, Drive.FULL_SPEED);

            //drive.intakeLeft(-1);

            drive.jewelUp();
            //timer.reset();
            //while (timer.seconds() < 1) {}

            //drive.intakeLeft(1);

            autoRotate(0, Drive.FULL_SPEED);

            /*for (int i = 0; i < 2; i++) {
                drive.intakeLeft(-1);
                while (timer.seconds() < C.get().getDouble("intakeBackTime")) {
                }
                timer.reset();

                drive.intakeLeft(1);
                while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {
                }
                timer.reset();
            }

            drive.intakeLeft(0);
            drive.intakeRight(0);*/

            //autoRotate(0, Drive.FULL_SPEED/4);
        } catch (NullPointerException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            telemetry.addData("NullPointerException", sw.toString());
        }
    }

    @Override
    public boolean opModeIsActive(){
        if (intakeCount > 0 && this.intakeTimer.milliseconds() / 1000 < C.get().getDouble("intakeForwardTime")){
            drive.intakeLeft(1);
            drive.intakeRight(1);
        }else if (intakeCount > 0 && this.intakeTimer.milliseconds() / 1000 < (C.get().getDouble("intakeForwardTime") + C.get().getDouble("intakeBackTime"))){
            drive.intakeLeft(-1);
            drive.intakeRight(-1);
        }else if (this.intakeTimer.milliseconds() / 1000 > (C.get().getDouble("intakeForwardTime") + C.get().getDouble("intakeBackTime"))){
            drive.intakeLeft(0);
            drive.intakeRight(0);
            if (intakeCount > 0){
                intakeCount--;
                intakeTimer.reset();
            }
        }if (readMark){
            vuMark = vuMarkIdentifier.getMarkInstant();
            if (vuMark != RelicRecoveryVuMark.UNKNOWN){
                readMark = false;
                com.vuforia.CameraDevice.getInstance().setFlashTorchMode(false);
            }
        }
        //telemetry.addData("Mark", vuMark);
        return super.opModeIsActive();
    }

    public void openIntakes(HashMap<String, String> parameters) {

        this.intakeCount = 3;
        this.intakeTimer.reset();
        /*if (this.intakeTimer.milliseconds() / 1000 < C.get().getDouble("intakeForwardTime")){
            drive.intakeLeft(1);
            drive.intakeRight(1);
        }else if (this.intakeTimer.milliseconds() / 1000 < (C.get().getDouble("intakeForwardTime") + C.get().getDouble("intakeBackTime"))){
            drive.intakeLeft(-1);
            drive.intakeRight(-1);
        }else if (this.intakeTimer.milliseconds() / 1000 > (C.get().getDouble("intakeForwardTime") + C.get().getDouble("intakeBackTime"))){
            drive.intakeLeft(0);
            drive.intakeRight(0);
            if (intakeCount > 0){
                intakeCount--;
                intakeTimer.reset();
            }
        }

        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        drive.intakeLeft(1);
        drive.intakeRight(1);
        while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {}
        timer.reset();

        for (int i = 0; i < 3; i++) {
            drive.intakeLeft(-1);
            while (timer.seconds() < C.get().getDouble("intakeBackTime")) {
            }
            timer.reset();

            drive.intakeLeft(1);
            while (timer.seconds() < C.get().getDouble("intakeForwardTime")) {
            }
            timer.reset();
        }

        drive.intakeLeft(0);
        drive.intakeRight(0);*/
    }
}
