package me.axiometry.tanks.server.io.protocol.bidirectional;

import java.io.IOException;

import me.axiometry.tanks.server.entity.Tank;
import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.*;
import me.axiometry.tanks.server.io.protocol.writable.Packet16EntityMove;

public class Packet17TankMove extends Packet16EntityMove implements
		ReadablePacket {
	private double x, y, speedX, speedY, rotation, barrelRotation;

	public Packet17TankMove() {
		super(null);
	}

	public Packet17TankMove(Tank tank) {
		super(tank);
		barrelRotation = tank.getBarrelRotation();
	}

	@Override
	public byte getID() {
		return 17;
	}

	@Override
	public void readData(ByteStream stream) throws IOException {
		x = readInt(stream) / 32D;
		y = readInt(stream) / 32D;
		speedX = readShort(stream) / 32D;
		speedY = readShort(stream) / 32D;
		rotation = readShort(stream) / 32D;
		barrelRotation = readShort(stream) / 32D;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		super.writeData(stream);
		writeShort((short) ((barrelRotation % 360) * 32D), stream);
	}

	@Override
	public void processData(ClientNetworkManager manager) {
		Tank tank = manager.getTank();
		tank.setX(x);
		tank.setY(y);
		tank.setSpeedX(speedX);
		tank.setSpeedY(speedY);
		tank.setRotation(rotation);
		tank.setBarrelRotation(barrelRotation);
	}
}
