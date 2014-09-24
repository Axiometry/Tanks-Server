package me.axiometry.tanks.server.entity;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import me.axiometry.tanks.server.TanksServer;
import me.axiometry.tanks.server.io.protocol.writable.*;
import me.axiometry.tanks.server.world.World;

public abstract class AbstractEntity implements Entity {
	protected final TanksServer server;

	protected Random random = new Random();
	protected double x = 0, y = 0, rotation = 0, speedX = 0, speedY = 0,
			width = 0, height = 0;
	protected World world;

	private boolean dead = false;
	private final int id;

	private static final AtomicInteger nextID = new AtomicInteger(0);

	public AbstractEntity(World world) {
		id = nextID.getAndIncrement();
		this.world = world;
		server = world.getServer();
		init();
	}

	protected abstract void init();

	@Override
	public abstract void update();

	@Override
	public double getDistanceTo(Entity entity) {
		return getDistanceTo(entity.getX(), entity.getY());
	}

	@Override
	public double getDistanceTo(double x, double y) {
		return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2));
	}

	@Override
	public double getRotation() {
		return rotation;
	}

	@Override
	public double getSpeedX() {
		return speedX;
	}

	@Override
	public double getSpeedY() {
		return speedY;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	@Override
	public void setSpeedX(double speedX) {
		this.speedX = speedX;
	}

	@Override
	public void setSpeedY(double speedY) {
		this.speedY = speedY;
	}

	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public void kill() {
		dead = true;
	}

	/*
	 * Begin utility methods
	 */

	public void setSpeed(double speed) {
		speedX = speed * Math.sin((rotation % 360) * (Math.PI / 180.0F));
		speedY = speed * -Math.cos((rotation % 360) * (Math.PI / 180.0F));
	}

	public void move(double friction) {
		x += speedX;
		y += speedY;
		if(speedX > 0) {
			speedX /= friction;
			if(speedX < 0.005)
				speedX = 0;
		} else if(speedX < 0) {
			speedX /= friction;
			if(speedX > -0.005)
				speedX = 0;
		}
		if(speedY > 0) {
			speedY /= friction;
			if(speedY < 0.005)
				speedY = 0;
		} else if(speedY < 0) {
			speedY /= friction;
			if(speedY > -0.005)
				speedY = 0;
		}
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public Metadata getMetadata() {
		return new MetadataImpl();
	}

	@Override
	public void updateMetadata(Metadata metadata) {
	}

	@Override
	public Packet10EntitySpawn createSpawnPacket(boolean toSelf) {
		return new Packet10EntitySpawn(this);
	}

	@Override
	public Packet16EntityMove createMovementPacket(boolean toSelf) {
		return new Packet16EntityMove(this);
	}
}
