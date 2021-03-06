package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.supers.Globals;
import org.firstinspires.ftc.teamcode.supers.Mode;
import org.firstinspires.ftc.teamcode.supers.Robot;

@TeleOp(name="TeleOp", group="driving")
public class DriveOpMode extends LinearOpMode {
    private Robot robot;

    private double speedSetting = 1.0;
    private boolean lastRBumper = false, lastLBumper = false, currentRBumper, currentLBumper, toggle = true, lastA = false, currentA, lastLButton = false, lastRButton = false, currentLButton, currentRButton;
    private double[] positions = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    private int currentIndex = 0;
    private double[] hookPos = new double[]{1, 0.45};
    private int hookIndex = 0;
    private int sweepMode = 0;

    private Telemetry telem;

    @Override
    public void runOpMode() throws InterruptedException{
        robot = new Robot(this);

        robot.init(Mode.TELEOP);

        telem = Globals.telem;

        robot.win();

        waitForStart();

        while(isStarted() && !isStopRequested()){

            // mecannum math
            double r = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y);
            double robotAngle = Math.atan2(gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.PI / 4;
            double rightX = -gamepad1.right_stick_x;

            double lf = r * Math.sin(robotAngle) * speedSetting + rightX * speedSetting;
            double lb = r * Math.cos(robotAngle) * speedSetting + rightX * speedSetting;
            double rf = r * Math.cos(robotAngle) * speedSetting - rightX * speedSetting;
            double rb = r * Math.sin(robotAngle) * speedSetting - rightX * speedSetting;

            if(lf > 0.7) lf = 1;
            if(lf < -0.7) lf = -1;
            if(lb > 0.7) lb = 1;
            if(lb < -0.7) lb = -1;
            if(rf > 0.7) rf = 1;
            if(rf < -0.7) rf = -1;
            if(rb > 0.7) rb = 1;
            if(rb < -0.7) rb = -1;

            robot.tele.lf.setPower(-lf);
            robot.tele.lb.setPower(-lb);
            robot.tele.rf.setPower(-rf);
            robot.tele.rb.setPower(-rb);

            // intake servos
            currentLBumper = gamepad1.left_bumper;
            currentRBumper = gamepad1.right_bumper;
            if(currentLBumper && !lastLBumper){
                // decrement position
                int nextPos = currentIndex - 1;
                if(nextPos == -1) nextPos = 0;
                currentIndex = nextPos;
            }
            if(currentRBumper && !lastRBumper){
                // increment position
                int nextPos = currentIndex + 1;
                if(nextPos == 6) nextPos = 5;
                currentIndex = nextPos;
            }
            robot.tele.rservo.setPosition(positions[currentIndex] + 0.05);
            robot.tele.lservo.setPosition(positions[currentIndex]);
            lastLBumper = currentLBumper;
            lastRBumper = currentRBumper;

            // hook
            currentA = gamepad1.a;
            if(currentA && !lastA){
                int nextPos = (hookIndex + 1) % 2;
                hookIndex = nextPos;
            }
            robot.tele.hook.setPosition(hookPos[hookIndex]);
            lastA = currentA;

            // sweeper
            currentLButton = gamepad1.left_stick_button;
            currentRButton = gamepad1.right_stick_button;
            if(currentLButton && !lastLButton){
                sweepMode -= 1;
                if(sweepMode <= -2) sweepMode = -1;
            }
            if(currentRButton && !lastRButton){
                sweepMode += 1;
                if(sweepMode >= 2) sweepMode = 1;
            }
            if(sweepMode == 0) {
                robot.tele.lsweeper.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
                robot.tele.rsweeper.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
            }
            else if(sweepMode ==  1){
                robot.tele.lsweeper.setPower(1);
                robot.tele.rsweeper.setPower(1);
            }
            else{
                robot.tele.lsweeper.setPower(-1);
                robot.tele.rsweeper.setPower(-1);
            }
            lastLButton = currentLButton;
            lastRButton = currentRButton;

            // telemetry
            telem.addData("Servo pos: ", positions[currentIndex]);
            telem.addData("Touched: ", robot.tele.intake.isPressed());
            telem.addData("Hook pos: ", hookPos[hookIndex]);
            telem.addData("right back", robot.tele.rb.getPower());
            telem.addData("right front", robot.tele.rf.getPower());
            telem.addData("left back", robot.tele.lb.getPower());
            telem.addData("left front", robot.tele.lf.getPower());
            telem.update();
        }
    }
}
