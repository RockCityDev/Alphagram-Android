package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

/**
 * 绑定钱包
 */
public class BindWalletApi implements IRequestApi {

    private String wallet_type;
    private String wallet_address;
    private long chain_id;

    @Override
    public String getApi() {
        return "/user/bind/wallet";
    }


    public BindWalletApi setWallet_type(String wallet_type) {
        this.wallet_type = wallet_type;
        return this;
    }

    public BindWalletApi setWallet_address(String wallet_address) {
        this.wallet_address = wallet_address;
        return this;
    }

    public BindWalletApi setChain_id(long chain_id) {
        this.chain_id = chain_id;
        return this;
    }
}
