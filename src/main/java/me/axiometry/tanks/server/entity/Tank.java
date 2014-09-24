package me.axiometry.tanks.server.entity;

public interface Tank extends LivingEntity {
	public static final double ROTATION_SPEED = 1.5;
	public static final double FRICTION = 1.7;
	public static final double INERTIA = 3;
	public static final double TOP_SPEED = 0.25;

	public String getName();

	public double getBarrelRotation();

	public void setBarrelRotation(double barrelRotation);
}
