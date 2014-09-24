package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.world.World;

public final class TurretImpl extends AbstractHostileEntity implements Turret {
	private int fireTimer = 0;

	public TurretImpl(World world) {
		super(world);
	}

	@Override
	public short getNetID() {
		return 4;
	}

	@Override
	protected void init() {
		health = 25;
		maxHealth = 25;
		x = random.nextInt(world.getWidth() - 1) + 0.5;
		y = random.nextInt(world.getHeight() - 1) + 0.5;
		width = 1;
		height = 1;
	}

	@Override
	public void update() {
		if(world == null)
			return;
		Tank closest = null;
		double closestDistance = Double.MAX_VALUE;
		for(Entity entity : world.getEntities()) {
			if(!(entity instanceof Tank) || ((Tank) entity).getHealth() <= 0)
				continue;
			double distance = getDistanceTo(entity);
			if(distance < closestDistance) {
				closestDistance = distance;
				closest = (Tank) entity;
			}
		}
		target = closest;
		if(closest == null)
			return;
		double newRotation = Math.toDegrees(Math.atan(Math.abs(closest.getY()
				- y)
				/ Math.abs(closest.getX() - x)));
		rotation = (int) (360 - (newRotation + 90));
		if(closest.getX() - x > 0) {
			if(closest.getY() - y > 0) {
				rotation += 90;
				rotation = 360 - rotation;
				rotation += 90;
			} else
				rotation += 180;
		} else if(closest.getY() - y < 0) {
			rotation -= 90;
			rotation = 180 - rotation;
			rotation += 270;
		}
		fireTimer += 2;
		if(fireTimer > 50) {
			BulletImpl bullet = new BulletImpl(world, this, 4);
			bullet.setX(x);
			bullet.setY(y);
			bullet.setSpeedX(1.5 * Math.sin(((rotation) % 360)
					* (Math.PI / 180.0F)));
			bullet.setSpeedY(1.5 * -Math.cos(((rotation) % 360)
					* (Math.PI / 180.0F)));
			bullet.setRotation(rotation);
			world.spawnEntity(bullet);
			fireTimer = 0;
		}
	}

	@Override
	public boolean doDamage(Entity source, int damage) {
		if(source != null && source instanceof Bullet
				&& ((Bullet) source).getShooter() instanceof Turret) {
			return false;
		}
		return super.doDamage(source, damage);
	}

	@Override
	protected void onDeath() {
		world.spawnEntity(new Explosion(world, x, y, 4, 4));
	}
}
