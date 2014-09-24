package me.axiometry.tanks.server.io.protocol;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import com.sun.image.codec.jpeg.*;

public abstract class StreamUtils {
	public static Image readImage(ByteStream stream) throws IOException {
		int imageSize = readInt(stream);

		byte[] imageBytes = new byte[imageSize];
		for(int i = 0; i < imageSize; i++)
			imageBytes[i] = stream.read();

		ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(is);

		return decoder.decodeAsBufferedImage();
	}

	public static void writeImage(Image image, ByteStream stream)
			throws IOException {
		BufferedImage bi = new BufferedImage(image.getWidth(null),
				image.getHeight(null), BufferedImage.TYPE_INT_RGB);

		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, null);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
		encoder.encode(bi);
		byte[] byteArray = os.toByteArray();

		writeInt(byteArray.length, stream);
		for(byte b : byteArray)
			stream.write(b);
	}

	public static String readString(ByteStream stream) throws IOException {
		int length = readShort(stream);
		byte[] string = new byte[length];
		for(int i = 0; i < length; i++)
			string[i] = stream.read();
		return new String(string);
	}

	public static void writeString(String string, ByteStream stream)
			throws IOException {
		writeShort((short) string.length(), stream);
		for(byte b : string.getBytes())
			stream.write(b);
	}

	public static boolean readBoolean(ByteStream stream) throws IOException {
		return stream.read() == 0 ? true : false;
	}

	public static void writeBoolean(boolean b, ByteStream stream)
			throws IOException {
		stream.write((byte) (b ? 0 : 1));
	}

	public static short readShort(ByteStream stream) throws IOException {
		return (short) wideRead(stream, 2);
	}

	public static void writeShort(short value, ByteStream stream)
			throws IOException {
		wideWrite(stream, value, 2);
	}

	public static char readChar(ByteStream stream) throws IOException {
		return (char) wideRead(stream, 2);
	}

	public static void writeChar(char value, ByteStream stream)
			throws IOException {
		wideWrite(stream, value, 2);
	}

	public static int readInt(ByteStream stream) throws IOException {
		return (int) wideRead(stream, 4);
	}

	public static void writeInt(int value, ByteStream stream)
			throws IOException {
		wideWrite(stream, value, 4);
	}

	public static float readFloat(ByteStream stream) throws IOException {
		return Float.intBitsToFloat((int) wideRead(stream, 4));
	}

	public static void writeFloat(float value, ByteStream stream)
			throws IOException {
		wideWrite(stream, Float.floatToRawIntBits(value), 4);
	}

	public static long readLong(ByteStream stream) throws IOException {
		return wideRead(stream, 8);
	}

	public static void writeLong(long value, ByteStream stream)
			throws IOException {
		wideWrite(stream, value, 8);
	}

	public static double readDouble(ByteStream stream) throws IOException {
		return Double.longBitsToDouble(wideRead(stream, 8));
	}

	public static void writeDouble(double value, ByteStream stream)
			throws IOException {
		wideWrite(stream, Double.doubleToRawLongBits(value), 8);
	}

	private static void wideWrite(ByteStream stream, long value, int byteCount)
			throws IOException {
		for(int i = 0; i < byteCount; i++) {
			int offset = i * 8;
			byte b = (byte) ((value >> offset) & 0xff);
			stream.write(b);
		}
	}

	private static long wideRead(ByteStream stream, int byteCount)
			throws IOException {
		long value = 0;
		for(int i = 0; i < byteCount; i++) {
			int offset = i * 8;
			byte b = stream.read();
			value |= (long) (0xff & b) << offset;
		}
		return value;
	}
}
