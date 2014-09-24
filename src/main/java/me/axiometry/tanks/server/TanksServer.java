package me.axiometry.tanks.server;

import java.util.*;
import java.util.concurrent.*;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.ServerHandler;
import me.axiometry.tanks.server.io.io.IOServerHandler;
import me.axiometry.tanks.server.io.memory.*;
import me.axiometry.tanks.server.rendering.*;
import me.axiometry.tanks.server.util.*;
import me.axiometry.tanks.server.util.Timer;
import me.axiometry.tanks.server.world.*;

public final class TanksServer implements Runnable {
	public static final int DEFAULT_PORT = 33740;

	private final Timer timer;
	private final ExecutorService service;
	private final int port;

	private Future<?> task;
	private SpriteMap sprites;

	private final List<ServerHandler> serverHandlers;
	private final List<World> worlds;
	private final MemoryConnection connection;
	private int debugTimer = 50;
	private long ticksLived;

	private boolean shutdown = false;

	public TanksServer() {
		this(DEFAULT_PORT);
	}

	public TanksServer(int port) {
		this(null, port);
	}

	public TanksServer(MemoryConnection connection) {
		this(connection, DEFAULT_PORT);
	}

	public TanksServer(MemoryConnection connection, int port) {
		this.port = port;
		this.connection = connection;

		timer = new Timer(20F, 60);
		worlds = new ArrayList<World>();
		serverHandlers = new ArrayList<ServerHandler>();
		service = Executors.newCachedThreadPool(new NamedThreadFactory(
				"TanksServer"));
	}

	public synchronized void start() throws Exception {
		if(shutdown)
			throw new IllegalStateException();
		sprites = new SpriteMapImpl(getClass().getResourceAsStream(
				"/sprites.png"));
		World world = new WorldImpl(this);
		Random random = new Random();
		for(int i = 0; i < 0; i++) {
			Turret turret = new TurretImpl(world);
			turret.setX(random.nextInt(world.getWidth()));
			turret.setY(random.nextInt(world.getHeight()));
			world.spawnEntity(turret);
		}
		worlds.add(world);
		serverHandlers.add(new IOServerHandler(this));
		if(connection != null)
			serverHandlers.add(new MemoryServerHandler(this, connection));
		task = service.submit(this);
	}

	@Override
	public synchronized void run() {
		if(shutdown)
			throw new IllegalStateException();
		System.out.println("Server started successfully.");
		try {
			while(!shutdown) {
				timer.update();
				for(int i = 0; i < timer.getElapsedTicks(); i++) {
					try {
						runTick();
					} catch(Throwable exception) {
						exception.printStackTrace();
					}
				}
				if(timer.getFPSCoolDown() > 0) {
					try {
						Thread.sleep(timer.getFPSCoolDown());
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		} catch(Throwable exception) {
			exception.printStackTrace();
		}
	}

	private void runTick() {
		ticksLived++;
		debugTimer--;
		if(debugTimer <= 0) {
			debugTimer = 50;
		}
		for(ServerHandler serverHandler : serverHandlers)
			serverHandler.processPackets();
		List<World> worldsToRemove = new ArrayList<World>();
		for(World world : worlds)
			if(world.isDestroyed())
				worldsToRemove.add(world);
		worlds.removeAll(worldsToRemove);
		if(ticksLived % 500 == 0) {
			for(World world : worlds) {
				int turretCount = 0;
				for(Entity entity : world.getEntities())
					if(entity instanceof Turret)
						turretCount++;
				for(int i = 0; i < 30 - turretCount; i++)
					world.spawnEntity(new TurretImpl(world));
			}
		}
		for(World world : worlds) {
			try {
				world.update();
			} catch(Exception exception) {
				exception.printStackTrace();
				System.err.println("UNEXPECTED ERROR: World "
						+ world.toString() + "(" + world.getWidth() + "x"
						+ world.getHeight() + ", " + world.getEntities().length
						+ " entities)");
				world.destroy();
			}
		}
	}

	public void stop() {
		shutdown = true;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public long getTicksLived() {
		return ticksLived;
	}

	public Timer getTimer() {
		return timer;
	}

	public int getPort() {
		return port;
	}

	public ExecutorService getService() {
		return service;
	}

	public World[] getWorlds() {
		return worlds.toArray(new World[worlds.size()]);
	}

	public SpriteMap getSprites() {
		return sprites;
	}

	public Future<?> getTask() {
		return task;
	}

	public static void main(String[] args) {
		TanksServer server = new TanksServer();
		try {
			server.start();
		} catch(Throwable exception) {
			exception.printStackTrace();
			System.err.println("Unable to start game!");
			System.exit(-1);
		}
	}
}
