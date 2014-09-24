package me.axiometry.tanks.server.io.protocol.writable;

import java.io.IOException;

import me.axiometry.tanks.server.entity.FXEntity;
import me.axiometry.tanks.server.io.protocol.*;

public class Packet12FxSpawn extends AbstractPacket implements WritablePacket {
	private final FXEntity fx;

	public Packet12FxSpawn(FXEntity fx) {
		this.fx = fx;
	}

	public byte getID() {
		return 12;
	}

	@Override
	public void writeData(ByteStream stream) throws IOException {
		writeShort(fx.getNetID(), stream);
		writeInt((int) (fx.getX() * 32D), stream);
		writeInt((int) (fx.getY() * 32D), stream);
		writeInt((int) (fx.getSpeed() * 32D), stream);
		writeShort((short) ((fx.getRotation() % 360) * 32D), stream);
		writeShort((short) ((fx.getDegreesFreedom() - 1) % 360), stream);
		writeShort((short) fx.getAmount(), stream);
	}
}
