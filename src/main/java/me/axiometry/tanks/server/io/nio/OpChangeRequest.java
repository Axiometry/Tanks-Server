package me.axiometry.tanks.server.io.nio;

class OpChangeRequest {
	public enum RequestType {
		REGISTER, CHANGEOPS
	}

	private final NIOClientNetworkManager client;
	private final RequestType type;
	private final int ops;

	OpChangeRequest(NIOClientNetworkManager client, RequestType type, int ops) {
		if(client == null || type == null)
			throw new NullPointerException();
		this.client = client;
		this.type = type;
		this.ops = ops;
	}

	public NIOClientNetworkManager getClient() {
		return client;
	}

	public RequestType getType() {
		return type;
	}

	public int getOps() {
		return ops;
	}
}
