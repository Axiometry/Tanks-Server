package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet14UpdateMetadata extends AbstractPacket implements
		WritablePacket {
	protected int id;
	protected Metadata metadata;

	public Packet14UpdateMetadata(Entity entity) {
		id = entity.getID();
		metadata = entity.getMetadata();
	}

	@Override
	public byte getID() {
		return 14;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		stream.write((byte) id);
		writeMetadata(metadata, stream);
	}
}
