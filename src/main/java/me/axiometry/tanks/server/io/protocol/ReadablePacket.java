package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;

import me.axiometry.tanks.server.io.ClientNetworkManager;

public interface ReadablePacket extends Packet {
	public void readData(ByteStream stream) throws IOException;

	public void processData(ClientNetworkManager manager);
}
