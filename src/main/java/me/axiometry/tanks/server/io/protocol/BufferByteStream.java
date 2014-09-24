package me.axiometry.tanks.server.io.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferByteStream implements ByteStream {
	private final ByteBuffer buffer;
	private final int type;

	public BufferByteStream(ByteBuffer buffer) {
		this(buffer, READABLE | WRITABLE, buffer.position());
	}

	public BufferByteStream(ByteBuffer buffer, int type) {
		this(buffer, type, buffer.position());
	}

	public BufferByteStream(ByteBuffer buffer, int type, int index) {
		this.buffer = buffer;
		this.type = type;
	}

	@Override
	public synchronized byte read() throws IOException {
		if((type & READABLE) != READABLE)
			throw new IOException("Operation not supported");
		return buffer.get();
	}

	@Override
	public synchronized void write(byte b) throws IOException {
		if((type & WRITABLE) != WRITABLE)
			throw new IOException("Operation not supported");
		buffer.put(b);
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
