package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.protocol.writable.Packet21HealthUpdate;
import me.axiometry.tanks.server.world.World;

public abstract class AbstractLivingEntity extends AbstractEntity implements
		LivingEntity {
	protected int health, maxHealth;

	public AbstractLivingEntity(World world) {
		super(world);
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public int getMaxHealth() {
		return maxHealth;
	}

	@Override
	public boolean isDead() {
		return health <= 0;
	}

	@Override
	public void kill() {
		health = 0;
	}

	@Override
	public void setHealth(int health) {
		boolean fireDeathEvent = this.health > 0 && health <= 0;
		this.health = health;
		world.sendPacket(new Packet21HealthUpdate(getID(), health));
		if(fireDeathEvent)
			onDeath();
	}

	@Override
	public boolean doDamage(Entity source, int damage) {
		setHealth(health - damage);
		return true;
	}

	@Override
	public boolean doDamage(int damage) {
		return doDamage(null, damage);
	}

	protected void onDeath() {
	}
}
