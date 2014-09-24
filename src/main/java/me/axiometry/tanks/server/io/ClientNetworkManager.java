package me.axiometry.tanks.server.io;

import javax.naming.AuthenticationException;

import me.axiometry.tanks.server.entity.Tank;
import me.axiometry.tanks.server.io.protocol.*;

public interface ClientNetworkManager {
	public void defineReadablePacket(Class<? extends ReadablePacket> packet);

	public void sendPacket(WritablePacket packet);

	public void processPackets();

	public ServerHandler getServerHandler();

	public boolean isRunning();

	public void shutdown(String reason);

	public Tank getTank();

	/*
	 * Protocol methods
	 */

	public boolean isAuthenticated();

	public void authenticate(String username) throws AuthenticationException;
}