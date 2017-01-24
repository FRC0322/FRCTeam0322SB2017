package org.usfirst.frc0322.FRCTeam0322Strongback2017;

import org.strongback.command.*;
import org.strongback.components.Motor;

public class RunPickupMotor extends Command {
	private final Motor motor;
	
	public RunPickupMotor(Motor motor) {
		super(motor);
		this.motor = motor;
	}
	
	@Override
	public boolean execute() {
		this.motor.setSpeed(1.0);
		return true;
	}
}
