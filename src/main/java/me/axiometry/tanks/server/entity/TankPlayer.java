package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.bidirectional.Packet17TankMove;
import me.axiometry.tanks.server.io.protocol.writable.*;
import me.axiometry.tanks.server.util.Location;
import me.axiometry.tanks.server.world.World;

public class TankPlayer extends AbstractLivingEntity implements Tank,
		ClientBasedEntity {
	private final ClientNetworkManager networkManager;
	private final String username;

	private int timeToUpdate = 0;
	private double barrelRotation;

	public TankPlayer(World world, ClientNetworkManager networkManager,
			String username) {
		super(world);
		this.networkManager = networkManager;
		this.username = username;
	}

	@Override
	public String getName() {
		return username;
	}

	@Override
	public double getBarrelRotation() {
		return barrelRotation;
	}

	@Override
	public void setBarrelRotation(double barrelRotation) {
		this.barrelRotation = barrelRotation;
	}

	@Override
	protected void init() {
		health = 500;
		maxHealth = 500;
		Location spawnLocation = world.getSpawnLocation();
		x = spawnLocation.getX();
		y = spawnLocation.getY();
		width = 1;
		height = 1;
	}

	@Override
	public void update() {
		timeToUpdate--;
		if(timeToUpdate <= 0) {
			world.sendPacket(new Packet17TankMove(this), this);
			timeToUpdate = 5;
		}
	}

	@Override
	protected void onDeath() {
		world.spawnEntity(new Explosion(world, x, y, 15, 20));
		for(Entity entity : world.getEntities()) {
			if(entity instanceof ClientBasedEntity)
				((ClientBasedEntity) entity).getNetworkManager().sendPacket(
						new Packet15EntityDespawn(getID()));
		}
	}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public ClientNetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public short getNetID() {
		return 2;
	}

	@Override
	public Packet10EntitySpawn createSpawnPacket(boolean toSelf) {
		return new Packet11TankSpawn(this, toSelf);
	}

	@Override
	public Packet16EntityMove createMovementPacket(boolean toSelf) {
		return new Packet17TankMove(this);
	}
}
