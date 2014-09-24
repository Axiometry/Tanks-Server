package me.axiometry.tanks.server.io.memory;

import java.util.concurrent.*;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.io.*;

public class MemoryServerHandler implements ServerHandler {
	private final TanksServer server;
	private final MemoryClientNetworkManager client;
	private final ExecutorService service;

	public MemoryServerHandler(TanksServer server) {
		this.server = server;
		service = Executors.newCachedThreadPool();
		client = new MemoryClientNetworkManager(this);
	}

	public MemoryServerHandler(TanksServer server, MemoryConnection connection) {
		this.server = server;
		service = Executors.newCachedThreadPool();
		client = new MemoryClientNetworkManager(this, connection);
	}

	@Override
	public void run() {
	}

	@Override
	public ClientNetworkManager[] getClients() {
		return new ClientNetworkManager[] { client };
	}

	@Override
	public boolean isAlive() {
		return client.isRunning();
	}

	@Override
	public void processPackets() {
		client.processPackets();
	}

	@Override
	public TanksServer getServer() {
		return server;
	}

	public ExecutorService getService() {
		return service;
	}
}
