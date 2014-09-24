package me.axiometry.tanks.server.rendering;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class EmptySprite implements Sprite {
	private static final Image emptyImage = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);

	@Override
	public Image getImage() {
		return emptyImage;
	}

}
