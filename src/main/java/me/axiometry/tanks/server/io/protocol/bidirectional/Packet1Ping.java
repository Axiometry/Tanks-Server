package me.axiometry.tanks.server.io.protocol.bidirectional;

import java.io.IOException;
import java.util.Random;

import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet1Ping extends AbstractPacket implements ReadablePacket,
		WritablePacket {
	private static final Random random = new Random();

	@Override
	public byte getID() {
		return 1;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeInt(random.nextInt(), stream);
	}

	@Override
	public void readData(ByteStream stream) throws IOException {
		readInt(stream);
	}

	@Override
	public void processData(ClientNetworkManager manager) {
		System.out.println("Ping!");
		manager.sendPacket(this);
	}

}
