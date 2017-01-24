package org.usfirst.frc0322.FRCTeam0322Strongback2017;

import org.strongback.command.*;
import org.strongback.components.Motor;

public class StopShooterMotor extends Command {
	private final Motor motor;
	
	public StopShooterMotor(Motor motor) {
		super(motor);
		this.motor = motor;
	}
	
	@Override
	public boolean execute() {
		this.motor.setSpeed(0.0);
		return true;
	}
}
