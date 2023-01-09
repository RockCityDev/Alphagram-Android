package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import org.web3j.utils.Convert;

import teleblock.blockchain.Web3TransactionUtils;
import teleblock.network.api.blockchain.polygon.PolygonGasLimitApi;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：ethlimite
 */
public class EthGasLimitApi implements IRequestApi, IRequestHost {
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

    private String module = "proxy";
    private String action = "eth_estimateGas";
    private String apikey = MMKVUtil.getSystemMsg().eth_api_key;

    private String data;
    private String to;

    public EthGasLimitApi setData(String data) {
        this.data = data;
        return this;
    }

    public EthGasLimitApi setTo(String to) {
        this.to = to;
        return this;
    }
}
