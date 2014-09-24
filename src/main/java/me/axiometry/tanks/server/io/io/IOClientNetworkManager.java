package me.axiometry.tanks.server.io.io;

import java.util.concurrent.ExecutorService;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import me.axiometry.tanks.server.io.*;
import me.axiometry.tanks.server.io.protocol.*;

public class IOClientNetworkManager extends AbstractClientNetworkManager {
	private final IOServerHandler serverHandler;
	private final Socket socket;

	private final ByteBuffer readBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(Short.MAX_VALUE);

	public IOClientNetworkManager(IOServerHandler serverHandler, Socket socket) {
		this.serverHandler = serverHandler;
		this.socket = socket;

		ExecutorService service = serverHandler.getService();
		service.execute(new ReadThread());
		service.execute(new WriteThread());
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

	private class ReadThread implements Runnable {
		@Override
		public void run() {
			try {
				InputStream in = socket.getInputStream();
				while(isRunning()) {
					short size = (short) (((in.read() & 0xff) << 8) | (in
							.read() & 0xff));
					int id = in.read();
					System.out.println("Reading packet: " + id + " size: "
							+ size);
					if(id == -1)
						throw new IOException("Read -1");
					ReadablePacket packet = newReadablePacket((byte) id);
					if(packet == null)
						throw new IOException("Bad packet, id " + id);
					readBuffer.clear();
					for(int i = 0; i < size; i++)
						readBuffer.put((byte) in.read());
					readBuffer.rewind();
					packet.readData(new BufferByteStream(readBuffer));
					System.out.println("Amount actually read: "
							+ readBuffer.position());
					readBuffer.clear();
					queueReadPacket(packet);
				}
			} catch(IOException exception) {
				shutdown("Read Error: " + exception);
			}
		}
	}

	private class WriteThread implements Runnable {
		@Override
		public void run() {
			try {
				OutputStream out = socket.getOutputStream();
				while(isRunning()) {
					WritablePacket packet = getNextWritePacketOrWait();
					writeBuffer.clear();
					packet.writeData(new BufferByteStream(writeBuffer));
					short size = (short) writeBuffer.position();
					System.out.println("Writing packet: " + packet.getID()
							+ " size: " + size);
					out.write((size >> 8) & 0xff);
					out.write(size & 0xff);
					out.write(packet.getID());
					byte[] bytes = new byte[writeBuffer.position()];
					System.arraycopy(writeBuffer.array(), 0, bytes, 0,
							writeBuffer.position());
					out.write(bytes);
					writeBuffer.clear();
				}
			} catch(IOException exception) {
				shutdown("Write Error: " + exception);
			}
		}
	}
}