package address;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class AddressUtil {
    public final static int version = 1;

    public static final ECDomainParameters CURVE;
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    static {
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
    }

    public static AsymmetricCipherKeyPair generateECKey()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException{
        //Controller.warning("addProvider");
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
//        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
//        keyGen.initialize(ecSpec, new SecureRandom());
//        KeyPair keyPair = keyGen.generateKeyPair();
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    public static Address generatAddress()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
//        Controller.warning("addProvider");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        Controller.warning("generataeECKey");
        AsymmetricCipherKeyPair keyPair = AddressUtil.generateECKey();
//        Controller.warning("getInstance");
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters)keyPair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keyPair.getPublic();
        BigInteger x = privateKeyParameters.getD();

        byte[] pub = pubParams.getQ().getEncoded(false);
        byte[] scret = ecdsa.CryptUtil.bigIntegerToBytes(x, 32);

        System.out.println("私钥");
        ecdsa.CryptUtil.printHexString(scret);
        System.out.println("公钥");
        ecdsa.CryptUtil.printHexString(pub);

        byte[] bytes = null;
        bytes = pub;

        bytes = ecdsa.CryptUtil.sha256hash160(bytes);
        //ecdsa.CryptUtil.printHexString(bytes);

        String result = ecdsa.CryptUtil.toBase58(bytes, version);

        System.out.println("地址");
        System.out.println(result);
//
//        KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
//        PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));
//        PrivateKey privateKeyc = fact.generatePrivate(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()));
//        byte[] pub = publicKey.getEncoded();
//        byte[] pri = privateKeyc.getEncoded();
//        byte[] bytes;
//        bytes = CryptUtil.sha256hash160(pub);
//        String result = CryptUtil.toBase58(bytes, version);

        byte[] pri = x.toByteArray();

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
