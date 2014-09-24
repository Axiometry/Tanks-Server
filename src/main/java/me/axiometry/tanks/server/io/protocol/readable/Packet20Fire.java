package me.axiometry.tanks.server.io.protocol.readable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.*;
import me.axiometry.tanks.server.world.World;

public class Packet20Fire extends AbstractPacket implements ReadablePacket {
	public enum WeaponType {
		BULLET,
		CHARGE
	}

	private WeaponType type;
	private double rotation;

	@Override
	public byte getID() {
		return 20;
	}

	@Override
	public void readData(ByteStream stream) throws IOException {
		type = WeaponType.values()[stream.read()];
		rotation = readDouble(stream);
	}

	@Override
	public void processData(ClientNetworkManager manager) {
		Tank tank = manager.getTank();
		World world = tank.getWorld();
		AbstractEntity entity = null;
		double speed = 0, xOffset = 0, yOffset = 0;
		switch(type) {
		case BULLET:
			entity = new BulletImpl(world, manager.getTank(), 7);
			speed = 1.5;

			xOffset = ((tank.getWidth() - (tank.getWidth() / 4)) * Math
					.sin((rotation % 360) * (Math.PI / 180D)))
					+ (tank.getSpeedX() * 5);
			yOffset = ((tank.getHeight() - (tank.getHeight() / 4)) * -Math
					.cos((rotation % 360) * (Math.PI / 180D)))
					+ (tank.getSpeedY() * 5);
			SmokeFX smoke = new SmokeFX();
			smoke.setX(tank.getX() + xOffset);
			smoke.setY(tank.getY() + yOffset);
			smoke.setRotation(rotation - 10);
			smoke.setDegreesFreedom(21);
			smoke.setAmount(9);
			smoke.setSpeed(Math.abs(tank.getSpeedX()) > 0.1 ? 4 : 2);
			world.spawnFX(smoke);
			break;
		case CHARGE:
			entity = new LandmineImpl(world, manager.getTank());
			break;
		}
		entity.setX(tank.getX() + xOffset);
		entity.setY(tank.getY() + yOffset);
		entity.setRotation(rotation);
		if(speed > 0)
			entity.setSpeed(speed);
		world.spawnEntity(entity);
	}
}
