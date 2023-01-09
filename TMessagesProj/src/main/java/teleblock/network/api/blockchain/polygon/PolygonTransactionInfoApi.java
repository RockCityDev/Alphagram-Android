package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 通过交易哈希返回交易的详情
 * <p>
 * https://docs.polygonscan.com/api-endpoints/geth-parity-proxy#eth_gettransactionreceipt
 */
public class PolygonTransactionInfoApi implements IRequestApi, IRequestHost {
    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    @NonNull
    @Override
    public String getHost() {
        return "https://api.polygonscan.com/api";
    }

    private String module = "proxy";
    private String action = "eth_getTransactionReceipt";
    private String apikey = MMKVUtil.getSystemMsg().polygon_api_key;
    private String txhash;

    public PolygonTransactionInfoApi setTxhash(String txhash) {
        this.txhash = txhash;
        return this;
    }
}
