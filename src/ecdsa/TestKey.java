package ecdsa;

import java.awt.RenderingHints.Key;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.symmetric.AES.KeyGen;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class TestKey {

	// �㷨��׼
	private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
	public static ECDomainParameters CURVE = null;

	@Test
	public void testGetPrivateKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
			InvalidKeyException, SignatureException, IOException {
		// add provider BC
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH());
		byte[] bit = "22a47fa09a223f2aa079edf85a7c2d4f8720ee63e502ee2869afab7de234b80c".getBytes();

		byte[] decodeBit = Hex.decode(bit);

		System.out.println("base58:" + Base58.encode(decodeBit));
		ECParameterSpec ecParameterSpec = new ECParameterSpec(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(),
				CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
		ECPrivateKeySpec spc = new ECPrivateKeySpec(CryptUtil.bytesToBigInteger(decodeBit), ecParameterSpec);

		BigInteger integer = CryptUtil.bytesToBigInteger(decodeBit);
		System.out.println(integer.toString());

		KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
		PrivateKey privateKey = fact.generatePrivate(spc);

		ECPrivateKey key = (ECPrivateKey) privateKey;
		System.out.println(key.getS().toString());

		CryptUtil.printHexString(CryptUtil.bigIntegerToBytes(key.getS(), 32));
		System.out.println();

		String string = "test message";
		byte[] formatMessageForSigning = formatMessageForSigning(string);
		byte[] hashTwice = Sha256Hash.hashTwice(formatMessageForSigning);

		CryptUtil.printHexString(hashTwice);

		ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
		ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(key.getS(), CURVE);
		signer.init(true, privKey);
		BigInteger[] components = signer.generateSignature(hashTwice);
		final ECDSASignature signature = new ECDSASignature(components[0], components[1]);
		signature.ensureCanonical();
		byte[] encodeToDER = signature.encodeToDER();

		System.out.println("answer:");

		CryptUtil.printHexString(encodeToDER);
		// ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new
		// SHA256Digest()));
		// ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(key.getS(),
		// CURVE);
		// signer.init(true, privKey);
		// BigInteger[] components = signer.generateSignature(hashTwice);
		//
		// byte[] byteArray = derByteStream(components[0], components[1]).toByteArray();
		//
		// System.out.println("\n");
		//
		// CryptUtil.printHexString(byteArray);

		// ECDSASignature xx = new ECDSASignature(components[0],
		// components[1]).toCanonicalised();

	}

	public static ByteArrayOutputStream derByteStream(BigInteger r, BigInteger s) throws IOException {
		// Usually 70-72 bytes.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
		DERSequenceGenerator seq = new DERSequenceGenerator(bos);
		seq.addObject(new ASN1Integer(r));
		seq.addObject(new ASN1Integer(s));
		seq.close();
		return bos;
	}

	public static byte[] formatMessageForSigning(String message) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
			bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
			byte[] messageBytes = message.getBytes();
			VarInt size = new VarInt(messageBytes.length);
			bos.write(size.encode());
			bos.write(messageBytes);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e); // Cannot happen.
		}
	}

	public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
	public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES = BITCOIN_SIGNED_MESSAGE_HEADER.getBytes();

	public static class ECDSASignature {
		/**
		 * The two components of the signature.
		 */
		public BigInteger r, s;

		/**
		 * Constructs a signature with the given components. Does NOT automatically
		 * canonicalise the signature.
		 */
		public ECDSASignature(BigInteger r, BigInteger s) {
			this.r = r;
			this.s = s;
		}

		/**
		 * Will automatically adjust the S component to be less than or equal to half
		 * the curve order, if necessary. This is required because for every signature
		 * (r,s) the signature (r, -s (mod N)) is a valid signature of the same message.
		 * However, we dislike the ability to modify the bits of a Bitcoin transaction
		 * after it's been signed, as that violates various assumed invariants. Thus in
		 * future only one of those forms will be considered legal and the other will be
		 * banned.
		 */
		public void ensureCanonical() {
			if (s.compareTo(HALF_CURVE_ORDER) > 0) {
				// The order of the curve is the number of valid points that exist on that
				// curve. If S is in the upper
				// half of the number of valid points, then bring it back to the lower half.
				// Otherwise, imagine that
				// N = 10
				// s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
				// 10 - 8 == 2, giving us always the latter solution, which is canonical.
				s = CURVE.getN().subtract(s);
			}
		}

		/**
		 * DER is an international standard for serializing data structures which is
		 * widely used in cryptography. It's somewhat like protocol buffers but less
		 * convenient. This method returns a standard DER encoding of the signature, as
		 * recognized by OpenSSL and other libraries.
		 */
		public byte[] encodeToDER() {
			try {
				return derByteStream().toByteArray();
			} catch (IOException e) {
				throw new RuntimeException(e); // Cannot happen.
			}
		}

		public static ECDSASignature decodeFromDER(byte[] bytes) {
			try {
				ASN1InputStream decoder = new ASN1InputStream(bytes);
				DLSequence seq = (DLSequence) decoder.readObject();
				ASN1Integer r, s;
				try {
					r = (ASN1Integer) seq.getObjectAt(0);
					s = (ASN1Integer) seq.getObjectAt(1);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException(e);
				}
				decoder.close();
				// OpenSSL deviates from the DER spec by interpreting these values as unsigned,
				// though they should not be
				// Thus, we always use the positive versions. See:
				// http://r6.ca/blog/20111119T211504Z.html
				return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		protected ByteArrayOutputStream derByteStream() throws IOException {
			// Usually 70-72 bytes.
			ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
			DERSequenceGenerator seq = new DERSequenceGenerator(bos);
			seq.addObject(new ASN1Integer(r));
			seq.addObject(new ASN1Integer(s));
			seq.close();
			return bos;
		}

		static {
			// Tell Bouncy Castle to precompute data that's needed during secp256k1
			// calculations. Increasing the width
			// number makes calculations faster, but at a cost of extra memory usage and
			// with decreasing returns. 12 was
			// picked after consulting with the BC team.
			FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
			CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
					CURVE_PARAMS.getH());
			HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);

		}

		public static final BigInteger HALF_CURVE_ORDER;
	}
}
