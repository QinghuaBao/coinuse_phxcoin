package address;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by bcotm on 2017/6/19.
 */
public class AddressTest {
    public static String PUK = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAExKNwuuzNslu0CRvc7LE1DMwxhcoxmMzxLFOm4lT15tiFVGjQ9BOO5js1RR4A9VTBJhbuiS29XgWUW9zpqjE1Gw==";
    public static String PRK = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQggiqAyqsUdvGYTNCqzD1W5Q8NXEKq15MPXCNRdmpMaWegBwYFK4EEAAqhRANCAATEo3C67M2yW7QJ" +
            "G9zssTUMzDGFyjGYzPEsU6biVPXm2IVUaND0E47mOzVFHgD1VMEmFu6JLb1eBZRb3OmqMTUb";
    public static void main(String[] args) {
//        try {
//            /*****************************************************
//             *  Getting an address is so damn easy.              *
//             *****************************************************
//             */
//            Address addr = AddressUtil.generatAddress();
//            System.out.println("address : " + addr.getAddress());
//            System.out.println("publiceKye base64 : " + addr.getPublicKeyBase64());
//            System.out.println("privatekey base64 : " + addr.getPrivateKeyBase64());
//            System.out.println("public :" + addr.getPublicKeyHex());
//        } catch (NoSuchAlgorithmException |NoSuchProviderException |InvalidKeySpecException |InvalidAlgorithmParameterException |IOException e) {
//            e.printStackTrace();
//        }
        String src = "bqh";
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(PUK));
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decode(PRK));
            KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
            PublicKey publicKey = fact.generatePublic(x509EncodedKeySpec);
            PrivateKey privateKey = fact.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initSign(privateKey);
            signature.update(src.getBytes());
            byte[] res = signature.sign();
            System.out.println("hh");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }
}
