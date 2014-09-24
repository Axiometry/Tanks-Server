package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;

public interface WritablePacket extends Packet {
	public void writeData(ByteStream stream) throws IOException;
}
