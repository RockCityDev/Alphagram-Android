package teleblock.model.wallet;

import android.text.TextUtils;

import java.util.List;

/**
 * 创建日期：2022/8/10
 * 描述：
 */
public class WalletInfo {

    public int id;
    public String tg_user_id;
    public String nft_contract;
    public String nft_contract_image;
    public String nft_token_id;
    public String nft_photo_id;
    public String nft_name;
    public String nft_price;
    public String nft_token_standard;
    public long nft_chain_id;
    public int chain_id;
    public String chain_icon;
    public String chain_name;
    private int is_show_wallet;
    private int is_bind_wallet;
    private List<WalletInfoItem> wallet_info;
    private List<ChainInfoItem> chain_record;

    public void setIs_bind_wallet(int is_bind_wallet) {
        this.is_bind_wallet = is_bind_wallet;
    }

    public int getIs_bind_wallet() {
        return is_bind_wallet;
    }

    public void setIs_show_wallet(int is_show_wallet) {
        this.is_show_wallet = is_show_wallet;
    }

    public int getIs_show_wallet() {
        return is_show_wallet;
    }

    public long getTg_user_id() {
        if (TextUtils.isEmpty(tg_user_id)) {
            tg_user_id = "0";
        }
        return Long.parseLong(tg_user_id);
    }

    public long getNft_photo_id() {
        if (TextUtils.isEmpty(nft_photo_id)) {
            nft_photo_id = "0";
        }
        return Long.parseLong(nft_photo_id);
    }

    public List<ChainInfoItem> getChain_record() {
        return chain_record;
    }

    public void setChain_record(List<ChainInfoItem> chain_record) {
        this.chain_record = chain_record;
    }

    public List<WalletInfoItem> getWallet_info() {
        return wallet_info;
    }

    public void setWallet_info(List<WalletInfoItem> wallet_info) {
        this.wallet_info = wallet_info;
    }

    public class ChainInfoItem {
        public int chain_id;
        public String chain_icon;
        public String chain_name;
    }

    public class WalletInfoItem {
        private int user_id;
        private String wallet_type;
        private String wallet_address;

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getWallet_type() {
            return wallet_type;
        }

        public void setWallet_type(String wallet_type) {
            this.wallet_type = wallet_type;
        }

        public String getWallet_address() {
            return wallet_address;
        }

        public void setWallet_address(String wallet_address) {
            this.wallet_address = wallet_address;
        }
    }
}