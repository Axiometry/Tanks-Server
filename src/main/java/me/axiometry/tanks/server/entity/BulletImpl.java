package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.protocol.writable.Packet16EntityMove;
import me.axiometry.tanks.server.world.World;

public class BulletImpl extends AbstractProjectile implements Bullet {
	private static final double friction = 1.01;
	private int damage;
	private int timeToUpdate = 0;

	public BulletImpl(World world, Entity shooter, int damage) {
		super(world, shooter);
		this.damage = damage;
	}

	@Override
	public short getNetID() {
		return 5;
	}

	@Override
	protected void init() {
		width = 0.1;
		height = 0.1;
	}

	@Override
	public void update() {
		move(friction);
		if(world.checkEntityToTileCollision(this)) {
			x -= speedX;
			y -= speedY;
			world.spawnEntity(new Explosion(world, x, y, 0.5, 0));
			kill();
			return;
		}
		for(Entity entity : world.getEntities()) {
			if(entity.equals(this)
					|| (shooter != null && entity.equals(shooter)))
				continue;
			if(world.checkEntityToEntityCollision(this, entity)) {
				if(entity instanceof LivingEntity)
					((LivingEntity) entity).doDamage(damage);
				x -= speedX;
				y -= speedY;
				world.spawnEntity(new Explosion(world, x, y, 0.5, 0));
				kill();
				return;
			}
		}
		if(speedX == 0 && speedY == 0)
			kill();

		timeToUpdate--;
		if(timeToUpdate <= 0) {
			world.sendPacket(new Packet16EntityMove(this));
			timeToUpdate = 5;
		}
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}
}
