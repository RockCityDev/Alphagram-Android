package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.blockchain.Web3TransactionUtils;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.TTToken;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;

/**
 * 获取单个NFT
 */
public class TTSingleNftApi implements IRequestApi, IRequestHost {

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

    public static String createJson(NFTInfo nftInfo) {
        List<Object> params = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("to", nftInfo.contract_address);
        if (nftInfo.token_standard.endsWith("721")) {
            map.put("data", Web3TransactionUtils.encodeTokenURIData(new BigDecimal(nftInfo.token_id).toBigInteger()));
        } else {
            map.put("data", Web3TransactionUtils.encodeUriData(new BigDecimal(nftInfo.token_id).toBigInteger()));
        }
        params.add(map);
        params.add("latest");
        JsonRpc jsonRpc = new JsonRpc("eth_call", params);
        return JsonUtil.parseObjToJson(jsonRpc);
    }
}


