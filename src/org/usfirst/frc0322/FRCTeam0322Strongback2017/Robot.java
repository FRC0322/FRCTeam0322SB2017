/* Created Sat Jan 14 20:17:30 EST 2017 */
package org.usfirst.frc0322.FRCTeam0322Strongback2017;

import org.strongback.Strongback;
import org.strongback.components.AngleSensor;
import org.strongback.components.CurrentSensor;
import org.strongback.components.Motor;
import org.strongback.components.ThreeAxisAccelerometer;
import org.strongback.components.VoltageSensor;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.Gamepad;
import org.strongback.drive.TankDrive;
import org.strongback.hardware.Hardware;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.interfaces.Accelerometer.Range;

public class Robot extends IterativeRobot {
	private static final int LEFT_DRIVESTICK_PORT = 0;
	private static final int RIGHT_DRIVESTICK_PORT = 1;
	private static final int MANIPULATOR_STICK_PORT = 2;
	private static final int LF_MOTOR_PORT = 0;
	private static final int RF_MOTOR_PORT = 1;
	private static final int LR_MOTOR_PORT = 2;
	private static final int RR_MOTOR_PORT = 3;
	
	private static final SPI.Port GYRO_PORT = SPI.Port.kOnboardCS0;
	private static final SPI.Port ACCEL_PORT = SPI.Port.kOnboardCS1;
	private static final Range ACCEL_RANGE = Range.k2G;
	
	private FlightStick leftDriveStick, rightDriveStick;
	private Gamepad manipulatorStick;
	
	private TankDrive drivetrain;
	private ContinuousRange leftSpeed, rightSpeed;
	
	private ThreeAxisAccelerometer accel;
	private AngleSensor gyro;
	
	public static UsbCamera cameraServer;

    @Override
    public void robotInit() {
    	//Setup drivetrain
    	Motor leftDriveMotors = Motor.compose(Hardware.Motors.talon(LF_MOTOR_PORT),
    											Hardware.Motors.talon(LR_MOTOR_PORT));
    	Motor rightDriveMotors = Motor.compose(Hardware.Motors.talon(RF_MOTOR_PORT),
    											Hardware.Motors.talon(RR_MOTOR_PORT));
    	drivetrain = new TankDrive(leftDriveMotors, rightDriveMotors.invert());
    	
    	//Setup joysticks
    	leftDriveStick = Hardware.HumanInterfaceDevices.logitechAttack3D(LEFT_DRIVESTICK_PORT);
    	rightDriveStick = Hardware.HumanInterfaceDevices.logitechAttack3D(RIGHT_DRIVESTICK_PORT);
    	manipulatorStick = Hardware.HumanInterfaceDevices.xbox360(MANIPULATOR_STICK_PORT);
    	
    	//Setup sensors
    	accel = Hardware.Accelerometers.accelerometer(ACCEL_PORT, ACCEL_RANGE);
    	gyro = Hardware.AngleSensors.gyroscope(GYRO_PORT);
    	VoltageSensor battery = Hardware.powerPanel().getVoltageSensor();
    	CurrentSensor current = Hardware.powerPanel().getTotalCurrentSensor();
    	
    	//Setup drivetrain variables
    	ContinuousRange sensitivity = leftDriveStick.getAxis(2).invert().map(t -> (t + 1.0) / 2.0);
    	leftSpeed = leftDriveStick.getPitch().scale(sensitivity::read);
    	rightSpeed = rightDriveStick.getPitch().scale(sensitivity::read);
    	
    	//Setup Camera
    	cameraServer = CameraServer.getInstance().startAutomaticCapture();
        
    	Strongback.configure().recordNoEvents().recordNoData();
    }

    @Override
    public void teleopInit() {
        // Start Strongback functions ...
        Strongback.start();
    }

    @Override
    public void teleopPeriodic() {
    	//This line runs the drivetrain
    	drivetrain.tank(leftSpeed.read(), rightSpeed.read());
    }

    @Override
    public void disabledInit() {
    	drivetrain.stop();
        // Tell Strongback that the robot is disabled so it can flush and kill commands.
        Strongback.disable();
    }
    
	@Override
    public void disabledPeriodic() {
		//debugPrint();
    }
	
	public void debugPrint() {
		System.out.println("Gyro Angle " + gyro.getAngle());
    	System.out.println();
    	System.out.println("X-Axis " + accel.getXDirection().getAcceleration());
    	System.out.println("Y-Axis " + accel.getYDirection().getAcceleration());
    	System.out.println("Z-Axis " + accel.getZDirection().getAcceleration());
    	System.out.println();
    	System.out.println();
	}

}