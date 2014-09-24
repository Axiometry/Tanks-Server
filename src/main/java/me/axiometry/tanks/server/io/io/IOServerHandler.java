package me.axiometry.tanks.server.io.io;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.*;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.io.*;

public class IOServerHandler implements ServerHandler {
	private final TanksServer server;
	private final Future<?> task;

	private final ServerSocket serverSocket;
	private final ExecutorService service;

	private final List<IOClientNetworkManager> clients;

	public IOServerHandler(TanksServer server) throws IOException {
		this.server = server;
		clients = new Vector<IOClientNetworkManager>();
		serverSocket = new ServerSocket(server.getPort());
		service = Executors.newCachedThreadPool();

		ExecutorService service = server.getService();
		task = service.submit(this);
	}

	@Override
	public void run() {
		while(true) {
			try {
				Socket socket = serverSocket.accept();
				IOClientNetworkManager client = new IOClientNetworkManager(
						this, socket);
				synchronized(client) {
					clients.add(client);
				}
			} catch(IOException exception) {
				exception.printStackTrace();
			}

		}
	}

	@Override
	public ClientNetworkManager[] getClients() {
		return clients.toArray(new ClientNetworkManager[clients.size()]);
	}

	@Override
	public boolean isAlive() {
		return !task.isDone();
	}

	@Override
	public void processPackets() {
		synchronized(clients) {
			for(IOClientNetworkManager client : clients)
				client.processPackets();
		}
	}

	void onDisconnect() {
		synchronized(clients) {
			List<IOClientNetworkManager> deadClients = new ArrayList<IOClientNetworkManager>();
			for(IOClientNetworkManager client : clients)
				if(!client.isRunning())
					deadClients.add(client);
			clients.removeAll(deadClients);
		}
	}

	ExecutorService getService() {
		return service;
	}

	@Override
	public TanksServer getServer() {
		return server;
	}
}
