package address;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by bcotm on 2017/6/19.
 */
public class AddressTest {
    public static void main(String[] args) {
        try {
            /*****************************************************
             *  Getting an address is so damn easy.              *
             *****************************************************
             */
            Address addr = AddressUtil.generatAddress();
            System.out.println("address : " + addr.getAddress());
            System.out.println("publiceKye base64 : " + addr.getPublicKeyBase64());
            System.out.println("privatekey base64 : " + addr.getPrivateKeyBase64());
            System.out.println("public :" + addr.getPublicKeyHex());
        } catch (NoSuchAlgorithmException |NoSuchProviderException |InvalidKeySpecException |InvalidAlgorithmParameterException |IOException e) {
            e.printStackTrace();
        }
    }
}
