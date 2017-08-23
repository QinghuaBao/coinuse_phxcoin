package coin;

import address.Sha256Hash;
import coinuse.Controller;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import invoke.Invoke;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONObject;
import protos.Foamcoin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


/**
 * Created by bcotm on 2017/6/19.
 */
public class CoinFactory {

//    private static Logger logger = Logger.getLogger(CoinFactory.class);

    public PhxCoinInterface getCoinInstance() {
        return new Coin();
    }

    private static final class Coin implements PhxCoinInterface {
        private String ADD_URL;
        private String returnJson = "";
        private String sendJson = "";

//        public String ADD_URL;
        /**
         *  @param fromAddr
         *         转出方账户地址
         *  @param toAddr
         *         转入方地址
         *  @param value
         *         转账金额
         *  @param founder
         *         发起人
         *  @param until
         *         该笔交易的款项冻结时间,是持续多少时间,一般默认为-1,即无冻结时间
         *  @param pubstring,pristring
         *         用户公钥和私钥
         */
        // public static final String ADD_URL =
        // "http://192.168.65.129:7050/chaincode";//改为随机选择链上节点ip
        public String getTransferParam(String fromAddr, String toAddr, long value, String founder, long until,
                                       String pubstring, String pristring) throws IOException {
//            getAddressUrl();
            String[] param = { fromAddr };
            String res = postRequest("query", "coinbase", 1, "query_addrs", param);
			/* wzt修改7/12----如果查询出错,则返回null */
            if (res.contains("\"error\"")) {
                Controller.warning("获得账户[" + fromAddr + "]失败，无法签名！");
                return "";
            }
            String invokeParam = generateTransferInvokeParam(res, toAddr, value, founder, until, pubstring, pristring);
            return invokeParam;
        }

        public String getTxHash(String param) throws IOException {
            byte[] txBytes = Base64.decode(param);
            byte[] fhash = Sha256Hash.hash(txBytes);
            byte[] lhash = Sha256Hash.hash(fhash);
            return ByteUtils.toHexString(lhash);
        }

        public String doTransferByParam(String param) throws IOException {
//            getAddressUrl();
            String[] para = { param };
            String res = postRequest("invoke", "coinbase", 3, "invoke_transfer", para);
            return res;
        }

        public String doCoinbase(String addr, String pubKey, long value, long until)throws IOException{
            Foamcoin.TX.TXOUT.Builder txout = Foamcoin.TX.TXOUT.newBuilder();
            txout.setAddr(addr);
            txout.setUntil(until);
            txout.setValue(value);
            txout.setScriptPubKey(pubKey);
            txout.setUndefined("");

            Foamcoin.TX.Builder builder = Foamcoin.TX.newBuilder();
            builder.setFounder("foam_java_pc");
            builder.setTimestamp(System.currentTimeMillis());
            builder.setVersion(1);
            builder.addTxout(txout);

            String base64Data = Base64.encode(builder.build().toByteArray());

            String[] param = { base64Data };
            String res = postRequest("invoke", "coinbase", 3, "invoke_coinbase", param);
            return res;
        }

        public String getResParam(String addr) throws IOException {
//            getAddressUrl();
            String[] param = { addr };
            String res = postRequest("query", "coinbase", 1, "query_addrs", param);
            return res;
        }

        public String getAccountInfo(String addr) throws IOException {
//            getAddressUrl();
            String[] param = { addr };
            String res = postRequest("query", "coinbase", 1, "query_addrs", param);
			/* wzt修改7/6----如果查询出错,则返回null */
            if (res.contains("\"error\"")) {
                Controller.warning("获得账户[" + addr + "]失败！" + res);
                return "";
            }
            JSONObject js = genertateAccountInfo(res);
            return js.toString();
        }

        public String getPhxCoinInfo() throws IOException{
            String[] param = {};
            String res = postRequest("query", "coinbase", 1, "query_coin", param);
            if (res.contains("\"error\"")) {
                Controller.warning("获得凤币信息失败！" + res);
                return "";
            }
            JSONObject jsonObject = generateCoinInfo(res);
            return jsonObject.toString();
        }

        /* 7/25新增3个函数,获取余额,txout数量,获取指定数目的txout */
        public Long getAccountBalance(String addr) throws IOException {
//            getAddressUrl();
            String[] param = { addr };
            long balance = -1L;
            long ret = 0L;
            String res = postRequest("query", "coinbase", 1, "query_balance", param);
            if (res.contains("\"error\"")) {
                Controller.warning("获得账户[" + addr + "]余额失败！" + res);
                return 0L;
            }
            JSONObject jsonObject = new JSONObject(res);
            JSONObject result = jsonObject.getJSONObject("result");
            String message = result.getString("message");
            String decode = new String(Base64.decode(message));
            ret = Long.parseLong(decode);
//                logger.debug("余额数量:" + balance);
            //ret = Long.parseLong(String.valueOf(balance));

            return ret;
        }

