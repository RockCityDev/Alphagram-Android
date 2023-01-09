package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.blockchain.Web3TransactionUtils;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.TTToken;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;

/**
 * 获取代币余额
 */
public class TTTokensApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://mainnet-rpc.thundercore.com";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    public static String createJson(String address) {
        List<JsonRpc> jsonRpcList = new ArrayList<>();
        // 主币参数
        List<Object> params = new ArrayList<>();
        params.add(address);
        params.add("latest");
        jsonRpcList.add(new JsonRpc("eth_getBalance", params));
        // 代币参数
        List<TTToken> tokenList = MMKVUtil.getTTTokens();
        Map<String, String> map = new HashMap<>();
        for (TTToken ttToken : tokenList) {
            params = new ArrayList<>();
            map = new HashMap<>();
            map.put("to", ttToken.getContractAddress());
            map.put("data", Web3TransactionUtils.encodeBalanceOfData(address));
            params.add(map);
            params.add("latest");
            jsonRpcList.add(new JsonRpc("eth_call", params));
        }
        return JsonUtil.parseObjToJson(jsonRpcList);
    }
}


