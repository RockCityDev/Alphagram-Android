package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

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
 * 获取所有NFT
 */
public class TTNftsApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://tokenmanager.thundercore.com/json-rpc";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    public static String createJson(String address) {
        List<Object> params = new ArrayList<>();
        params.add(address);
        JsonRpc jsonRpc = new JsonRpc("tokenManager.NftOf", params);
        return JsonUtil.parseObjToJson(jsonRpc);
    }
}


