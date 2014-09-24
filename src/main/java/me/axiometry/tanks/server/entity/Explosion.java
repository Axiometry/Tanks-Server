package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.world.World;

public class Explosion extends AbstractEntity {
	private int timer = 0;
	private double yeild = 0;
	private int damage = 0;

	public Explosion(World world, double x, double y, double yeild, int damage) {
		super(world);
		this.x = x;
		this.y = y;
		this.yeild = yeild;
		this.damage = damage;
	}

	@Override
	protected void init() {
	}

	@Override
	public void update() {
		if(damage > 0 && timer == 2) {
			double radius = yeild * 1.2;
			for(Entity entity : world.getEntities())
				if(entity instanceof LivingEntity
						&& getDistanceTo(entity) <= radius)
					((LivingEntity) entity).doDamage(damage);
		}
		SmokeFX smoke = new SmokeFX();
		smoke.setX(x);
		smoke.setY(y);
		smoke.setAmount((int) (4 * yeild));
		smoke.setDegreesFreedom(360);
		smoke.setSpeed(yeild);
		world.spawnFX(smoke);
		timer++;
		if(timer > 5)
			kill();
	}

	@Override
	public short getNetID() {
		return 0;
	}
}
