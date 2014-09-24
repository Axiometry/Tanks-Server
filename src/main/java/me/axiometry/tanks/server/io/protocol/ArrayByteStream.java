package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;

public class ArrayByteStream implements ByteStream {
	private final byte[] buffer;
	private final int type;

	private int index;

	public ArrayByteStream(byte[] buffer) {
		this(buffer, 0, READABLE | WRITABLE);
	}

	public ArrayByteStream(byte[] buffer, int type) {
		this(buffer, 0, type);
	}

	public ArrayByteStream(byte[] buffer, int type, int index) {
		this.buffer = buffer;
		this.type = type;
		this.index = index;
	}

	@Override
	public synchronized byte read() throws IOException {
		if((type & READABLE) != READABLE)
			throw new IOException("Operation not supported");
		return buffer[index++];
	}

	@Override
	public synchronized void write(byte b) throws IOException {
		if((type & WRITABLE) != WRITABLE)
			throw new IOException("Operation not supported");
		buffer[index++] = b;
	}

	@Override
	public void flush() throws IOException {
		if((type & WRITABLE) != WRITABLE)
			throw new IOException("Operation not supported");
	}

	@Override
	public int getType() {
		return type;
	}
}