        public int getTxoutCount(String addr) throws IOException {
//            getAddressUrl();
            String[] param = { addr };
            int ret = 0;
            try {
                String res = postRequest("query", "coinbase", 1, "query_txout_count", param);
                if (res.contains("\"error\"")) {
                    Controller.warning("获得账户[" + addr + "]失败！\n" + res);
                    return 0;
                }
                JSONObject jsonObject = new JSONObject(res);
                JSONObject result = jsonObject.getJSONObject("result");
                String message = result.getString("message");
                String decode = new String(Base64.decode(message));
                ret = Integer.parseInt(decode);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            } finally {
                return ret;
            }
        }

        public String getAccountTxout(String addr, int num) throws IOException {
//            getAddressUrl();
            String[] param = { addr, "" + num };
            String res = postRequest("query", "coinbase", 1, "query_addrs_txout", param);
            return res;
        }

        public String getTransaction(String txHash) throws IOException {
//            getAddressUrl();
            String[] param = { txHash };
            String res = postRequest("query", "coinbase", 1, "query_tx", param);
			/* wzt修改7/6----如果查询出错,则返回null */
            if (res.contains("\"error\"")) {
                Controller.warning("获得交易记录[" + txHash + "]失败！\n" + res);
                return "";
            }
            JSONObject js = generateTransactionInfo(res);
            return js.toString();
        }

        /**
         * @Description: generate account information
         * @param: arg
         *             is the return value of postRequest to query address
         * @return: JSONObject 修改,增加try/catch错误捕获 这里是一个可以优化的点,减少APP端接收的数据量
         */
        public JSONObject genertateAccountInfo(String res) throws InvalidProtocolBufferException {

            JSONObject jsonObject = new JSONObject(res);
            byte[] byteData;
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                String base64ByteDate = result.getString("message");
                byteData = Base64.decode(base64ByteDate);
            } catch (Exception e) {

                return jsonObject;
                // TODO: handle exception
            }
            JSONObject resAccounts = new JSONObject();

            Foamcoin.QueryAddrResults queryAddrResults = Foamcoin.QueryAddrResults.parseFrom(byteData);
            Map<String, Foamcoin.TX.TXOUT> txoutMap = new HashMap<>();
            for (Map.Entry<String, Foamcoin.Account> queryAccount : queryAddrResults.getAccountsMap().entrySet()) {
                JSONObject tmpAccount = new JSONObject();
                tmpAccount.put("address", queryAccount.getValue().getAddr());
                tmpAccount.put("balance", queryAccount.getValue().getBalance());
                tmpAccount.put("txouts_count", queryAccount.getValue().getTxoutsCount());

                txoutMap = queryAccount.getValue().getTxoutsMap();
                for (Map.Entry<String, Foamcoin.TX.TXOUT> txout : txoutMap.entrySet()) {
                    JSONObject tmpTxout = new JSONObject();
                    tmpTxout.put("key", txout.getKey());// 需要在protobuf里面加入key的数据结构才能解析，key是txhash+txout_index
                    tmpTxout.put("value", txout.getValue().getValue());
                    tmpTxout.put("addr", txout.getValue().getAddr());
                    tmpTxout.put("until", txout.getValue().getUntil());
                    tmpTxout.put("scriptPubKey", txout.getValue().getScriptPubKey());
                    tmpTxout.put("undefined", txout.getValue().getUndefined());

                    tmpAccount.append("txouts", tmpTxout);
                }
                resAccounts.put(queryAccount.getKey(), tmpAccount);
            }
            return resAccounts;
        }

        public JSONObject generateTransactionInfo(String res) throws InvalidProtocolBufferException {
            JSONObject jsonObject = new JSONObject(res);
            JSONObject result = jsonObject.getJSONObject("result");
            String base64ByteDate = result.getString("message");
            byte[] byteData = Base64.decode(base64ByteDate);

            Foamcoin.TX queryTxResult = Foamcoin.TX.parseFrom(byteData);

            JSONObject tmpTx = new JSONObject();
            tmpTx.put("founder", queryTxResult.getFounder());
            tmpTx.put("version", queryTxResult.getVersion());
            tmpTx.put("timestamp", queryTxResult.getTimestamp());

            List<Foamcoin.TX.TXOUT> txoutList = queryTxResult.getTxoutList();

            for (Foamcoin.TX.TXOUT txout : txoutList) {
                JSONObject tmpTxout = new JSONObject();
                tmpTxout.put("value", txout.getValue());
                tmpTxout.put("addr", txout.getAddr());
                tmpTxout.put("until", txout.getUntil());
                tmpTxout.put("scriptPubKey", txout.getScriptPubKey());
                tmpTxout.put("undefined", txout.getUndefined());
                tmpTx.append("txouts", tmpTxout);
            }
            List<Foamcoin.TX.TXIN> txinList = queryTxResult.getTxinList();
            for (Foamcoin.TX.TXIN txin : txinList) {
                JSONObject tmpTxin = new JSONObject();
                tmpTxin.put("index", txin.getIx());
                tmpTxin.put("sourceHash", txin.getSourceHash());
                tmpTxin.put("addr", txin.getAddr());
                tmpTxin.put("script", txin.getScript());
                tmpTxin.put("undefined", txin.getUndefined());
                tmpTx.append("txins", tmpTxin);
            }
            return tmpTx;
        }

