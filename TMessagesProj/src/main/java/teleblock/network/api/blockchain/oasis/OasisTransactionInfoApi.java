package teleblock.network.api.blockchain.oasis;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 通过交易哈希返回交易的详情
 * <p>
 * https://explorer.emerald.oasis.dev/api-docs#transaction
 */
public class OasisTransactionInfoApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://explorer.emerald.oasis.doorgod.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/api";
    }

    private String module = "transaction";
    private String action = "gettxinfo";
    private String txhash;

    public OasisTransactionInfoApi setTxhash(String txhash) {
        this.txhash = txhash;
        return this;
    }
}
