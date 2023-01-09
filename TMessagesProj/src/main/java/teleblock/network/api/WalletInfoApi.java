package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

import teleblock.blockchain.BlockchainConfig;
import teleblock.util.MMKVUtil;

/**
 * 根据钱包获取NFT相关数据
 */
public class WalletInfoApi implements IRequestApi {

    private String wallet_address;

    @Override
    public String getApi() {
        return "/user/wallet/info";
    }

    public WalletInfoApi setWallet_address(String wallet_address) {
        this.wallet_address = wallet_address;
        return this;
    }
}
