package me.axiometry.tanks.server.world;

import java.util.*;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.WritablePacket;
import me.axiometry.tanks.server.io.protocol.writable.*;
import me.axiometry.tanks.server.rendering.SpriteMap;
import me.axiometry.tanks.server.util.*;

public final class WorldImpl implements World {
	private static final Entity[] emptyEntityArray = new Entity[0];

	private final TanksServer server;
	private final List<Tile> tiles;
	private Tile[][] map;
	private int worldHeight;
	private int worldWidth;
	private Location spawnLocation;

	private final List<Entity> entities;
	private final Deque<Entity> entitiesToSpawn;

	private boolean destroyed = false;

	public WorldImpl(TanksServer server) throws Exception {
		this.server = server;
		tiles = new ArrayList<Tile>();
		load();
		entities = new ArrayList<Entity>();
		entitiesToSpawn = new ArrayDeque<Entity>();
	}

	private void load() throws Exception {
		String data = new String(IOTools.download(getClass().getResource(
				"/world.dat")));
		SpriteMap spriteMap = server.getSprites();
		int definingSection = 0;
		List<Integer[]> map = new ArrayList<Integer[]>();
		tiles.clear();
		for(String line : data.split("\n")) {
			if(line.isEmpty())
				continue;
			if(line.equals("TILES") && definingSection != 1) {
				definingSection = 1;
				continue;
			}
			if(line.startsWith("MAP ") && definingSection != 2) {
				String[] spawnLocationParts = line.split(" ")[1].split(",");
				double spawnX = Double.parseDouble(spawnLocationParts[0]);
				double spawnY = Double.parseDouble(spawnLocationParts[1]);
				spawnLocation = new Location(spawnX, spawnY);
				definingSection = 2;
				continue;
			}
			String[] parts = line.split(",");
			switch(definingSection) {
			case 1:
				byte id = Byte.parseByte(parts[0]);
				int spriteX = Integer.parseInt(parts[1]);
				int spriteY = Integer.parseInt(parts[2]);
				boolean solid = Boolean.valueOf(parts[3]);
				tiles.add(new SpriteTile(id, solid, spriteMap.getSpriteAt(
						spriteX, spriteY)));
				continue;
			case 2:
				Integer[] mapLine = new Integer[parts.length];
				if(map.size() > 0 && mapLine.length != map.get(0).length)
					throw new RuntimeException("Invalid map");
				for(int i = 0; i < parts.length; i++)
					mapLine[i] = Integer.valueOf(parts[i]);
				map.add(mapLine);
			}
		}
		if(map.size() == 0) {
			this.map = new Tile[0][0];
			worldHeight = 0;
			worldWidth = 0;
			return;
		}
		Tile[][] newMap = new Tile[map.size()][map.get(0).length];
		for(int i = 0; i < newMap.length; i++) {
			Integer[] ids = map.get(i);
			newMap[i] = new Tile[ids.length];
			for(int j = 0; j < ids.length; j++) {
				for(Tile tile : tiles)
					if(tile.getID() == ids[j].intValue())
						newMap[i][j] = tile;
			}
		}
		worldWidth = newMap.length;
		if(worldWidth > 0)
			worldHeight = newMap[0].length;
		else
			worldHeight = 0;
		this.map = newMap;
	}

	@Override
	public synchronized void update() {
		if(destroyed)
			return;
		while(entitiesToSpawn.peek() != null)
			entities.add(entitiesToSpawn.poll());
		List<Entity> deadEntities = new ArrayList<Entity>();
		for(Entity entity : entities) {
			if(entity.isDead()) {
				deadEntities.add(entity);
				sendPacket(new Packet15EntityDespawn(entity.getID()));
			}
		}
		entities.removeAll(deadEntities);
		for(Entity entity : entities) {
			entity.update();
			if(destroyed)
				return;
		}
	}

	@Override
	public int getHeight() {
		return worldHeight;
	}

	@Override
	public int getWidth() {
		return worldWidth;
	}

	@Override
	public Entity[] getEntities() {
		return entities.toArray(emptyEntityArray);
	}

	@Override
	public boolean spawnEntity(Entity entity) {
		if(entity == null)
			throw new NullPointerException();
		if(!entitiesToSpawn.offer(entity))
			return false;
		entity.setWorld(this);
		if(entity.getNetID() == 0)
			return true;
		Packet10EntitySpawn spawnPacket = entity.createSpawnPacket(false);
		if(entity instanceof ClientBasedEntity)
			sendPacket(spawnPacket, (ClientBasedEntity) entity);
		else
			sendPacket(spawnPacket);
		return true;
	}

	@Override
	public void spawnFX(FXEntity fx) {
		if(fx == null)
			throw new NullPointerException();
		sendPacket(fx.createSpawnPacket());
	}

	@Override
	public Tile[] getTileCache() {
		return tiles.toArray(new Tile[tiles.size()]);
	}

	@Override
	public Location getSpawnLocation() {
		return spawnLocation;
	}

	@Override
	public Tile getTileAt(Location location) {
		if(location == null)
			throw new NullPointerException();
		return getTileAt((int) (location.getX() - 0.5),
				(int) (location.getY() - 0.5));
	}

	@Override
	public int getTileIDAt(Location location) {
		if(location == null)
			throw new NullPointerException();
		return getTileIDAt((int) (location.getX() - 0.5),
				(int) (location.getY() - 0.5));
	}

	@Override
	public int getTileIDAt(int x, int y) {
		Tile tile = getTileAt(x, y);
		if(tile == null)
			return 0;
		return tile.getID();
	}

	@Override
	public Tile getTileAt(int x, int y) {
		if(x < 0 || y < 0 || x > worldWidth - 1 || y > worldHeight - 1)
			return null;
		return map[x][y];
	}

