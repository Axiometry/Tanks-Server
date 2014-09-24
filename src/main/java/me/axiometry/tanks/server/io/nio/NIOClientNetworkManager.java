package me.axiometry.tanks.server.io.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import me.axiometry.tanks.server.io.*;
import me.axiometry.tanks.server.io.protocol.*;

public class NIOClientNetworkManager extends AbstractClientNetworkManager {
	private final NIOServerHandler serverHandler;
	private final SocketChannel socket;

	NIOClientNetworkManager(NIOServerHandler serverHandler, SocketChannel socket) {
		this.serverHandler = serverHandler;
		this.socket = socket;
		System.out.println("Client connected! "
				+ socket.socket().getInetAddress().getHostName());

		serverHandler.getService().submit(new WriteTask());
	}

	protected synchronized void handleRead(byte[] bytes) throws IOException {
		if(!isRunning() || bytes.length == 0)
			return;
		byte id = bytes[0];
		ReadablePacket packet = newReadablePacket(id);
		if(packet == null)
			throw new IOException("Bad packet, id " + id);
		packet.readData(new ArrayByteStream(bytes, ByteStream.READABLE, 1));

		queueReadPacket(packet);
	}

	@Override
	public ServerHandler getServerHandler() {
		return serverHandler;
	}

	@Override
	public synchronized void shutdown(String reason) {
		try {
			socket.close();
			serverHandler.onDisconnect();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
		super.shutdown(reason);
	}

	SocketChannel getSocket() {
		return socket;
	}

	private class WriteTask implements Runnable {
		@Override
		public void run() {
			while(isRunning()) {
				WritablePacket packet = getNextWritePacketOrWait();
				ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);
				buffer.put(packet.getID());
				try {
					packet.writeData(new BufferByteStream(buffer,
							ByteStream.WRITABLE, 1));
				} catch(IOException exception) {
					exception.printStackTrace();
				}
				byte[] bytes = new byte[buffer.position()];
				System.arraycopy(buffer.array(), 0, bytes, 0, buffer.position());
				buffer = ByteBuffer.wrap(bytes);
				serverHandler.send(NIOClientNetworkManager.this, buffer);
			}
		}
	}
}
