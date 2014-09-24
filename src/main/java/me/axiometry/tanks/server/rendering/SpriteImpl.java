package me.axiometry.tanks.server.rendering;

import java.awt.Image;

public final class SpriteImpl implements Sprite {
	private final Image image;

	public SpriteImpl(Image image) {
		this.image = image;
	}

	@Override
	public Image getImage() {
		return image;
	}

}
