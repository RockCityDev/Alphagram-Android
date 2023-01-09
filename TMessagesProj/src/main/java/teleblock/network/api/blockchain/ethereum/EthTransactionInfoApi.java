package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 通过交易哈希返回交易的详情
 * <p>
 * https://docs.etherscan.io/api-endpoints/geth-parity-proxy#eth_gettransactionreceipt
 */
public class EthTransactionInfoApi implements IRequestApi, IRequestHost {
    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    @NonNull
    @Override
    public String getHost() {
        return "https://api.etherscan.io/api";
    }

    private String module = "proxy";
    private String action = "eth_getTransactionReceipt";
    private String apikey = MMKVUtil.getSystemMsg().eth_api_key;
    private String txhash;

    public EthTransactionInfoApi setTxhash(String txhash) {
        this.txhash = txhash;
        return this;
    }
}
