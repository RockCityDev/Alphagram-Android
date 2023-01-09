package teleblock.network.api.blockchain.ethereum;

import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpIgnore;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 获取单个NFT
 */
public class EthSingleNftApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.opensea.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/api/v1/asset/" + asset_contract_address + "/" + token_id + "/";
    }

    @HttpIgnore
    private String asset_contract_address; // 此 NFT 的合约地址
    @HttpIgnore
    private String token_id; // 此项目的令牌 ID
    private String account_address; // 代币所有者的地址
    private boolean include_orders; //确定是否应在响应中包含订单信息的标志

    public EthSingleNftApi setAsset_contract_address(String asset_contract_address) {
        this.asset_contract_address = asset_contract_address;
        return this;
    }

    public EthSingleNftApi setToken_id(String token_id) {
        this.token_id = token_id;
        return this;
    }

    public EthSingleNftApi setAccount_address(String account_address) {
        this.account_address = account_address;
        return this;
    }

    public EthSingleNftApi setInclude_orders(boolean include_orders) {
        this.include_orders = include_orders;
        return this;
    }
}
