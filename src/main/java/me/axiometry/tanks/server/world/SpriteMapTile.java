package me.axiometry.tanks.server.world;

import me.axiometry.tanks.server.rendering.Sprite;

public final class SpriteMapTile implements Tile {
	private final byte id;
	private final boolean solid;
	private final Sprite sprite;

	public SpriteMapTile(byte id, boolean solid, Sprite sprite) {
		if(sprite == null)
			throw new NullPointerException();
		this.id = id;
		this.solid = solid;
		this.sprite = sprite;
	}

	@Override
	public byte getID() {
		return id;
	}

	@Override
	public boolean isSolid() {
		return solid;
	}

	@Override
	public Sprite getSprite() {
		return sprite;
	}
}