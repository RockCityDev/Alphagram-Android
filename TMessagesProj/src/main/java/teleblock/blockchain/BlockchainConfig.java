
package teleblock.blockchain;


import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.R;

import java.util.List;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.util.MMKVUtil;

/**
 * 创建日期：2022/8/29
 * 描述：
 */
public class BlockchainConfig {

    public static final String PKG_META_MASK = "io.metamask";
    public static final String PKG_IMTOKEN = "im.token.app";
    public static final String PKG_TRUST_WALLET = "com.wallet.crypto.trustapp";
    public static final String PKG_TOKEN_POCKET = "vip.mytokenpocket";
    public static final String PKG_SPOT_WALLET = "com.spot.spot";

    /**
     * 钱包 图标
     */
    public enum WalletIconType {
        METAMASK("MetaMask", "MetaMask", PKG_META_MASK, "metamask", R.drawable.logo_meta_mask_connect_wallet),
        IMTOKEN("imToken", "imToken", PKG_IMTOKEN, "im_token", R.drawable.logo_imtoken_wallet),
        TRUSTWALLET("Trust Wallet Android", "Trust Wallet", PKG_TRUST_WALLET, "trust", R.drawable.logo_trust_connect_wallet),
        TOKENPOCKET("TokenPocket", "TokenPocket", PKG_TOKEN_POCKET, "token_pocket", R.drawable.logo_token_pocket_connect_wallet),
        SPOTWALLET("Spot Wallet", "Spot Wallet", PKG_SPOT_WALLET, "spot", R.drawable.logo_spot_connect_wallet);

        private String fullName;
        private String walletName;
        private String pkg;
        private String walletType;
        private int icon;

        WalletIconType(String fullName, String walletName, String pkg, String walletType, int icon) {
            this.fullName = fullName;
            this.walletName = walletName;
            this.pkg = pkg;
            this.walletType = walletType;
            this.icon = icon;
        }
    }

    /**
     * 获取钱包包名
     */
    public static String getPkgByFullName(String fullName) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.fullName.equals(fullName)) {
                return walletIconType.pkg;
            }
        }
        return "";
    }

    /**
     * 获取钱包包名
     */
    public static String getPkgByFullWalletType(String walletType) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.walletType.equals(walletType)) {
                return walletIconType.pkg;
            }
        }
        return "";
    }

    /**
     * 获取钱包图标
     *
     * @param pkg
     * @return
     */
    public static int getWalletIconByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equalsIgnoreCase(pkg)) {
                return walletIconType.icon;
            }
        }
        return 0;
    }

    /**
     * 获取钱包type
     */
    public static String getWalletTypeByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equals(pkg)) {
                return walletIconType.walletType;
            }
        }
        return "";
    }

    /**
     * 获取钱包名称
     */
    public static String getWalletNameByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equals(pkg)) {
                return walletIconType.walletName;
            }
        }
        return "";
    }

    /**
     * 获取区块链配置
     */
    public static WalletNetworkConfigEntity.WalletNetworkConfigChainType getChainType(long chainId) {
        List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> chainTypes = MMKVUtil.getWalletNetworkConfigEntity().getChainType();
        return CollectionUtils.find(chainTypes, item -> chainId == item.getId());
    }

    /**
     * 获取主币配置
     */
    public static WalletNetworkConfigEntity.WalletNetworkConfigEntityItem getMainCurrency(WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType) {
        return CollectionUtils.find(chainType.getCurrency(), item -> item.isIs_main_currency());
    }
}