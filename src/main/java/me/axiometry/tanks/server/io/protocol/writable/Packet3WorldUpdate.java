package me.axiometry.tanks.server.io.protocol.writable;

import java.awt.Image;
import java.io.IOException;

import me.axiometry.tanks.server.io.protocol.*;
import me.axiometry.tanks.server.rendering.Sprite;
import me.axiometry.tanks.server.world.*;

public class Packet3WorldUpdate extends AbstractPacket implements
		WritablePacket {
	private final World world;

	public Packet3WorldUpdate(World world) {
		this.world = world;
	}

	@Override
	public byte getID() {
		return 3;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		Tile[] tileCache = world.getTileCache();
		writeInt(tileCache.length, stream);
		for(Tile tile : tileCache) {
			writeShort(tile.getID(), stream);
			writeBoolean(tile.isSolid(), stream);
			Sprite sprite = tile.getSprite();
			Image image = sprite.getImage();
			writeImage(image, stream);
		}
		writeInt(world.getWidth(), stream);
		writeInt(world.getHeight(), stream);

		for(Tile[] tiles : world.getTiles()) {
			for(int i = 0; i < tiles.length;) {
				Tile tile = tiles[i];
				stream.write(tile.getID());
				short skipCount = 0;
				i++;
				for(int j = i; j < tiles.length
						&& tiles[j].getID() == tile.getID()
						&& skipCount < Short.MAX_VALUE; j++)
					skipCount++;
				// Some bandwidth saving... packet went from size 17459 to 3744
				if(skipCount > 2) {
					i += skipCount;
					stream.write(Byte.MAX_VALUE);
					writeShort(skipCount, stream);
				}
			}
		}
	}

	public World getWorld() {
		return world;
	}
}
