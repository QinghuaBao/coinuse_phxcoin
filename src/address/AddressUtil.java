package address;

import coinuse.Controller;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class AddressUtil {
    public final static int version = 1;

    public static KeyPair generateECKey()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException{
        Controller.warning("addProvider");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        return keyPair;
    }

    public static Address generatAddress()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
//        Controller.warning("addProvider");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        Controller.warning("generataeECKey");
        KeyPair keyPair = AddressUtil.generateECKey();
//        Controller.warning("getInstance");
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
        PrivateKey privateKeyc = fact.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));
        byte[] pub = publicKey.getEncoded();
        byte[] pri = privateKeyc.getEncoded();
        byte[] bytes;
        bytes = CryptUtil.sha256hash160(pub);
        String result = CryptUtil.toBase58(bytes, version);

        Address addr = new Address();
        addr.setVersion(version);
        addr.setAddress(result);
        addr.setPrivateKey(pri);
        addr.setPublicKey(pub);
        addr.setPrivateKeyBase64(Base64.encode(pri));
        addr.setPublicKeyBase64(Base64.encode(pub));
        addr.setPrivateKeyHex(CryptUtil.printHexString(pri));
        addr.setPublicKeyHex(CryptUtil.printHexString(pub));

        return addr;
    }
}
