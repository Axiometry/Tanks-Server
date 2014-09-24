package me.axiometry.tanks.server.entity;

import static me.axiometry.tanks.server.io.protocol.StreamUtils.*;

import java.io.IOException;

import me.axiometry.tanks.server.io.protocol.ByteStream;
import me.axiometry.tanks.server.util.IntHashMap;

public class MetadataImpl implements Metadata {
	private final IntHashMap<Object> map = new IntHashMap<Object>();

	@Override
	public int[] getIds() {
		return map.getKeys();
	}

	@Override
	public String getString(int id) {
		return (String) map.get(id);
	}

	@Override
	public int getInt(int id) {
		return (Integer) map.get(id);
	}

	@Override
	public byte getByte(int id) {
		return (Byte) map.get(id);
	}

	@Override
	public short getShort(int id) {
		return (Short) map.get(id);
	}

	@Override
	public long getLong(int id) {
		return (Long) map.get(id);
	}

	@Override
	public float getFloat(int id) {
		return (Float) map.get(id);
	}

	@Override
	public double getDouble(int id) {
		return (Double) map.get(id);
	}

	@Override
	public Type getType(int id) {
		return Type.byClass(map.get(id).getClass());
	}

	@Override
	public void setString(int id, String string) {
		map.put(id, string);
	}

	@Override
	public void setInt(int id, int i) {
		map.put(id, Integer.valueOf(i));
	}

	@Override
	public void setByte(int id, byte b) {
		map.put(id, Byte.valueOf(b));
	}

	@Override
	public void setShort(int id, short s) {
		map.put(id, Short.valueOf(s));
	}

	@Override
	public void setLong(int id, long l) {
		map.put(id, Long.valueOf(l));
	}

	@Override
	public void setFloat(int id, float f) {
		map.put(id, Float.valueOf(f));
	}

	@Override
	public void setDouble(int id, double d) {
		map.put(id, Double.valueOf(d));
	}

	@Override
	public void serialize(int id, ByteStream stream) throws IOException {
		Object value = map.get(id);
		Type type = Type.byClass(value.getClass());
		stream.write((byte) type.ordinal());
		switch(type) {
		case STRING:
			writeString((String) value, stream);
			break;
		case INT:
			writeInt((Integer) value, stream);
			break;
		case BYTE:
			stream.write((Byte) value);
			break;
		case SHORT:
			writeShort((Short) value, stream);
			break;
		case LONG:
			writeLong((Long) value, stream);
			break;
		case FLOAT:
			writeFloat((Float) value, stream);
			break;
		case DOUBLE:
			writeDouble((Double) value, stream);
			break;
		}
	}

	@Override
	public void deserialize(int id, ByteStream stream) throws IOException {
		Type type = Type.values()[stream.read()];
		switch(type) {
		case STRING:
			setString(id, readString(stream));
			break;
		case INT:
			setInt(id, readInt(stream));
			break;
		case BYTE:
			setByte(id, stream.read());
			break;
		case SHORT:
			setShort(id, readShort(stream));
			break;
		case LONG:
			setLong(id, readLong(stream));
			break;
		case FLOAT:
			setFloat(id, readFloat(stream));
			break;
		case DOUBLE:
			setDouble(id, readDouble(stream));
			break;
		}
	}

}
