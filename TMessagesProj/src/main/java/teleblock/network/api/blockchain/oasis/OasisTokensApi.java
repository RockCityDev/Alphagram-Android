package teleblock.network.api.blockchain.oasis;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 获取地址拥有的代币列表。
 */
public class OasisTokensApi implements IRequestHost, IRequestApi {

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

    private String module = "account";
    private String action = "tokenlist";
    private String address;

    public OasisTokensApi setAddress(String address) {
        this.address = address;
        return this;
    }
}