	@Override
	public Tile[][] getTiles() {
		return map;
	}

	@Override
	public boolean checkEntityCollision(Entity entity) {
		boolean colliding = checkEntityToTileCollision(entity);
		for(Entity otherEntity : entities) {
			if(otherEntity.equals(entity))
				continue;
			if(colliding)
				break;
			colliding |= checkEntityToEntityCollision(entity, otherEntity);
		}
		return colliding;
	}

	@Override
	public boolean checkEntityToTileCollision(Entity entity) {
		double newX = entity.getX();
		double newY = entity.getY();
		if(newX < 0.5 || newY < 0.5 || newX > worldWidth - 0.5
				|| newY > worldHeight - 0.5)
			return true;
		int x = (int) (newX - (entity.getWidth() / 2.0));
		int y = (int) (newY - (entity.getHeight() / 2.0));
		Tile tile = getTileAt(x, y);
		if(tile == null || tile.isSolid())
			return true;

		// if("".equals(""))
		// return false;
		tile = getTileAt((int) (newX + (entity.getWidth() / 2.0D)),
				(int) (newY + (entity.getHeight() / 2.0)));
		if(tile == null || tile.isSolid())
			return true;
		tile = getTileAt((int) (newX + (entity.getWidth() / 2.0D)),
				(int) (newY - (entity.getHeight() / 2.0)));
		if(tile == null || tile.isSolid())
			return true;
		tile = getTileAt((int) (newX - (entity.getWidth() / 2.0D)),
				(int) (newY + (entity.getHeight() / 2.0)));
		if(tile == null || tile.isSolid())
			return true;
		tile = getTileAt((int) (newX - (entity.getWidth() / 2.0D)),
				(int) (newY + (entity.getHeight() / 2.0)));
		if(tile == null || tile.isSolid())
			return true;
		return false;
	}

	@Override
	public boolean checkEntityToEntityCollision(Entity entity,
			Entity otherEntity) {
		if(otherEntity.getWidth() == 0 && otherEntity.getHeight() == 0
				|| entity.getDistanceTo(otherEntity) > 2)
			return false;
		double x = entity.getX() - (entity.getWidth() / 1.5);
		double y = entity.getY() - (entity.getHeight() / 1.5);
		double x2 = entity.getX() + (entity.getWidth() / 1.5);
		double y2 = entity.getY() + (entity.getHeight() / 1.5);
		double rotation = entity.getRotation();
		double newX = entity.getX() + (x - entity.getX()) * Math.cos(rotation)
				+ (y - entity.getY()) * Math.sin(rotation);
		double newY = entity.getY() - (x - entity.getX()) * Math.sin(rotation)
				+ (y - entity.getY()) * Math.cos(rotation);
		double newX2 = entity.getX() + (x2 - entity.getX())
				* Math.cos(rotation) + (y2 - entity.getY())
				* Math.sin(rotation);
		double newY2 = entity.getY() - (x2 - entity.getX())
				* Math.sin(rotation) + (y2 - entity.getY())
				* Math.cos(rotation);

		double otherX = otherEntity.getX() - (otherEntity.getWidth() / 1.5);
		double otherY = otherEntity.getY() - (otherEntity.getHeight() / 1.5);
		double otherX2 = otherEntity.getX() + (otherEntity.getWidth() / 1.5);
		double otherY2 = otherEntity.getY() + (otherEntity.getHeight() / 1.5);
		double otherRotation = otherEntity.getRotation();
		double otherNewX = otherEntity.getX() + (otherX - otherEntity.getX())
				* Math.cos(otherRotation) + (otherY - otherEntity.getY())
				* Math.sin(otherRotation);
		double otherNewY = otherEntity.getY() - (otherX - otherEntity.getX())
				* Math.sin(otherRotation) + (otherY - otherEntity.getY())
				* Math.cos(otherRotation);
		double otherNewX2 = otherEntity.getX() + (otherX2 - otherEntity.getX())
				* Math.cos(otherRotation) + (otherY2 - otherEntity.getY())
				* Math.sin(otherRotation);
		double otherNewY2 = otherEntity.getY() - (otherX2 - otherEntity.getX())
				* Math.sin(otherRotation) + (otherY2 - otherEntity.getY())
				* Math.cos(otherRotation);

		if(Math.min(newX, newX2) < Math.max(otherNewX, otherNewX2)
				&& Math.max(newX, newX2) > Math.min(otherNewX, otherNewX2)
				&& Math.min(newY, newY2) < Math.max(otherNewY, otherNewY2)
				&& Math.max(newY, newY2) > Math.min(otherNewY, otherNewY2))
			return true;
		return false;
	}

	@SuppressWarnings("unused")
	private boolean checkCollision(double x1, double y1, int w1, int h1,
			double rot1, double x2, double y2, int w2, int h2, double rot2) {
		// double distX1 = w1 / 2D;
		// double urx1 = x1 +
		// double r = Math.sqrt(())
		return true;
	}

	@Override
	public void sendPacket(WritablePacket packet, ClientBasedEntity... excluded) {
		mainLoop: for(Entity otherEntity : entities) {
			if(!(otherEntity instanceof ClientBasedEntity))
				continue;
			for(ClientBasedEntity entity : excluded)
				if(entity != null && entity.equals(otherEntity))
					continue mainLoop;
			ClientNetworkManager networkManager = ((TankPlayer) otherEntity)
					.getNetworkManager();
			networkManager.sendPacket(packet);
		}
	}

	@Override
	public synchronized void destroy() {
		destroyed = true;
		entities.clear();
		entitiesToSpawn.clear();
		tiles.clear();
		map = null;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public TanksServer getServer() {
		return server;
	}
}
