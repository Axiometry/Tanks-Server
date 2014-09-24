package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;

public interface ByteStream {
	public static final int READABLE = 1, WRITABLE = 2;

	public byte read() throws IOException;

	public void write(byte b) throws IOException;

	public void flush() throws IOException;

	public int getType();
}
