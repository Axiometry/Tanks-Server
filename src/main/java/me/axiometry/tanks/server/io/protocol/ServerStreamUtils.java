package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;

import me.axiometry.tanks.server.entity.Metadata;

public abstract class ServerStreamUtils extends StreamUtils {
	public static void readMetadata(Metadata metadata, ByteStream stream)
			throws IOException {
		int amount = stream.read();
		for(int i = 0; i < amount; i++) {
			int id = stream.read();
			metadata.deserialize(id, stream);
		}
	}

	public static void writeMetadata(Metadata metadata, ByteStream stream)
			throws IOException {
		int[] ids = metadata.getIds();
		stream.write((byte) ids.length);
		for(int i = 0; i < ids.length; i++) {
			int id = ids[i];
			stream.write((byte) id);
			metadata.serialize(id, stream);
		}
	}
}
