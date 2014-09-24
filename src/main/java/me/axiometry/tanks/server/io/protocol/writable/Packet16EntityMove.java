package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.Entity;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet16EntityMove extends AbstractPacket implements
		WritablePacket {
	private int id;
	private double x, y, speedX, speedY, rotation;

	public Packet16EntityMove(Entity entity) {
		if(entity == null)
			return;
		id = entity.getID();
		x = entity.getX();
		y = entity.getY();
		speedX = entity.getSpeedX();
		speedY = entity.getSpeedY();
		rotation = entity.getRotation();
	}

	@Override
	public byte getID() {
		return 16;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeInt(id, stream);
		writeInt((int) (x * 32D), stream);
		writeInt((int) (y * 32D), stream);
		writeShort((short) (speedX * 32D), stream);
		writeShort((short) (speedY * 32D), stream);
		writeShort((short) ((rotation % 360) * 32D), stream);
	}

}
