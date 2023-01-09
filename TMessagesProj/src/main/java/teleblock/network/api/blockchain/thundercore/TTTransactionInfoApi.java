package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 通过交易哈希返回交易的详情
 * <p>
 * https://jsapi.apiary.io/apis/viewblock/reference/0/transaction.html
 */
public class TTTransactionInfoApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.viewblock.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/v1/thundercore/txs/" + hash;
    }

    private String hash;

    public TTTransactionInfoApi setHash(String hash) {
        this.hash = hash;
        return this;
    }
}
