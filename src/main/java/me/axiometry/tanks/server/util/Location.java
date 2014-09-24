package me.axiometry.tanks.server.util;

public final class Location {
	private final double x, y;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
