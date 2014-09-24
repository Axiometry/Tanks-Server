package me.axiometry.tanks.server.entity;

import java.io.IOException;

import me.axiometry.tanks.server.io.protocol.ByteStream;

public interface Metadata {
	public enum Type {
		STRING(String.class),
		INT(Integer.class),
		BYTE(Byte.class),
		SHORT(Short.class),
		LONG(Long.class),
		FLOAT(Float.class),
		DOUBLE(Double.class);

		private final Class<?> typeClass;

		private Type(Class<?> typeClass) {
			this.typeClass = typeClass;
		}

		public Class<?> getTypeClass() {
			return typeClass;
		}

		public static Type byClass(Class<?> typeClass) {
			for(Type type : Type.values())
				if(typeClass.equals(type.getTypeClass()))
					return type;
			return null;
		}
	}

	public int[] getIds();

	public String getString(int id);

	public int getInt(int id);

	public byte getByte(int id);

	public short getShort(int id);

	public long getLong(int id);

	public float getFloat(int id);

	public double getDouble(int id);

	public Type getType(int id);

	public void setString(int id, String string);

	public void setInt(int id, int i);

	public void setByte(int id, byte b);

	public void setShort(int id, short s);

	public void setLong(int id, long l);

	public void setFloat(int id, float f);

	public void setDouble(int id, double d);

	public void serialize(int id, ByteStream stream) throws IOException;

	public void deserialize(int id, ByteStream stream) throws IOException;
}
