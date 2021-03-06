package org.firstinspires.ftc.teamcode.supers;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.vision.actualpipelines.BlackPipeline;
import org.openftc.easyopencv.OpenCvPipeline;

public class Auto {
    private final DcMotor lf, lb, rf, rb, lsweeper, rsweeper;
    public final Servo lservo, rservo, hook;
    public final TouchSensor intake;

    private final BNO055IMU imu;
    public BNO055IMU.Parameters params = new BNO055IMU.Parameters();

    private boolean isInitialized = false;

    private Telemetry telem;


    public Auto(){
        telem = Globals.telem;

        lservo = Globals.hwMap.servo.get("lservo");
        rservo = Globals.hwMap.servo.get("rservo");
        hook = Globals.hwMap.servo.get("back");

        lf = Globals.hwMap.dcMotor.get("lf");
        lb = Globals.hwMap.dcMotor.get("lb");
        rf = Globals.hwMap.dcMotor.get("rf");
        rb = Globals.hwMap.dcMotor.get("rb");

        lsweeper = Globals.hwMap.dcMotor.get("lsweep");
        rsweeper = Globals.hwMap.dcMotor.get("rsweep");

        intake = Globals.hwMap.touchSensor.get("input");

        lf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lsweeper.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rsweeper.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        lf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lsweeper.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rsweeper.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        lf.setDirection(DcMotorSimple.Direction.REVERSE);
        lb.setDirection(DcMotorSimple.Direction.REVERSE);
//        rsweeper.setDirection(DcMotorSimple.Direction.REVERSE);
        lservo.setDirection(Servo.Direction.REVERSE);

        imu = Globals.hwMap.get(BNO055IMU.class, "imu");
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        params.calibrationDataFile = "BNO055IMUCalibration.json";
    }
    public void initGyro(){
        imu.initialize(params);
        isInitialized = true;
    }

    public double getCurrentAngle(){
        if(isInitialized) {
            return imu.getAngularOrientation().firstAngle;
        }
        else throw new IllegalStateException("Robot/gyro not properly initialized");
    }


    public void drive(Direction direction, double distance, double speed) {

        int newLeftFrontTarget = 0;
        int newLeftBackTarget = 0;
        int newRightFrontTarget = 0;
        int newRightBackTarget = 0;

        double wheelDiam = 4.0;
        double ticksPerRev = 537.6;
        double inchesPerRev = wheelDiam * Math.PI;
        double ticksPerInch = ticksPerRev/inchesPerRev;

        if (direction == Direction.FORWARD) {
            distance *= ticksPerInch;
            newLeftFrontTarget = lf.getCurrentPosition() + (int) distance;
            newLeftBackTarget = lb.getCurrentPosition() + (int) distance;
            newRightFrontTarget = rf.getCurrentPosition() + (int) distance;
            newRightBackTarget = rb.getCurrentPosition() + (int) distance;
        }
        if (direction == Direction.BACK) {
            distance *= ticksPerInch;
            newLeftFrontTarget = lf.getCurrentPosition() - (int) distance;
            newLeftBackTarget = lb.getCurrentPosition() - (int) distance;
            newRightFrontTarget = rf.getCurrentPosition() - (int) distance;
            newRightBackTarget = rb.getCurrentPosition() - (int) distance;
        }
        if (direction == Direction.LEFT) {
            distance *= ticksPerInch;
            newLeftFrontTarget = lf.getCurrentPosition() - (int) distance;
            newLeftBackTarget = lb.getCurrentPosition() + (int) distance;
            newRightFrontTarget = rf.getCurrentPosition() + (int) distance;
            newRightBackTarget = rb.getCurrentPosition() - (int) distance;
        }
        if (direction == Direction.RIGHT) {
            distance *= ticksPerInch;
            newLeftFrontTarget = lf.getCurrentPosition() + (int) distance;
            newLeftBackTarget = lb.getCurrentPosition() - (int) distance;
            newRightFrontTarget = rf.getCurrentPosition() - (int) distance;
            newRightBackTarget = rb.getCurrentPosition() + (int) distance;
        }

        // Ensure that the OpMode is still active
        if (Globals.opMode.opModeIsActive()) {
            lf.setTargetPosition(newLeftFrontTarget);
            lb.setTargetPosition(newLeftBackTarget);
            rf.setTargetPosition(newRightFrontTarget);
            rb.setTargetPosition(newRightBackTarget);

            // Turn On RUN_TO_POSITION
            lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Reset timer and begin to run the motors
            if(direction == Direction.LEFT || direction == Direction.RIGHT){
                lf.setPower(Math.abs(speed));
                lb.setPower(Math.abs(speed));
                rf.setPower(Math.abs(speed));
                rb.setPower(Math.abs(speed));
            }
            else {
                lf.setPower(Math.abs(speed));
                rf.setPower(Math.abs(speed));
                lb.setPower(Math.abs(speed));
                rb.setPower(Math.abs(speed));
            }

            // Keep looping until the motor is at the desired position that was inputted
            while (Globals.opMode.opModeIsActive() &&
                    (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy())) {

                // Display current status of motor paths
                telem.addData("Path1", "Running to %7d :%7d :%7d :%7d", newLeftFrontTarget, newLeftBackTarget, newRightFrontTarget, newRightBackTarget);
                telem.addData("Path2", "Running at %7d :%7d :%7d :%7d", lf.getCurrentPosition(), lb.getCurrentPosition(), rf.getCurrentPosition(), rb.getCurrentPosition());
                telem.addData("right back", rb.getPower());
                telem.addData("right front", rf.getPower());
                telem.addData("left back", lb.getPower());
                telem.addData("left front", lf.getPower());
                telem.update();
            }

            // Stop all motion
            if(direction == Direction.LEFT || direction == Direction.RIGHT) {
                lf.setPower(0);
                lb.setPower(0);
                rf.setPower(0);
                rb.setPower(0);
            }
            else {
                lf.setPower(0);
                rf.setPower(0);
                lb.setPower(0);
                rb.setPower(0);
            }

            resetEncoders();

        }
    }

    public void autoIntake(){
        int startPos;
        int distanceTraveled = 0;

        lsweeper.setPower(1.0);
        rsweeper.setPower(1.0);

        ElapsedTime intakeTimer = new ElapsedTime();

        startPos = (lf.getCurrentPosition() + rf.getCurrentPosition() + lb.getCurrentPosition() + rb.getCurrentPosition()) / 4;

        lf.setPower(0.2);
        rf.setPower(0.2);
        lb.setPower(0.2);
        rb.setPower(0.2);

        intakeTimer.reset();

        while(intakeTimer.seconds() < 7 && !intake.isPressed()){

        }

        lf.setPower(0);
        lb.setPower(0);
        rf.setPower(0);
        rb.setPower(0);

        distanceTraveled = ((lf.getCurrentPosition() + rf.getCurrentPosition() + lb.getCurrentPosition() + rb.getCurrentPosition()) / 4) - startPos;

        int distanceInches = (int) (distanceTraveled * ((4.0 * Math.PI) / 537.6));

        drive(Direction.BACK, distanceInches, 0.5);

//        lsweeper.setPower(0);
//        rsweeper.setPower(0);
    }

    public void output(){
        ElapsedTime outputTimer = new ElapsedTime();

        lsweeper.setPower(-1.0);
        rsweeper.setPower(-1.0);

        while(outputTimer.seconds() < 1){

        }

        lsweeper.setPower(0);
        rsweeper.setPower(0);

    }

    public void resetEncoders(){
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        lf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

}
