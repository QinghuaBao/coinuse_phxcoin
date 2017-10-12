package ecdsa;

import java.math.BigInteger;
import java.util.Base64;

import org.junit.Test;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

public class CryptUtil {
	public static void printHexString(byte[] b) {
		System.out.println("base64: " + Base64.getEncoder().encodeToString(b));
		
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase() + "");
		}
		System.out.println("");
	}

	/**
	 * ���� RIPEMD160(SHA256(input)). ���ڵ�ַ������
	 * 
	 */
	public static byte[] sha256hash160(byte[] input) {
		byte[] sha256 = Sha256Hash.hash(input);
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(sha256, 0, sha256.length);
		byte[] out = new byte[20];
		digest.doFinal(out, 0);
		return out;
	}

	public static final String toBase58(byte[] bytes, int version) {
		// ��Ҫ��ԭbyte�ײ����һ���ֽڰ汾��
		// ��β�����byte��������sha256���ɵ�ǰ4���ֽ���ΪУ��λ
		byte[] addressBytes = new byte[1 + bytes.length + 4];
		addressBytes[0] = (byte) version;
		System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
		CryptUtil.printHexString(addressBytes);
		byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
		System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);

		System.out.println("���У��λ��:");
		CryptUtil.printHexString(addressBytes);
		return Base58.encode(addressBytes);
	}

	/**
	 * 16���Ƶ��ַ�����ʾת���ֽ�����
	 * 
	 * @param hexString
	 *            16���Ƹ�ʽ���ַ���
	 * @return ת������ֽ�����
	 **/
	public static byte[] hexStr2ByteArray(String hexString) {
		if (hexString == null)
			throw new IllegalArgumentException("this hexString must not be empty");

		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
			// ��Ϊ��16���ƣ����ֻ��ռ��4λ��ת�����ֽ���Ҫ����16���Ƶ��ַ�����λ����
			// ��hex ת����byte "&" ����Ϊ�˷�ֹ�������Զ���չ
			// hexת����byte ��ʵֻռ����4λ��Ȼ��Ѹ�λ����������λ
			// Ȼ��|������ ����λ ���ܵõ� ���� 16������ת����һ��byte.
			//
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}

	/**
	 * 16�����ַ���ת����byte����
	 * 
	 * @param 16�����ַ���
	 * @return ת�����byte����
	 */
	public static byte[] hex2Byte(String hex) {
		String digital = "0123456789ABCDEF";
		char[] hex2char = hex.toCharArray();
		byte[] bytes = new byte[hex.length() / 2];
		int temp;
		for (int i = 0; i < bytes.length; i++) {
			// ��ʵ������ĺ�����һ���� multiple 16 ��������4λ �����ͳ��˸�4λ��
			// Ȼ��͵���λ��ӣ� �൱�� λ����"|"
			// ��Ӻ������ ���� λ "&" ���� ��ֹ�������Զ���չ. {0xff byte����ʾ��}
			temp = digital.indexOf(hex2char[2 * i]) * 16;
			temp += digital.indexOf(hex2char[2 * i + 1]);
			bytes[i] = (byte) (temp & 0xff);
		}
		return bytes;
	}

	// �����ֱ���Ϊ�ֽ�����
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
	
	// ���ֽ�����תΪbigint
	public static BigInteger bytesToBigInteger(byte[] bitytes) {
		if (bitytes == null) {
			return null;
		}		
		byte[] bytes = new byte[bitytes.length + 1];	
		System.arraycopy(bitytes, 0, bytes, 1, 32);
		return new BigInteger(bytes);
	}	
}
