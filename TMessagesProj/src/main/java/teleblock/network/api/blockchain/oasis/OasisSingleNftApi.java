package teleblock.network.api.blockchain.oasis;

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
import teleblock.util.JsonUtil;

/**
 * 获取单个NFT
 */
public class OasisSingleNftApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://explorer.emerald.oasis.doorgod.io/api/eth-rpc";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    public static String createJson(NFTInfo nftInfo, String ownerAddress) {
        List<Object> params = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("to", nftInfo.contract_address);
        map.put("data", Web3TransactionUtils.encodeTokensOfOwnerData(ownerAddress));
        params.add(map);
        params.add("latest");
        JsonRpc jsonRpc = new JsonRpc("eth_call", params);
        return JsonUtil.parseObjToJson(jsonRpc);
    }
}


