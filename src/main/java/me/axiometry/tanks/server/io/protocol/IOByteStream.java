package me.axiometry.tanks.server.io.protocol;

import java.io.*;

public class IOByteStream implements ByteStream {
	private final InputStream inputStream;
	private final OutputStream outputStream;
	public int bytesRead = 0;
	private final int type;

	public IOByteStream(InputStream inputStream) {
		this.inputStream = inputStream;
		outputStream = null;
		type = READABLE;
	}

	public IOByteStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		inputStream = null;
		type = WRITABLE;
	}

	public IOByteStream(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		type = READABLE | WRITABLE;
	}

	@Override
	public synchronized byte read() throws IOException {
		if(inputStream == null)
			throw new IOException("Operation not supported");
		bytesRead++;
		return (byte) inputStream.read();
	}

	@Override
	public synchronized void write(byte b) throws IOException {
		if(outputStream == null)
			throw new IOException("Operation not supported");
		outputStream.write(b);
		outputStream.flush();
	}

	@Override
	public synchronized void flush() throws IOException {
		if(outputStream == null)
			throw new IOException("Operation not supported");
		outputStream.flush();
	}

	@Override
	public int getType() {
		return type;
	}
}
