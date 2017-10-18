package ecdsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECMultiplier;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;

public class Run {
	// �汾
	public final static int version = 1;
	private static byte[] bytes = null;
	// �㷨��׼
	private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
	// ����
	public static ECDomainParameters CURVE = null;

	public static void main(String[] args)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
		// ��Ӽ���Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH());

		// ���ɹ�Կ
		ECKeyPairGenerator generator = new ECKeyPairGenerator();
		ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, new SecureRandom());
		generator.init(keygenParams);
		AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
		ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
		ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
		BigInteger x = privParams.getD();
		System.out.println("siyao " + new String(Base64.getEncoder().encode(x.toByteArray())));

		byte[] pub = pubParams.getQ().getEncoded(false);
		byte[] scret = CryptUtil.bigIntegerToBytes(x, 32);
		
		System.out.println("私钥");
		CryptUtil.printHexString(scret);
		System.out.println("公钥");
		//pub = Base64.getDecoder().decode("BNf94GVnQ1XNecdDCVNBJhzhtUDGMYwWZrFWjAbTQwJLt8Jk0ye82OonfiaOaBYxQvLqE46sUPV04EOAmRluH1M=");
		CryptUtil.printHexString(pub);

		bytes = pub;

		bytes = CryptUtil.sha256hash160(bytes);
		CryptUtil.printHexString(bytes);

		String result = CryptUtil.toBase58(bytes, version);

		System.out.println("地址");
		System.out.println(result);
	}

}
