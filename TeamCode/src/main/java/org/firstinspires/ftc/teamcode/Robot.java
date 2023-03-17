package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Robot {
    // Hardware
    public IMU imu;
    public Servo LA, RA;
    public DcMotor FL, FR, BL, BR;

    // Variables
    ElapsedTime PID_timer = new ElapsedTime();
    public double yaw, error, lasterror=0, intergralsum=0;

    public void Initialize(IMU IMU, DcMotor.RunMode mode,
                           DcMotor Front_Left, DcMotor Front_Right,
                           DcMotor Back_Left,  DcMotor Back_Right,
                           double Arm_pos, Servo Left_Arm, Servo Right_Arm) {
        // Add Variable
        imu = IMU;
        FL = Front_Left;
        FR = Front_Right;
        BL = Back_Left;
        BR = Back_Right;
        LA = Left_Arm;
        RA = Right_Arm;

        // Initialize IMU
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.FORWARD,
                                                                       RevHubOrientationOnRobot.UsbFacingDirection.UP)));

        // Reverse Motors
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);
        // setMode Motors
        FL.setMode(mode);
        FR.setMode(mode);
        BL.setMode(mode);
        BR.setMode(mode);
        // SetBehavior Motors
        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reverse Servo
        RA.setDirection(Servo.Direction.REVERSE);
        // Set Servo Position
        LA.setPosition(Arm_pos);
        RA.setPosition(Arm_pos);
    }
    public boolean Plus_Minus(double input, int check, double range) {
        return check - range < input && input < check + range;
    }

    public double AngleWrap(double radians) {
        while (radians > Math.PI) radians -= 2 * Math.PI;
        while (radians < -Math.PI) radians += 2 * Math.PI;
        return radians;
    }

    public double PIDControl(double setpoint){
        yaw = -imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double[] K_PID = {0.8, 0.3, 0.2};
        error = AngleWrap(setpoint - yaw);
        intergralsum = Plus_Minus(Math.toDegrees(error), 0, 0.45) ? 0 : intergralsum + (error * PID_timer.seconds());
        double derivative = (error - lasterror) / PID_timer.seconds();
        lasterror = error;

        PID_timer.reset();

        double output = (error * K_PID[0]) + (intergralsum * K_PID[1]) + (derivative * K_PID[2]);
        while (0 < output && output < 0.05) output = 0.05;
        while (-0.05 < output && output < 0) output = -0.05;
        return output;
    }
}
