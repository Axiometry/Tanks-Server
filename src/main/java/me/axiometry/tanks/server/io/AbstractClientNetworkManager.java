package me.axiometry.tanks.server.io;

import java.util.*;
import java.lang.reflect.Constructor;

import javax.naming.AuthenticationException;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.protocol.*;
import me.axiometry.tanks.server.io.protocol.bidirectional.*;
import me.axiometry.tanks.server.io.protocol.readable.*;
import me.axiometry.tanks.server.io.protocol.writable.Packet3WorldUpdate;
import me.axiometry.tanks.server.util.IntHashMap;
import me.axiometry.tanks.server.world.World;

public abstract class AbstractClientNetworkManager implements
		ClientNetworkManager {
	private final IntHashMap<Class<? extends ReadablePacket>> readablePackets;
	private final Queue<ReadablePacket> packetProcessQueue;
	private final Queue<WritablePacket> packetWriteQueue;

	private Tank tank;

	private boolean shutdown = false;

	public AbstractClientNetworkManager() {
		readablePackets = new IntHashMap<Class<? extends ReadablePacket>>();
		packetProcessQueue = new ArrayDeque<ReadablePacket>();
		packetWriteQueue = new ArrayDeque<WritablePacket>();

		defineDefaultPackets();
	}

	private void defineDefaultPackets() {
		defineReadablePacket(Packet1Ping.class);
		defineReadablePacket(Packet2Handshake.class);
		defineReadablePacket(Packet17TankMove.class);
		defineReadablePacket(Packet20Fire.class);
		defineReadablePacket(Packet22Respawn.class);
	}

	@Override
	public void defineReadablePacket(Class<? extends ReadablePacket> packetClass) {
		if(!isRunning())
			return;
		if(packetClass == null)
			throw new NullPointerException("Null packet");
		Constructor<? extends ReadablePacket> constructor;
		try {
			constructor = packetClass.getConstructor();
		} catch(Exception exception1) {
			throw new IllegalArgumentException("No default constructor");
		}
		Packet packet;
		try {
			packet = constructor.newInstance();
		} catch(Exception exception) {
			throw new IllegalArgumentException(exception);
		}
		byte id = packet.getID();
		if(getReadablePacketClass(id) != null)
			throw new IllegalArgumentException("Duplicate packet id");
		readablePackets.put(id, packetClass);
	}

	protected ReadablePacket newReadablePacket(byte id) {
		try {
			return getReadablePacketClass(id).newInstance();
		} catch(Exception exception) {}
		return null;
	}

	protected Class<? extends ReadablePacket> getReadablePacketClass(byte id) {
		return readablePackets.get(id);
	}

	@Override
	public void sendPacket(WritablePacket packet) {
		if(!isRunning() || packet == null)
			return;
		queueWritePacket(packet);
	}

	@Override
	public void processPackets() {
		if(!isRunning())
			return;
		List<ReadablePacket> packetsToProcess = new ArrayList<ReadablePacket>();
		synchronized(packetProcessQueue) {
			while(!packetProcessQueue.isEmpty())
				packetsToProcess.add(packetProcessQueue.poll());
		}
		try {
			for(ReadablePacket packet : packetsToProcess)
				packet.processData(this);
		} catch(Exception exception) {
			shutdown(exception.toString());
		}
	}

	protected void queueReadPacket(ReadablePacket packet) {
		synchronized(packetProcessQueue) {
			packetProcessQueue.offer(packet);
			packetProcessQueue.notifyAll();
		}
	}

	protected void queueWritePacket(WritablePacket packet) {
		synchronized(packetWriteQueue) {
			packetWriteQueue.offer(packet);
			packetWriteQueue.notifyAll();
		}
	}

	protected ReadablePacket getNextReadPacket() {
		synchronized(packetProcessQueue) {
			return packetProcessQueue.poll();
		}
	}

	protected ReadablePacket getNextReadPacketOrWait() {
		while(isRunning()) {
			synchronized(packetProcessQueue) {
				ReadablePacket packet = packetProcessQueue.poll();
				if(packet == null) {
					try {
						packetProcessQueue.wait(2000);
					} catch(InterruptedException exception) {}
				} else
					return packet;
			}
		}
		return null;
	}

	protected WritablePacket getNextWritePacket() {
		synchronized(packetWriteQueue) {
			return packetWriteQueue.poll();
		}
	}

	protected WritablePacket getNextWritePacketOrWait() {
		while(isRunning()) {
			try {
				System.out.println("*Start checking");
				synchronized(packetWriteQueue) {
					System.out.println("**sync");
					WritablePacket packet = packetWriteQueue.poll();
					if(packet == null) {
						System.out.println("**waiting");
						try {
							packetWriteQueue.wait(2000);
						} catch(InterruptedException exception) {}
						System.out.println("**done waiting");
					} else {
						System.out.println("**returning!");
						return packet;
					}
				}
			} finally {
				System.out.println("*Done checking");
			}
		}
		return null;
	}

	@Override
	public boolean isRunning() {
		return !shutdown;
	}

	@Override
	public void shutdown(String reason) {
		if(tank != null)
			tank.kill();
		System.out.println("Client exited: " + reason);
		shutdown = true;
	}

	@Override
	public Tank getTank() {
		return tank;
	}

	@Override
	public boolean isAuthenticated() {
		return tank != null;
	}

	@Override
	public void authenticate(String username) throws AuthenticationException {
		if(isAuthenticated())
			throw new AuthenticationException("Already authenticated!");
		World world = getServerHandler().getServer().getWorlds()[0];
		tank = new TankPlayer(world, this, username);
		world.spawnEntity(tank);
		sendPacket(new Packet3WorldUpdate(world));
		sendPacket(tank.createSpawnPacket(true));
		for(Entity entity : world.getEntities())
			if(entity.getNetID() != 0 && entity != tank)
				sendPacket(entity.createSpawnPacket(false));
	}

}
