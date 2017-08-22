package address;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.math.BigInteger;

/**
 * Created by bqh on 2017/4/7.
 * <p>
 * E-mail:M201672845@hust.edu.cn
 */
public class CryptUtil {
    public static String printHexString(byte[] b) {
        String pubstr = "";
        for (int i = 0; i < b.length; i++) {

            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            pubstr += hex.toUpperCase();
        }
        return pubstr;
    }

    /**
     * 计算 RIPEMD160(SHA256(input)). 用于地址的生成
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
        // 需要将原byte首部添加一个字节版本号
        // 在尾部添加byte进行两次sha256生成的前4个字节作为校验位
        byte[] addressBytes = new byte[1 + bytes.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        CryptUtil.printHexString(addressBytes);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);

        //System.out.println("添加校验位后:");
        CryptUtil.printHexString(addressBytes);
        return Base58.encode(addressBytes);
    }

    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString
     *            16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] hexStr2ByteArray(String hexString) {
        if (hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            // 将hex 转换成byte "&" 操作为了防止负数的自动扩展
            // hex转换成byte 其实只占用了4位，然后把高位进行右移四位
            // 然后“|”操作 低四位 就能得到 两个 16进制数转换成一个byte.
            //
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 16进制字符串转换成byte数组
     *
     * @param  hex 16进制字符串
     * @return 转换后的byte数组
     */
    public static byte[] hex2Byte(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            // 其实和上面的函数是一样的 multiple 16 就是右移4位 这样就成了高4位了
            // 然后和低四位相加， 相当于 位操作"|"
            // 相加后的数字 进行 位 "&" 操作 防止负数的自动扩展. {0xff byte最大表示数}
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xff);
        }
        return bytes;
    }

    // 将数字编码为字节数组
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
}
