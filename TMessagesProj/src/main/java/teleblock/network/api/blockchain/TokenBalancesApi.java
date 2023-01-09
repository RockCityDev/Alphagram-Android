package teleblock.network.api.blockchain;

import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpRename;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 获取代币余额列表
 * <p>
 * https://api.zapper.fi/v2/apps/tokens/balances?addresses%5B%5D=0xdF1Cc8163f61B6F7648b8250d5E916A8837c44A2&network=ethereum
 */
public class TokenBalancesApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.zapper.fi";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/v2/apps/tokens/balances";
    }

    @HttpRename("addresses[]")
    private String addresses; // 地址
    private String network; // 网络

    public TokenBalancesApi setAddresses(String addresses) {
        this.addresses = addresses;
        return this;
    }

    /**
     * 根据主币获取网络
     */
    public TokenBalancesApi setNetwork(String symbol) {
        if ("ETH".equalsIgnoreCase(symbol)) {
            this.network = "ethereum";
        } else if ("MATIC".equalsIgnoreCase(symbol)) {
            this.network = "polygon";
        }else if ("ROSE".equalsIgnoreCase(symbol)) {
            this.network = "oasis";
        }
        return this;
    }
}
