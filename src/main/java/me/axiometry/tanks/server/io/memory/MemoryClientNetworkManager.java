package me.axiometry.tanks.server.io.memory;

import java.util.concurrent.ExecutorService;
import java.io.IOException;

import me.axiometry.tanks.server.io.*;
import me.axiometry.tanks.server.io.protocol.*;

public class MemoryClientNetworkManager extends AbstractClientNetworkManager {
	private final MemoryConnection connection;
	private final MemoryServerHandler serverHandler;

	public MemoryClientNetworkManager(MemoryServerHandler serverHandler) {
		this.serverHandler = serverHandler;
		connection = new MemoryConnection();

		ExecutorService service = serverHandler.getService();
		service.execute(new ReadThread());
		service.execute(new WriteThread());
	}

	public MemoryClientNetworkManager(MemoryServerHandler serverHandler,
			MemoryConnection partner) {
		this.serverHandler = serverHandler;
		connection = new MemoryConnection(partner);

		ExecutorService service = serverHandler.getService();
		service.execute(new ReadThread());
		service.execute(new WriteThread());
	}

	@Override
	public ServerHandler getServerHandler() {
		return serverHandler;
	}

	@Override
	public void shutdown(String reason) {
		connection.pairWith(null);
		super.shutdown(reason);
	}

	public MemoryConnection getConnection() {
		return connection;
	}

	private class ReadThread implements Runnable {
		@Override
		public void run() {
			try {
				while(isRunning()) {
					ByteStream stream = connection.getStream();
					System.out.println("loop");
					if(stream == null) {
						try {
							Thread.sleep(1);
						} catch(InterruptedException exception) {}
						continue;
					}
					System.out.println("Read 0");
					// stream.read();
					System.out.println("Read 1: " + stream.read());
					// stream.read();
					System.out.println("Read 2: " + stream.read());
					int id = stream.read();
					System.out.println("Reading packet: " + id);
					if(id == -1)
						throw new IOException("Read -1");
					ReadablePacket packet = newReadablePacket((byte) id);
					if(packet == null)
						throw new IOException("Bad packet, id " + id);
					packet.readData(stream);
					queueReadPacket(packet);
				}
				System.out.println("Done");
			} catch(IOException exception) {
				shutdown("Read Error: " + exception);
			}
		}
	}

	private class WriteThread implements Runnable {
		@Override
		public void run() {
			try {
				while(isRunning()) {
					ByteStream stream = connection.getStream();
					System.out.println("loopw");
					if(stream == null) {
						try {
							Thread.sleep(1);
						} catch(InterruptedException exception) {}
						continue;
					}
					System.out.println("Waiting for write");
					WritablePacket packet = getNextWritePacketOrWait();
					System.out.println("Sending packet: " + packet.getID());
					stream.flush();
					stream.write((byte) 0);
					System.out.println("Send 0");
					stream.write((byte) 0);
					System.out.println("Send 1");
					stream.write(packet.getID());
					System.out.println("Send 2");
					packet.writeData(stream);
					System.out.println("Send 3");
					stream.flush();
				}
				System.out.println("Done");
			} catch(IOException exception) {
				shutdown("Write Error: " + exception);
			}
		}
	}
}
