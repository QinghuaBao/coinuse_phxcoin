package ecdsa;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

import org.junit.Test;

import ecdsa.ECKey.Signature;

public class ECKeyTest {
	@Test
	public void testNormal() {
//		ECKey key = new ECKey(
//				new BigInteger("15669280622064402590813018920111567599171487990495490501918129189672877078540"));
		ECKey key = ECKey.generateECKey(new SecureRandom());
		String mymessage = "test message";
		byte[] hashTwice = Sha256Hash.hashTwice(mymessage.getBytes());

		String dsdsdsignedMessage = key.signMessage(hashTwice);
		System.out.println("ans: " + dsdsdsignedMessage);

		Signature sign = key.sign(hashTwice);
		
		System.out.println("r is " + sign.r.toString());
		System.out.println("s is " + sign.s.toString());

		String xString = Base64.getEncoder().encodeToString(sign.Serialize());
		
		System.out.println(xString);
		
		String pubKey = Base64.getEncoder().encodeToString(key.pub);
		System.out.println(pubKey);
	}
}
