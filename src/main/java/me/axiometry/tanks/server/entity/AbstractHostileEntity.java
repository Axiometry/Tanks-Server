package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.world.World;

public abstract class AbstractHostileEntity extends AbstractLivingEntity
		implements HostileEntity {
	protected LivingEntity target = null;

	public AbstractHostileEntity(World world) {
		super(world);
	}

	public LivingEntity getTarget() {
		return target;
	}
}
