package me.axiometry.tanks.server.io.protocol.readable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.*;
import me.axiometry.tanks.server.io.ClientNetworkManager;
import me.axiometry.tanks.server.io.protocol.*;
import me.axiometry.tanks.server.io.protocol.writable.Packet11TankSpawn;
import me.axiometry.tanks.server.world.World;

public class Packet22Respawn extends AbstractPacket implements ReadablePacket {
	@Override
	public byte getID() {
		return 22;
	}

	@Override
	public void readData(ByteStream stream) throws IOException {
	}

	@Override
	public void processData(ClientNetworkManager manager) {
		Tank tank = manager.getTank();
		World world = tank.getWorld();
		if(tank.getHealth() <= 0) {
			tank.setHealth(tank.getMaxHealth());
			System.out.println("Respawning tank: " + tank.getName());
			manager.sendPacket(new Packet11TankSpawn(tank, true));
			for(Entity entity : world.getEntities())
				if(entity != tank && entity instanceof ClientBasedEntity)
					((ClientBasedEntity) entity).getNetworkManager()
							.sendPacket(new Packet11TankSpawn(tank));
		}
	}
}
