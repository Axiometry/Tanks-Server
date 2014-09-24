package me.axiometry.tanks.server.rendering;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class SpriteMapImpl implements SpriteMap {
	private static final int defaultSize = 32;
	private final Sprite[][] sprites;

	public SpriteMapImpl(InputStream inputStream) throws IOException {
		this(inputStream, defaultSize);
	}

	public SpriteMapImpl(InputStream inputStream, int spriteSize)
			throws IOException {
		this(ImageIO.read(inputStream), spriteSize);
	}

	public SpriteMapImpl(BufferedImage image) {
		this(image, defaultSize);
	}

	public SpriteMapImpl(BufferedImage image, int spriteSize) {
		int spritesVertically = image.getHeight() / spriteSize;
		int spritesHorizontally = image.getWidth() / spriteSize;
		sprites = new Sprite[spritesVertically][spritesHorizontally];
		for(int x = 0; x < spritesVertically; x++) {
			for(int y = 0; y < spritesHorizontally; y++) {
				sprites[x][y] = new SpriteImpl(image.getSubimage(
						y * spriteSize, x * spriteSize, spriteSize, spriteSize));
			}
		}
	}

	@Override
	public Sprite getSpriteAt(int x, int y) {
		if(x >= sprites.length || y >= sprites[x].length)
			return null;
		return sprites[x][y];
	}
}
