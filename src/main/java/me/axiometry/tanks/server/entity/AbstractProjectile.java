package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.world.World;

public abstract class AbstractProjectile extends AbstractEntity implements
		Projectile {
	protected Entity shooter;

	protected AbstractProjectile(World world, Entity shooter) {
		super(world);
		this.shooter = shooter;
	}

	@Override
	public Entity getShooter() {
		return shooter;
	}
}
