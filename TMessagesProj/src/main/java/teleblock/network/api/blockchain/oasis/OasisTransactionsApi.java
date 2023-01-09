package teleblock.network.api.blockchain.oasis;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 获取地址下的所有交易记录
 * <p>
 * https://explorer.emerald.oasis.dev/api-docs#account
 */
public class OasisTransactionsApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://explorer.emerald.oasis.doorgod.io/api";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    private String module = "account";
    private String action = "txlist";
    private String address;
    private int page;
    private String sort = "desc";

    public OasisTransactionsApi setAddress(String address) {
        this.address = address;
        return this;
    }

    public OasisTransactionsApi setPage(int page) {
        this.page = page;
        return this;
    }
}
