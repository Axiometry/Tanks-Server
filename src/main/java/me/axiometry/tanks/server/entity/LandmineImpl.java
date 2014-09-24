package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.world.World;

public class LandmineImpl extends AbstractProjectile implements Landmine {
	private int flashTimer = 0;
	private int timer = 0;
	private boolean red = true;

	public LandmineImpl(World world, Entity shooter) {
		super(world, shooter);
	}
	
	@Override
	public short getNetID() {
		return 6;
	}

	@Override
	protected void init() {
	}

	@Override
	public void update() {
		timer++;
		flashTimer++;
		if(flashTimer > (timer > 90 ? (timer > 130 ? 2 : 10) : 30)) {
			red = !red;
			flashTimer = 0;
		}
		if(timer > 160) {
			world.spawnEntity(new Explosion(world, x, y, 12, 50));
			kill();
			return;
		}
	}
}
