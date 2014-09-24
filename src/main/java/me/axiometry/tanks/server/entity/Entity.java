package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.protocol.writable.*;
import me.axiometry.tanks.server.world.World;

public interface Entity {
	public void update();

	public double getX();

	public double getY();

	public double getRotation();

	public double getSpeedX();

	public double getSpeedY();

	public double getWidth();

	public double getHeight();

	public double getDistanceTo(Entity entity);

	public double getDistanceTo(double x, double y);

	public void setX(double x);

	public void setY(double y);

	public void setRotation(double rotation);

	public void setSpeedX(double speedX);

	public void setSpeedY(double speedY);

	public boolean isDead();

	public void kill();

	public World getWorld();

	public void setWorld(World world);

	public short getNetID();

	public int getID();

	public Metadata getMetadata();

	public void updateMetadata(Metadata metadata);

	public Packet10EntitySpawn createSpawnPacket(boolean toSelf);

	public Packet16EntityMove createMovementPacket(boolean toSelf);
}