        public JSONObject generateCoinInfo(String res) throws InvalidProtocolBufferException{
            JSONObject jsonObject = new JSONObject(res);
            JSONObject result = jsonObject.getJSONObject("result");
            String data = result.getString("message");

            Foamcoin.HydruscoinInfo coinInfo = Foamcoin.HydruscoinInfo.parseFrom(data.getBytes());

            JSONObject temp = new JSONObject();
            temp.put("coinTotal", coinInfo.getCoinTotal());
            temp.put("accountTotal", coinInfo.getAccountTotal());
            temp.put("txoutTotal", coinInfo.getTxoutTotal());
            temp.put("txTotal", coinInfo.getTxTotal());
            temp.put("placeHolder", coinInfo.getPlaceholder());
            return temp;
        }

        /**
         * @description: generate a string which th invoke method need
         * @param: res
         *             query result, pick up the txouts of account des the
         *             address which you where transfer value the money you will
         *             transfer
         * @return: invoke param
         */

        @Override
        public String generateTransferInvokeParam(String res, String des, long value, String founder, long until,
                                                  String pubstring, String pristring) throws InvalidProtocolBufferException {
            JSONObject jsonObject = new JSONObject(res);
            JSONObject result = jsonObject.getJSONObject("result");
            String base64ByteDate = result.getString("message");
            byte[] byteData = Base64.decode(base64ByteDate);

            Foamcoin.QueryAddrResults queryAddrResults = Foamcoin.QueryAddrResults.parseFrom(byteData);
            Map<String, Foamcoin.TX.TXOUT> txoutMap = new HashMap<>();
            for (Map.Entry<String, Foamcoin.Account> queryAccount : queryAddrResults.getAccountsMap().entrySet()) {
                txoutMap = queryAccount.getValue().getTxoutsMap();
            }

            String param = null;
            try {
                // Invoke String
                param = new String(Invoke.invoke(txoutMap, des, value, founder, until, pubstring, pristring));
            } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException
                    | InvalidKeyException e) {
                e.printStackTrace();
            }

            return param;
        }

        /**
         * @description: connect to the blockchain
         * @param:
         * @return: the connection
         */

        public HttpURLConnection connectToBlockChain() throws IOException {
            // connect to blockchain
            URL url = new URL("http://" + ADD_URL+"/chaincode");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            return connection;
        }

        public void setADD_URL(String ADD_URL) {
            this.ADD_URL = ADD_URL;
        }

        /**
         * @description: coin the request to blockchain
         * @param: method
         *             query or invoke chaincodeName the chaincode name which
         *             you deploy id query(5), invoke(3)
         * @return: the respose of blockchain
         */

        public String postRequest(String method, String chaincodeName, int id, String function, String[] param)
                throws IOException {

            HttpURLConnection connection = connectToBlockChain();
            connection.connect();

            // POST Request
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            ArrayList<String> ctorMsg = new ArrayList<>();
            ctorMsg.add(function);
            Collections.addAll(ctorMsg, param);
            JSONObject jsonObject = generateJson(method, chaincodeName, ctorMsg.toArray(new String[ctorMsg.size()]),
                    id);
            sendJson = jsonObject.toString();

            out.writeBytes(jsonObject.toString());
            out.flush();
            out.close();

            // Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            StringBuilder stringBuilder = new StringBuilder("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                stringBuilder.append(lines);
            }
            reader.close();
            // disconnect
            connection.disconnect();
            returnJson = stringBuilder.toString();
            return stringBuilder.toString();
        }

        /**
         * @description: generate json data like { "jsonrpc": "2.0", "method":
         *               "invoke", "params": { "type": 1, "chaincodeID":{
         *               "name":"mycc" }, "ctorMsg": { "args":["invoke", "a",
         *               "b", "10"] } }, "id": 3 }
         * @param: method
         *             query or invoke chaincodeName the chaincode name which
         *             you deploy args the param of chaincode id query is 5,
         *             invoke is 3
         * @return: the json object which will send to blockchain
         */

        public JSONObject generateJson(String method, String chaincodeName, String[] args, int id) {
            JSONObject jsonObject = new JSONObject();
            JSONObject paramsJson = new JSONObject();

            JSONObject chaincodeIDJson = new JSONObject();
            chaincodeIDJson.put("name", chaincodeName);
            JSONObject ctorMsgJson = new JSONObject();
            ctorMsgJson.put("args", args);

            paramsJson.put("type", 1);
            paramsJson.put("chaincodeID", chaincodeIDJson);
            paramsJson.put("ctorMsg", ctorMsgJson);

            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("method", method);
            jsonObject.put("params", paramsJson);
            jsonObject.put("id", id);

            return jsonObject;
        }

        public String getSendJson() {
            return sendJson;
        }

        public String getReturnJson() {
            return returnJson;
        }
    }
}

