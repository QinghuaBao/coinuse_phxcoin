package invoke;


import address.Sha256Hash;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import ecdsa.ECKey;
import org.bouncycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECMultiplier;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.util.encoders.Base64;
import protos.Foamcoin;
import sun.security.provider.DSAPrivateKey;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Map;

/**
 * Created by chenqx303 on 17/2/23.
 */

public class Invoke {
    public final static int version = 1;

    public static byte[] invoke(Map<String, Foamcoin.TX.TXOUT> txouts, String des, long value, String founder, long until,
                                String pubstring, String pristring)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException{
        //add provider BC
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");

        byte[] pri = pristring.getBytes();
        byte[] pub = pubstring.getBytes();
        ECKey key = new ECKey(new BigInteger(Base64.decode(pri)));
//        PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(Base64.decode(pri));
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(pub));
//        PrivateKey privateKey = fact.generatePrivate(priKeySpec);
//        PublicKey publicKey = fact.generatePublic(x509EncodedKeySpec);

//        Signature signature = Signature.getInstance("SHA1withECDSA");

        // 测试验证公私钥
//        signature.initSign(privateKey);
//        signature.update("bqh".getBytes());
//        System.out.println("bqh" + HexBin.encode(signature.sign()));
//
//        signature.initVerify(publicKey);
//        signature.update("bqh".getBytes());
//        boolean flag = signature.verify(HexBin.decode("3046022100B8811C3E5115ED6CC4D9095EFE848E3C612537F72658FEB" +
//                "46D36373E986C3077022100EBA3313DD8831EFE60CFB870CE97B7229037DA1099BA978BFEFB4C6BDC4174F2"));
//        System.out.println("验证: " + flag);

        // construct TX
        Foamcoin.TX.Builder tx = Foamcoin.TX.newBuilder();
        tx.setVersion(version);
        tx.setTimestamp(System.currentTimeMillis());
        tx.setFounder(founder);
        double countAmount=0;
        //construct txin, sender's txout must not be null
        for(Map.Entry<String, Foamcoin.TX.TXOUT> txout:txouts.entrySet()){
            Foamcoin.TX.TXIN.Builder builder = Foamcoin.TX.TXIN.newBuilder();
            builder.setAddr(txout.getValue().getAddr());
            builder.setSourceHash(txout.getKey().split(":")[0]);
            builder.setIx(Integer.parseInt(txout.getKey().split(":")[1]));
            tx.addTxin(builder.build());
            if(txout.getValue().getUntil()==-1){//这里需要判断这个值是否是-1，如果为其他就不可用
                countAmount+=txout.getValue().getValue();//获得每个零钱的金额，需要逐个累加知道大于需要转账的金额。
                if(countAmount>=value){
                    break;
                }
            }
        }

        Foamcoin.TX.TXOUT.Builder txout = Foamcoin.TX.TXOUT.newBuilder();
        txout.setValue(value);
        txout.setAddr(des);
        txout.setScriptPubKey(new String(Base64.encode(pubkeyFromBigInteger(key.getPriv()))));
        txout.setUntil(until);
        txout.setUndefined("");

        tx.addTxout(txout);

        byte[] txData = transfer((int)tx.getVersion(), tx.getTxinList(), tx.getTxoutList(), tx.getFounder(), key);
        return txData;

    }

    public static byte[] transfer(int version, List<Foamcoin.TX.TXIN> txins, List<Foamcoin.TX.TXOUT> txouts, String founder, ECKey key) {
        Foamcoin.TX.Builder coinBaseTransfer = Foamcoin.TX.newBuilder();
        coinBaseTransfer.setFounder(founder);
        coinBaseTransfer.setVersion(version);
        coinBaseTransfer.addAllTxin(txins);
        coinBaseTransfer.addAllTxout(txouts);
        coinBaseTransfer.setTimestamp(System.currentTimeMillis());

        byte[] script = new byte[0];
        try {
            script = newsignScript(key, coinBaseTransfer.build());
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException  e) {
            e.printStackTrace();
        }
        coinBaseTransfer.clearTxin();
        Foamcoin.TX.TXIN.Builder txin;
        for (int i = 0; i < txins.size(); i++) {
            txin = txins.get(i).toBuilder();
            txin.setScript(new String(Base64.encode(script)));
            coinBaseTransfer.addTxin(txin.build());
        }
        return Base64.encode(coinBaseTransfer.build().toByteArray());

    }

    public static byte[] newsignScript(ECKey key, Foamcoin.TX coinBaseTransfer)
            throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] txHashBytes = txHash(coinBaseTransfer);
        String temp = new String(Base64.encode(txHashBytes));
//        System.out.println(txHashBytes.toString());
        //txHashBytes = java.util.Base64.getDecoder().decode("xSXBmQVjIQ6FUkcR4C1eTR+G5WzFojeN4+JKKvMHo24=");
        ECKey.Signature signature = key.sign(txHashBytes);
        //String xString = java.util.Base64.getEncoder().encodeToString(signature.Serialize());
        return signature.Serialize();
    }

    public static byte[] signScript(PrivateKey privateKey, Foamcoin.TX coinBaseTransfer)
            throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] txHashBytes = txHash(coinBaseTransfer);
        ASN1Integer r = null, s = null;
        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initSign(privateKey);
        dsa.update(txHashBytes);
        /*
         * Now that all the data to be signed has been read in, generate a
         * signature for it
         */
        byte[] realSig = dsa.sign();
//        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));
        return realSig;
    }


    private static byte[] txHash(Foamcoin.TX tx) {
        byte[] txBytes = tx.toByteArray();
        byte[] fhash = Sha256Hash.hash(txBytes);
        byte[] lhash = Sha256Hash.hash(fhash);
        return lhash;
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
