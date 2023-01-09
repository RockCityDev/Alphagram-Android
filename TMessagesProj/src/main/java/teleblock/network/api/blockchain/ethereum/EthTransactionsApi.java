package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 获取地址下的所有交易记录
 */
public class EthTransactionsApi implements IRequestApi, IRequestHost {

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
    private String action = "txlist";
    private String address;
    private String startblock = "0";
    private String endblock = "99999999";
    private int page;
    private String offset = "10";
    private String sort = "desc";
    private String apikey = MMKVUtil.getSystemMsg().eth_api_key;

    public EthTransactionsApi setAddress(String address) {
        this.address = address;
        return this;
    }

    public EthTransactionsApi setPage(int page) {
        this.page = page;
        return this;
    }
}
