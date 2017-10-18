package ecdsa;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

import org.junit.Test;

import ecdsa.ECKey.Signature;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECMultiplier;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.util.encoders.Hex;

public class ECKeyTest {
	@Test
	public void testNormal() {
//		ECKey key = new ECKey(
//				new BigInteger("15669280622064402590813018920111567599171487990495490501918129189672877078540"));
		//ECKey key = ECKey.generateECKey(new SecureRandom());
		ECKey key = new ECKey(new BigInteger(org.spongycastle.util.encoders.Base64.decode("AOV/46LSV6DaXy5wvy8SXt6ddgjEkqVH/CTNGdihQuMp")));

		String mymessage = "test message";
		byte[] hashTwice = Sha256Hash.hashTwice(mymessage.getBytes());

		hashTwice = Base64.getDecoder().decode("p+oHmfsvPXoDeEoqJUSO34FKgQbG+8+8ycH3lQ76hVo=");
		String dsdsdsignedMessage = key.signMessage(hashTwice);
		System.out.println("ans: " + dsdsdsignedMessage);

		Signature sign = key.sign(hashTwice);
		
		System.out.println("r is " + sign.r.toString());
		System.out.println("s is " + sign.s.toString());

		String xString = Base64.getEncoder().encodeToString(sign.Serialize());
		xString = new String(Hex.encode(sign.Serialize()));
		System.out.println(xString);
		
		String pubKey = Base64.getEncoder().encodeToString(pubkeyFromBigInteger(key.priv));
		System.out.println(pubKey);
	}

	private static byte[] pubkeyFromBigInteger(BigInteger bigInteger){
		X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
		ECPrivateKeyParameters privParams = new ECPrivateKeyParameters(bigInteger, new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH()));

		ECMultiplier ecMultiplier = new FixedPointCombMultiplier();
		ECPoint Q = ecMultiplier.multiply(privParams.getParameters().getG(), bigInteger);
		ECPublicKeyParameters pubParams = new ECPublicKeyParameters(Q, privParams.getParameters());
		byte[] pub = pubParams.getQ().getEncoded(false);
		return pub;
	}
}
