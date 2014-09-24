package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.protocol.writable.Packet12FxSpawn;
import me.axiometry.tanks.server.world.World;

public abstract class FXEntity {
	private World world;
	private double x, y, speed, rotation = 0;
	private int degreesFreedom = 0, amount = 0;

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public int getDegreesFreedom() {
		return degreesFreedom;
	}

	public void setDegreesFreedom(int degreesFreedom) {
		this.degreesFreedom = degreesFreedom;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Packet12FxSpawn createSpawnPacket() {
		return new Packet12FxSpawn(this);
	}

	public abstract short getNetID();
}
