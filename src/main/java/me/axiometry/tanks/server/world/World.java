package me.axiometry.tanks.server.world;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.protocol.WritablePacket;
import me.axiometry.tanks.server.util.Location;

public interface World {
	public void update();

	public Tile[][] getTiles();

	public Location getSpawnLocation();

	public Tile getTileAt(Location location);

	public Tile getTileAt(int x, int y);

	public int getTileIDAt(Location location);

	public int getTileIDAt(int x, int y);

	public int getWidth();

	public int getHeight();

	public Entity[] getEntities();

	public Tile[] getTileCache();

	public boolean spawnEntity(Entity entity);

	public void spawnFX(FXEntity fx);

	public boolean checkEntityCollision(Entity entity);

	public boolean checkEntityToTileCollision(Entity entity);

	public boolean checkEntityToEntityCollision(Entity entity,
			Entity otherEntity);

	public void sendPacket(WritablePacket packet, ClientBasedEntity... excluded);

	public void destroy();

	public boolean isDestroyed();

	public TanksServer getServer();
}
