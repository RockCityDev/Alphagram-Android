package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 获取地址下的所有交易记录
 */
public class PolygonTransactionsApi implements IRequestApi, IRequestHost {

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
    private String action = "txlist";
    private String address;
    private String startblock = "0";
    private String endblock = "99999999";
    private int page;
    private String offset = "10";
    private String sort = "desc";
    private String apikey = MMKVUtil.getSystemMsg().polygon_api_key;

    public PolygonTransactionsApi setAddress(String address) {
        this.address = address;
        return this;
    }

    public PolygonTransactionsApi setPage(int page) {
        this.page = page;
        return this;
    }
}
