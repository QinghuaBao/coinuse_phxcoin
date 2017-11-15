package coin;

import org.json.JSONObject;
import protos.Foamcoin;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wzhutian on 2017/11/4.
 * redis存有一张索引号队列表, 一堆回收站k-v,一张数据Map表
 * 索引号队列表保存"txoutQueue" + fromAddr - [txoutKey],获取txout请求从这个表中拿号,rpop弹出
 * 回收站保存"txout" + txoutKey - fromAddr,所有txout都放入回收站,一旦pop,则设置30秒过期(3个块的时间)
 * 数据表保存{fromAddr,[txoutKey,jsonobject]}
 * PS: 索引号列表解决数据访问问题,回收站k-v解决数据更新问题
 */
public class RedisCache {
    private Jedis jedis;

    RedisCache() {
        jedis = new Jedis("localhost");
    }

    public boolean isConnected() {
        try {
            jedis.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 从获取足量的txout
     *
     * @param fromAddr   转出方地址
     * @param txoutValue 转账金额
     * @return 包含所有txout的Map
     */
    public Map<String, Foamcoin.TX.TXOUT> getTxout(String fromAddr, long txoutValue) {
        Map<String, Foamcoin.TX.TXOUT> txoutMap = new HashMap<>();
        txoutMap = getTxoutFromRedis(fromAddr, txoutValue, txoutMap, 0);
        return txoutMap;
    }

    /**
     * 根据现有的钱数,增加txout
     *
     * @param fromAddr   转出方地址
     * @param txoutValue 转账总需金额数
     * @param txoutMap   已经有了一些txout的Map
     * @param haveValue  已经有的金额数
     * @return 包含所有txout的Map
     */
    public Map<String, Foamcoin.TX.TXOUT> getTxoutFromRedis(String fromAddr, long txoutValue,
                                                            Map<String, Foamcoin.TX.TXOUT> txoutMap, long haveValue) {
        long countAmount = haveValue;//收集到的总额
        long length = jedis.llen("txoutQueue" + fromAddr);//最大循环次数
        for (int i = 0; i < length; i++) {
            String key = jedis.rpop("txoutQueue" + fromAddr);
            if (key == null) {//redis中的txout索引队列空了
                break;
            }
            jedis.expire("txout" + key, 30);//设置30秒后丢弃key
            String jsonStr = jedis.hget(fromAddr, key);
            jedis.hdel(fromAddr, key);//阅后即焚
            JSONObject jsonObject = new JSONObject(jsonStr);
            long value = Long.parseLong(jsonObject.getString("value"));
            String addr = jsonObject.getString("addr");
            long until = Long.parseLong(jsonObject.getString("until"));
            String scriptPubKey = jsonObject.getString("scriptPubKey");
            Foamcoin.TX.TXOUT.Builder builder = Foamcoin.TX.TXOUT.newBuilder();
            builder.setValue(value);
            builder.setAddr(addr);
            builder.setUntil(until);
            builder.setScriptPubKey(scriptPubKey);
            txoutMap.put(key, builder.build());
            if (until == -1) {//这里需要判断这个值是否是-1，如果为其他就不可用
                countAmount += value;//获得每个零钱的金额，需要逐个累加知道大于需要转账的金额。
                if (countAmount >= txoutValue) {
                    break;
                }
            }
        }
        return txoutMap;
    }

    /**
     * 保存txout到redis中
     *
     * @param fromAddr 转出方地址
     * @param txoutMap 从区块链获取到的txoutMap
     */
    public void saveTxout(String fromAddr, Map<String, Foamcoin.TX.TXOUT> txoutMap) {
        Map<String, String> dataMap = new HashMap<>();
        for (Map.Entry<String, Foamcoin.TX.TXOUT> txout : txoutMap.entrySet()) {
            String key = txout.getKey();
            //判断回收站与中是否有该Key
            if (jedis.exists("txout" + key)) {
                continue;
            } else {//没有则立即加入
                jedis.set("txout" + key, fromAddr);
            }
            long value = txout.getValue().getValue();
            String addr = txout.getValue().getAddr();
            long until = txout.getValue().getUntil();
            String scriptPubKey = txout.getValue().getScriptPubKey();
            JSONObject json = new JSONObject();
            json.put("value", value + "");
            json.put("addr", addr);
            json.put("until", until + "");
            json.put("scriptPubKey", scriptPubKey);
            json.put("fromAddr", fromAddr);
            dataMap.put(key, json.toString());
            jedis.lpush("txoutQueue" + fromAddr, key);
        }
        if (dataMap.size() != 0) {
            jedis.hmset(fromAddr, dataMap);//设置数据表
        }
    }
}
