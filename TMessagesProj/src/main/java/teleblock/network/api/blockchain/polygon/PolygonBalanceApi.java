package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 获取polygon链的主币余额
 * <p>
 * https://docs.polygonscan.com/api-endpoints/accounts#get-matic-balance-for-a-single-address
 */
public class PolygonBalanceApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.polygonscan.com/api";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    private String module = "account";
    private String action = "balance";
    private String address;
    private String apikey = MMKVUtil.getSystemMsg().polygon_api_key;

    public PolygonBalanceApi setAddress(String address) {
        this.address = address;
        return this;
    }
}
