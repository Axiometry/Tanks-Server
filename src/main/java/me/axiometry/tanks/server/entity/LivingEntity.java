package me.axiometry.tanks.server.entity;

public interface LivingEntity extends Entity {
	public int getHealth();

	public int getMaxHealth();

	public void setHealth(int health);

	public boolean doDamage(Entity source, int damage);

	public boolean doDamage(int damage);
}
