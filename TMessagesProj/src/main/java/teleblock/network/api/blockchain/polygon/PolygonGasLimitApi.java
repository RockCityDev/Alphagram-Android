package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import org.web3j.utils.Convert;

import teleblock.blockchain.Web3TransactionUtils;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：polygon limite
 */
public class PolygonGasLimitApi implements IRequestApi, IRequestHost {
    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    @NonNull
    @Override
    public String getHost() {
        return "https://api.polygonscan.com/api";
    }

    private String module = "proxy";
    private String action = "eth_estimateGas";
    private String apikey = MMKVUtil.getSystemMsg().polygon_api_key;

    private String data;
    private String to;

    public PolygonGasLimitApi setData(String data) {
        this.data = data;
        return this;
    }

    public PolygonGasLimitApi setTo(String to) {
        this.to = to;
        return this;
    }
}
