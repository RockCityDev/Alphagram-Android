package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：ethgas费查询
 */
public class EthGasfeeApi implements IRequestApi, IRequestHost {

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

    private String module = "gastracker";
    private String action = "gasoracle";
    private String apikey = MMKVUtil.getSystemMsg().eth_api_key;
}
