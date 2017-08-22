package address;

/**
 * Created by bcotm on 2017/6/19.
 */
public class Address {
    private int version;
    private String address;
    private byte[] privateKey;
    private byte[] publicKey;
    private String privateKeyBase64;
    private String publicKeyBase64;
    private String privateKeyHex;
    private String publicKeyHex;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public String getPrivateKeyHex() {
        return privateKeyHex;
    }

    public void setPrivateKeyHex(String privateKeyHex) {
        this.privateKeyHex = privateKeyHex;
    }

    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
}
