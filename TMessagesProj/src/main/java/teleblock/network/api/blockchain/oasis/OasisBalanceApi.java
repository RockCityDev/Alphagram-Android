package teleblock.network.api.blockchain.oasis;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * Time:2022/9/30
 * Author:Perry
 * Description：oasis获取账户余额
 */
public class OasisBalanceApi implements IRequestHost, IRequestApi {

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
    private String action = "balance";
    private String address;

    public OasisBalanceApi setAddress(String address) {
        this.address = address;
        return this;
    }
}
