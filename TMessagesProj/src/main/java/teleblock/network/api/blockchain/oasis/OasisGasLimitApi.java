package teleblock.network.api.blockchain.oasis;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.model.wallet.JsonRpc;
import teleblock.util.JsonUtil;

/**
 * 生成并返回允许交易完成所需的气体估计值
 */
public class OasisGasLimitApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://emerald.oasis.dev";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    public static String createJson(String to, String data) {
        List<Object> params = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("to", to);
        map.put("data", data);
        params.add(map);
        JsonRpc jsonRpc = new JsonRpc("eth_estimateGas", params);
        return JsonUtil.parseObjToJson(jsonRpc);
    }
}


