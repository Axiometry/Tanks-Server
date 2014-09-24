package me.axiometry.tanks.server.io.memory;

import java.io.*;

import me.axiometry.tanks.server.io.protocol.*;

public class MemoryConnection {
	private MemoryConnection partner;
	private ByteStream stream;

	private PipedInputStream in;
	private PipedOutputStream out;

	private String asdf;

	public MemoryConnection() {
		String[] parts = Thread.currentThread().getStackTrace()[2]
				.getClassName().split("\\.");
		asdf = parts[parts.length - 1];
	}

	public MemoryConnection(MemoryConnection partner) {
		String[] parts = Thread.currentThread().getStackTrace()[2]
				.getClassName().split("\\.");
		asdf = parts[parts.length - 1];
		pairWith(partner);
	}

	public final MemoryConnection getPartner() {
		return partner;
	}

	public synchronized final void pairWith(MemoryConnection partner) {
		System.out.println("Paired " + asdf + " with " + partner.asdf);
		setPartner(partner, true);
		if(partner != null)
			partner.setPartner(this, false);
	}

	private final void setPartner(MemoryConnection partner, boolean establish) {
		if(this.partner != null)
			this.partner.setPartner(null, false);
		System.out.println(asdf + " - Establishing: " + establish);
		if(partner != null) {
			if(establish) {
				in = new PipedInputStream();
				out = new PipedOutputStream();
			} else {
				try {
					in = new PipedInputStream(partner.out);
					out = new PipedOutputStream(partner.in);
				} catch(IOException exception) {
					clearPipes();
					exception.printStackTrace();
					return;
				}
			}
			stream = new IOByteStream(in, out);
		} else {
			clearPipes();
			stream = null;
		}
		this.partner = partner;
	}

	private void clearPipes() {
		try {
			if(in != null)
				in.close();
		} catch(IOException exception) {}
		try {
			if(out != null)
				out.close();
		} catch(IOException exception) {}

		in = null;
		out = null;
	}

	public final ByteStream getStream() {
		return stream;
	}
}
