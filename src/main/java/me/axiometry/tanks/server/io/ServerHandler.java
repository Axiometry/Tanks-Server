package me.axiometry.tanks.server.io;

import me.axiometry.tanks.server.TanksServer;

public interface ServerHandler extends Runnable {
	public ClientNetworkManager[] getClients();

	public boolean isAlive();

	public void processPackets();

	public TanksServer getServer();
}
