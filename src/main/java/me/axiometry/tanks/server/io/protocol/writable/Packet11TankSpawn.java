package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.Tank;
import me.axiometry.tanks.server.io.protocol.ByteStream;

public class Packet11TankSpawn extends Packet10EntitySpawn {
	private final String name;
	private final double barrelRotation;

	public Packet11TankSpawn(Tank tank) {
		this(tank, false);
	}

	public Packet11TankSpawn(Tank tank, boolean central) {
		super(tank);
		if(central)
			netID = 1;
		name = tank.getName();
		barrelRotation = tank.getBarrelRotation();
	}

	@Override
	public byte getID() {
		return 11;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		super.writeData(stream);
		writeString(name, stream);
		writeShort((short) ((barrelRotation % 360) * 32D), stream);
	}
}
