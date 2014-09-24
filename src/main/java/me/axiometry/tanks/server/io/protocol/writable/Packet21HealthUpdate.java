package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.io.protocol.*;

public class Packet21HealthUpdate extends AbstractPacket implements
		WritablePacket {
	private final int id, health;

	public Packet21HealthUpdate(int id, int health) {
		this.id = id;
		this.health = health;
	}

	@Override
	public byte getID() {
		return 21;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeInt(id, stream);
		writeInt(health, stream);
	}
}
