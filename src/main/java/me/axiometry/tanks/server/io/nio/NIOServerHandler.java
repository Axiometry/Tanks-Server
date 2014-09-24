package me.axiometry.tanks.server.io.nio;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.io.*;

public final class NIOServerHandler implements ServerHandler {
	private final TanksServer server;
	private final Future<?> task;

	private final Selector selector;
	private final ServerSocketChannel serverChannel;
	private final ExecutorService service;

	private final Map<SocketChannel, NIOClientNetworkManager> clients;
	private final Map<NIOClientNetworkManager, ClientTask> tasks;

	public NIOServerHandler(TanksServer server) throws IOException {
		this.server = server;
		clients = new HashMap<SocketChannel, NIOClientNetworkManager>();
		tasks = new HashMap<NIOClientNetworkManager, ClientTask>();
		service = Executors.newCachedThreadPool();

		selector = Selector.open();

		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(server.getPort());
		serverChannel.socket().bind(address);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		ExecutorService service = server.getService();
		task = service.submit(this);
	}

	@Override
	public void run() {
		while(true) {
			try {
				synchronized(tasks) {
					for(ClientTask task : new ArrayList<ClientTask>(
							tasks.values())) {
						Queue<ByteBuffer> pendingData = task.pendingData;
						synchronized(pendingData) {
							if(pendingData.size() == 0)
								continue;
						}
						NIOClientNetworkManager client = task.client;
						SocketChannel socket = client.getSocket();
						SelectionKey key = socket.keyFor(selector);
						if(key == null || !key.isValid())
							continue;
						key.interestOps(SelectionKey.OP_WRITE);
					}
				}

				System.out.println("Selecting...");
				selector.select();

				Iterator<SelectionKey> selectedKeys = selector.selectedKeys()
						.iterator();
				while(selectedKeys.hasNext()) {
					final SelectionKey key = selectedKeys.next();
					selectedKeys.remove();
					if(!key.isValid())
						continue;

					// service.execute(new Runnable() {
					// @Override
					// public void run() {
					try {
						if(key.isAcceptable())
							accept(key);
						else if(key.isReadable() || key.isWritable())
							wakeup(key);
					} catch(CancelledKeyException exception) {} catch(Exception exception) {
						exception.printStackTrace();
					}
					// }
					// });
				}
			} catch(ClosedSelectorException exception) {} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		if(socketChannel == null)
			return;
		socketChannel.configureBlocking(false);
		NIOClientNetworkManager client = new NIOClientNetworkManager(this,
				socketChannel);
		synchronized(clients) {
			clients.put(socketChannel, client);
		}
		synchronized(tasks) {
			tasks.put(client, new ClientTask(client));
		}
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	private void wakeup(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		NIOClientNetworkManager networkManager = null;
		synchronized(clients) {
			networkManager = clients.get(socketChannel);
		}
		if(networkManager == null)
			throw new IOException("Network manager for key not found");
		if(!networkManager.isRunning())
			return;

		ClientTask task;
		synchronized(tasks) {
			task = tasks.get(networkManager);
		}
		if(task == null || task.reading || task.writing)
			return;
		synchronized(task.lock) {
			task.lock.notifyAll();
		}
	}

	void send(NIOClientNetworkManager networkManager, ByteBuffer data) {
		if(!networkManager.isRunning())
			return;
		Queue<ByteBuffer> pendingData;
		synchronized(tasks) {
			ClientTask task = tasks.get(networkManager);
			if(task == null)
				return;
			pendingData = task.pendingData;
		}
		if(pendingData == null)
			return;
		synchronized(pendingData) {
			pendingData.offer(ByteBuffer.wrap(data.array()));
		}
		selector.wakeup();
	}

	@Override
	public ClientNetworkManager[] getClients() {
		synchronized(clients) {
			return clients.values().toArray(
					new ClientNetworkManager[clients.size()]);
		}
	}

	@Override
	public boolean isAlive() {
		return !task.isDone();
	}

	@Override
	public void processPackets() {
		synchronized(clients) {
			for(NIOClientNetworkManager client : clients.values())
				client.processPackets();
		}
	}

	void onDisconnect() {
		synchronized(clients) {
			List<SocketChannel> shutdownClients = new ArrayList<SocketChannel>();
			for(Entry<SocketChannel, NIOClientNetworkManager> entry : clients
					.entrySet())
				if(!entry.getValue().isRunning())
					shutdownClients.add(entry.getKey());
			for(SocketChannel channel : shutdownClients) {
				try {
					channel.keyFor(selector).cancel();
				} catch(Exception exception) {}
				clients.remove(channel);
			}
		}
		synchronized(tasks) {
			List<NIOClientNetworkManager> shutdownManagers = new ArrayList<NIOClientNetworkManager>();
			for(NIOClientNetworkManager networkManager : tasks.keySet()) {
				SocketChannel socket = networkManager.getSocket();
				if(!networkManager.isRunning() || !socket.isOpen()
						|| socket.keyFor(selector) == null
						|| !socket.keyFor(selector).isValid())
					shutdownManagers.add(networkManager);
			}
			for(NIOClientNetworkManager networkManager : shutdownManagers) {
				ClientTask task = tasks.remove(networkManager);
				task.shutdown();
			}
		}
	}

	private class ClientTask implements Runnable {
		private final NIOClientNetworkManager client;
		private final Queue<ByteBuffer> pendingData;
		private final Future<?> taskFuture;
		private final Object lock = new Object();
		private volatile boolean reading = false, writing = false;

		public ClientTask(NIOClientNetworkManager client) {
			this.client = client;
			pendingData = new ArrayDeque<ByteBuffer>();
			taskFuture = service.submit(this);
		}

		@Override
		public void run() {
			while(!taskFuture.isCancelled()) {
				SelectionKey key = client.getSocket().keyFor(selector);
				if(key == null)
					continue;
				if(!key.isValid())
					return;
				try {
					if(key.isReadable())
						read(key);
					else if(key.isWritable())
						write(key);
				} catch(Throwable exception) {
					exception.printStackTrace();
					try {
						key.channel().close();
						key.cancel();
					} catch(Exception exception1) {}
					client.shutdown("Read/write error");
					return;
				}
				try {
					synchronized(lock) {
						lock.wait(5000);
					}
				} catch(InterruptedException exception) {}
			}
		}

		private void read(SelectionKey key) throws IOException {
			try {
				reading = true;
				SocketChannel socketChannel = client.getSocket();
				ByteBuffer readBuffer = ByteBuffer.allocate(2);
				int numRead = socketChannel.read(readBuffer);
				if(numRead == -1)
					throw new IOException("Read -1");
				if(numRead == 0)
					return;
				while(numRead < 2)
					numRead += socketChannel.read(readBuffer);
				short size = (short) (((short) (readBuffer.get(0) & 0xff) << 8) | (readBuffer
						.get(1) & 0xff));
				readBuffer = ByteBuffer.allocate(size + 1);
				numRead = 0;
				while(numRead < (size + 1))
					numRead += socketChannel.read(readBuffer);
				// System.out.println("Read packet id " + readBuffer.get(0)
				// + " of size: " + size + " (" + numRead + ")");
				client.handleRead(readBuffer.array());
			} finally {
				reading = false;
			}
		}

		private void write(SelectionKey key) throws IOException {
			try {
				writing = true;
				synchronized(pendingData) {
					if(pendingData.isEmpty())
						return;
					key.interestOps(SelectionKey.OP_WRITE);
					SocketChannel socketChannel = client.getSocket();

					while(!pendingData.isEmpty()) {
						ByteBuffer buf = pendingData.poll();
						// System.out.println("Sending packet id " + buf.get(0)
						// + " of size: " + buf.array().length);
						short size = (short) buf.array().length;
						try {
							socketChannel.write(ByteBuffer.wrap(new byte[] {
									(byte) ((size >> 8) & 0xff),
									(byte) (size & 0xff) }));
						} catch(IOException exception) {
							exception.printStackTrace();
							client.shutdown(exception.getMessage());
						}
						socketChannel.write(buf);
						if(buf.remaining() > 0)
							break;
					}

					key.interestOps(SelectionKey.OP_READ);
				}
			} finally {
				writing = false;
			}
		}

		private void shutdown() {
			taskFuture.cancel(true);
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
