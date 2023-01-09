package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：eth链下面的主币余额
 */
public class EthBalanceApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "http://api.etherscan.io/api";
    }

    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    private String module = "account";
    private String action = "balance";
    private String address;
    private String tag = "latest";
    private String apikey = MMKVUtil.getSystemMsg().eth_api_key;

    public EthBalanceApi setAddress(String address) {
        this.address = address;
        return this;
    }
}
