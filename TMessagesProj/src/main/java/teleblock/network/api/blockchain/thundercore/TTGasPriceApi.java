package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.JsonRpc;
import teleblock.util.JsonUtil;

/**
 * 返回以 wei 为单位的每 gas 的当前价格
 */
public class TTGasPriceApi implements IRequestApi, IRequestHost {

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

    public static String createJson() {
        List<Object> params = new ArrayList<>();
        JsonRpc jsonRpc = new JsonRpc("eth_gasPrice", params);
        return JsonUtil.parseObjToJson(jsonRpc);
    }
}


