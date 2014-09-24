package me.axiometry.tanks.server.entity;

import me.axiometry.tanks.server.io.ClientNetworkManager;

public interface ClientBasedEntity extends Entity {
	public ClientNetworkManager getNetworkManager();
}
