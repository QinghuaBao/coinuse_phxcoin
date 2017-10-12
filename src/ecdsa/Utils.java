package ecdsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import com.google.common.base.Charsets;

import static com.google.common.base.Preconditions.checkArgument;

public class Utils {

	public static final BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
	private static final MessageDigest digest;

	static {
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // Can't happen.
		}
	}

	public static long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	final protected static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

	public static String bytesToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars).toUpperCase(Locale.US);
	}

	public static byte[] sha256hash160(byte[] input) {
		try {
			byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
			RIPEMD160Digest digest = new RIPEMD160Digest();
			digest.update(sha256, 0, sha256.length);
			byte[] out = new byte[20];
			digest.doFinal(out, 0);
			return out;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // Cannot happen.
		}
	}

	public static String toAddress(byte[] pubKeyHash) {
		checkArgument(pubKeyHash.length == 20, "Addresses are 160-bit hashes, " + "so you must provide 20 bytes");

		int version = 0;
		checkArgument(version < 256 && version >= 0);

		byte[] addressBytes = new byte[1 + pubKeyHash.length + 4];
		addressBytes[0] = (byte) version;
		System.arraycopy(pubKeyHash, 0, addressBytes, 1, pubKeyHash.length);
		byte[] check = Utils.doubleDigest(addressBytes, 0, pubKeyHash.length + 1);
		System.arraycopy(check, 0, addressBytes, pubKeyHash.length + 1, 4);
		return Base58.encode(addressBytes);
	}

	public static byte[] doubleDigest(byte[] input) {
		return doubleDigest(input, 0, input.length);
	}

	/**
	 * Calculates the SHA-256 hash of the given byte range, and then hashes the
	 * resulting hash again. This is standard procedure in Bitcoin. The resulting
	 * hash is in big endian form.
	 */
	public static byte[] doubleDigest(byte[] input, int offset, int length) {
		synchronized (digest) {
			digest.reset();
			digest.update(input, offset, length);
			byte[] first = digest.digest();
			return digest.digest(first);
		}
	}

	public static byte[] formatMessageForSigning(String message) {
		return message.getBytes();
//		try {
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
//			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
//			byte[] messageBytes = message.getBytes(Charsets.UTF_8);
//			VarInt size = new VarInt(messageBytes.length);
//			bos.write(size.encode());
//			bos.write(messageBytes);
//			return bos.toByteArray();
//		} catch (IOException e) {
//			throw new RuntimeException(e); // Cannot happen.
//		}
	}

	public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
	public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES = BITCOIN_SIGNED_MESSAGE_HEADER
			.getBytes(Charsets.UTF_8);

	public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
		if (b == null) {
			return null;
		}
		byte[] bytes = new byte[numBytes];
		byte[] biBytes = b.toByteArray();
		int start = (biBytes.length == numBytes + 1) ? 1 : 0;
		int length = Math.min(biBytes.length, numBytes);
		System.arraycopy(biBytes, start, bytes, numBytes - length, length);
		return bytes;
	}
}
