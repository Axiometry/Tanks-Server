package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet10EntitySpawn extends AbstractPacket implements
		WritablePacket {
	protected short netID;
	private int id;
	private double x, y, rotation;
	private Metadata metadata;

	public Packet10EntitySpawn() {
	}

	public Packet10EntitySpawn(Entity entity) {
		netID = entity.getNetID();
		id = entity.getID();
		x = entity.getX();
		y = entity.getY();
		rotation = entity.getRotation();
		metadata = entity.getMetadata();
	}

	@Override
	public byte getID() {
		return 10;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeShort(netID, stream);
		writeInt(id, stream);
		writeInt((int) (x * 32D), stream);
		writeInt((int) (y * 32D), stream);
		writeShort((short) ((rotation % 360) * 32D), stream);
		writeMetadata(metadata, stream);
	}

}
