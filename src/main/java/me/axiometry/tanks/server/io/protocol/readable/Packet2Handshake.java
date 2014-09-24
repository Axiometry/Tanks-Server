package me.axiometry.tanks.server.io.protocol.readable;

import java.io.IOException;

import javax.naming.AuthenticationException;

import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet2Handshake extends AbstractPacket implements ReadablePacket {
	private String username;

	public Packet2Handshake() {
	}

	@Override
	public byte getID() {
		return 2;
	}

	@Override
	public void readData(ByteStream stream) throws IOException {
		username = readString(stream);
	}

	@Override
	public void processData(ClientNetworkManager manager) {
		System.out.println("Username: " + username);
		try {
			manager.authenticate(username);
		} catch(AuthenticationException exception) {
			manager.shutdown("Authentication error");
		}
	}

	public String getUsername() {
		return username;
	}
}
