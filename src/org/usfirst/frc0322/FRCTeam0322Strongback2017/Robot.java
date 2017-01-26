/* Created Sat Jan 14 20:17:30 EST 2017 */
package org.usfirst.frc0322.FRCTeam0322Strongback2017;

import org.strongback.Strongback;
import org.strongback.SwitchReactor;
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
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.analog.adis16448.frc.ADIS16448_IMU;

public class Robot extends IterativeRobot {
	private static final int LEFT_DRIVESTICK_PORT = 0;
	private static final int RIGHT_DRIVESTICK_PORT = 1;
	private static final int MANIPULATOR_STICK_PORT = 2;
	private static final int LF_MOTOR_PORT = 0;
	private static final int RF_MOTOR_PORT = 1;
	private static final int LR_MOTOR_PORT = 2;
	private static final int RR_MOTOR_PORT = 3;
	private static final int SHOOTER_MOTOR_PORT = 4;
	
	private static final int LIFT_MOTOR_CAN = 1;
	private static final int PICKUP_MOTOR_CAN = 2;
	
	/*
	private static final SPI.Port GYRO_PORT = SPI.Port.kOnboardCS0;
	private static final SPI.Port ACCEL_PORT = SPI.Port.kOnboardCS1;
	private static final Range ACCEL_RANGE = Range.k2G;
	*/
	
	private FlightStick leftDriveStick, rightDriveStick;
	private Gamepad manipulatorStick;
	
	private TankDrive drivetrain;
	private ContinuousRange leftSpeed, rightSpeed;
	
	private SwitchReactor lift, pickup, shooter;
	private Motor liftMotor, pickupMotor, shooterMotor;
	
	//private ThreeAxisAccelerometer accel;
	//private AngleSensor gyro;
	private ADIS16448_IMU imu;
	
	public static UsbCamera cameraServer;

    @Override
    public void robotInit() {
    	//Setup drivetrain
    	Motor leftDriveMotors = Motor.compose(Hardware.Motors.talon(LF_MOTOR_PORT),
    											Hardware.Motors.talon(LR_MOTOR_PORT));
    	Motor rightDriveMotors = Motor.compose(Hardware.Motors.talon(RF_MOTOR_PORT),
    											Hardware.Motors.talon(RR_MOTOR_PORT));
    	drivetrain = new TankDrive(leftDriveMotors, rightDriveMotors.invert());
    	
    	//Setup Manipulators
    	liftMotor = Hardware.Motors.talonSRX(LIFT_MOTOR_CAN);
    	pickupMotor = Hardware.Motors.talonSRX(PICKUP_MOTOR_CAN);
    	shooterMotor = Hardware.Motors.talon(SHOOTER_MOTOR_PORT);
    	
    	//Setup joysticks
    	leftDriveStick = Hardware.HumanInterfaceDevices.logitechAttack3D(LEFT_DRIVESTICK_PORT);
    	rightDriveStick = Hardware.HumanInterfaceDevices.logitechAttack3D(RIGHT_DRIVESTICK_PORT);
    	manipulatorStick = Hardware.HumanInterfaceDevices.xbox360(MANIPULATOR_STICK_PORT);
    	
    	//Setup sensors
    	imu = new ADIS16448_IMU();
    	//accel = Hardware.Accelerometers.accelerometer(ACCEL_PORT, ACCEL_RANGE);
    	//gyro = Hardware.AngleSensors.gyroscope(GYRO_PORT);
    	VoltageSensor battery = Hardware.powerPanel().getVoltageSensor();
    	CurrentSensor current = Hardware.powerPanel().getTotalCurrentSensor();
    	
    	//Setup drivetrain variables
    	ContinuousRange sensitivity = leftDriveStick.getAxis(2).invert().map(t -> (t + 1.0) / 2.0);
    	leftSpeed = leftDriveStick.getPitch().scale(sensitivity::read);
    	rightSpeed = rightDriveStick.getPitch().scale(sensitivity::read);
    	
    	//Setup Switches
    	lift = Strongback.switchReactor();
    	pickup = Strongback.switchReactor();
    	shooter = Strongback.switchReactor();
    	
    	//Setup Camera
    	cameraServer = CameraServer.getInstance().startAutomaticCapture();
    	
    	Strongback.dataRecorder()
		.register("Battery Volts", 1000, battery::getVoltage)
		.register("Current load", 1000, current::getCurrent)
		.register("Left Motors", leftDriveMotors)
		.register("Right Motors", rightDriveMotors)
		.register("LeftDriveStick", 1000, leftSpeed::read)
		.register("RightDriveStick", 1000, rightSpeed::read)
		.register("Drive Sensitivity", 1000, sensitivity::read);
        
    	Strongback.configure().recordNoEvents().recordDataToFile("FRC0322Java-");
    	//Strongback.configure().recordNoEvents().recordNoData();
    }
	@Override
    public void autonomousInit() {
        // Start Strongback functions ...
        Strongback.start();
    }
    
	@Override
    public void autonomousPeriodic() {
    	debugPrint();
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
    	
    	//This section controls the lift
    	lift.onTriggered(manipulatorStick.getA(), ()->Strongback.submit(new RunLiftMotor(liftMotor)));
    	lift.onUntriggered(manipulatorStick.getA(), ()->Strongback.submit(new StopLiftMotor(liftMotor)));
    	
    	//This section controls the pickup mechanism
    	pickup.onTriggered(manipulatorStick.getB(), ()->Strongback.submit(new RunPickupMotor(pickupMotor)));
    	pickup.onUntriggered(manipulatorStick.getB(), ()->Strongback.submit(new StopPickupMotor(pickupMotor)));
    	
    	//This section controls the shooter mechanism
    	shooter.onTriggered(manipulatorStick.getX(), ()->Strongback.submit(new RunShooterMotor(shooterMotor)));
    	shooter.onUntriggered(manipulatorStick.getX(), ()->Strongback.submit(new StopShooterMotor(shooterMotor)));
    	
    	SmartDashboard.putData("IMU", imu);
    	debugPrint();
    }

    @Override
    public void disabledInit() {
    	drivetrain.stop();
        // Tell Strongback that the robot is disabled so it can flush and kill commands.
        Strongback.disable();
    }
    
	@Override
    public void disabledPeriodic() {
		debugPrint();
    }
	
	public void debugPrint() {
		System.out.println("Gyro Angle " + imu.getAngle());
    	System.out.println();
    	System.out.println("X-Axis Acceleration " + imu.getAccelX());
    	System.out.println("Y-Axis Acceleration " + imu.getAccelY());
    	System.out.println("Z-Axis Acceleration " + imu.getAccelZ());
    	System.out.println();
    	System.out.println("Temperature " + imu.getTemperature());
    	System.out.println("Pressure  " + imu.getBarometricPressure());
    	System.out.println();
    	System.out.println();
	}

}
