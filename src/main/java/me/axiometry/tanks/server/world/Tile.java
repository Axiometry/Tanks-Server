package me.axiometry.tanks.server.world;

import me.axiometry.tanks.server.rendering.Sprite;

public interface Tile {
	public byte getID();

	public boolean isSolid();

	public Sprite getSprite();
}
