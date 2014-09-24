package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.io.protocol.*;

public class Packet15EntityDespawn extends AbstractPacket implements
		WritablePacket {
	private final int id;

	public Packet15EntityDespawn(int id) {
		this.id = id;
	}

	@Override
	public byte getID() {
		return 15;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeInt(id, stream);
	}

}
