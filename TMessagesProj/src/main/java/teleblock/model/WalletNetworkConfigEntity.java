package teleblock.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Time:2022/8/30
 * Author:Perry
 * Description：钱包网络配置实体类
 */
public class WalletNetworkConfigEntity implements Serializable {

    private List<WalletNetworkConfigEntityItem> walletType;
    private List<WalletNetworkConfigChainType> chainType;
    private List<WalletNetworkConfigEntityItem> tokenType;

    public List<WalletNetworkConfigEntityItem> getWalletType() {
        return walletType == null ? new ArrayList<>() : walletType;
    }

    public void setWalletType(List<WalletNetworkConfigEntityItem> walletType) {
        this.walletType = walletType;
    }

    public List<WalletNetworkConfigEntityItem> getTokenType() {
        return tokenType == null ? new ArrayList<>() : tokenType;
    }

    public void setTokenType(List<WalletNetworkConfigEntityItem> tokenType) {
        this.tokenType = tokenType;
    }

    public List<WalletNetworkConfigChainType> getChainType() {
        return chainType == null ? new ArrayList<>() : chainType;
    }

    public void setChainType(List<WalletNetworkConfigChainType> chainType) {
        this.chainType = chainType;
    }

    public static class WalletNetworkConfigChainType implements Serializable{
        private long id;
        private String name;
        private List<WalletNetworkConfigEntityItem> currency;
        private String icon;
        private String rpc_url;
        private int main_currency_id;
        private String main_currency_name;
        private String explorer_url;
        private List<WalletNetworkConfigChainTypeBtn> button;

        public WalletNetworkConfigChainType(long id, String name, String icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        public void setButton(List<WalletNetworkConfigChainTypeBtn> button) {
            this.button = button;
        }

        public List<WalletNetworkConfigChainTypeBtn> getButton() {
            return button;
        }

        public void setExplorer_url(String explorer_url) {
            this.explorer_url = explorer_url;
        }

        public String getExplorer_url() {
            return explorer_url;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<WalletNetworkConfigEntityItem> getCurrency() {
            return currency;
        }

        public void setCurrency(List<WalletNetworkConfigEntityItem> currency) {
            this.currency = currency;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getRpc_url() {
            return rpc_url;
        }

        public void setRpc_url(String rpc_url) {
            this.rpc_url = rpc_url;
        }

        public int getMain_currency_id() {
            return main_currency_id;
        }

        public void setMain_currency_id(int main_currency_id) {
            this.main_currency_id = main_currency_id;
        }

        public String getMain_currency_name() {
            return main_currency_name;
        }

        public void setMain_currency_name(String main_currency_name) {
            this.main_currency_name = main_currency_name;
        }
    }

    public static class WalletNetworkConfigEntityItem implements Serializable{
        private long id;
        private String name;
        private boolean is_main_currency;
        private String icon;
        private int decimal;
        private String coin_id;

        public WalletNetworkConfigEntityItem(long id, String name,String icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setCoin_id(String coin_id) {
            this.coin_id = coin_id;
        }

        public String getCoin_id() {
            return coin_id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isIs_main_currency() {
            return is_main_currency;
        }

        public void setIs_main_currency(boolean is_main_currency) {
            this.is_main_currency = is_main_currency;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public void setDecimal(int decimal) {
            this.decimal = decimal;
        }

        public int getDecimal() {
            return decimal;
        }
    }

    public static class WalletNetworkConfigChainTypeBtn implements Serializable {
        private int id;
        private int chain_id;
        private String type;
        private String name;
        private String icon;
        private String link;
        private String icon_link;
        private int sort;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getChain_id() {
            return chain_id;
        }

        public void setChain_id(int chain_id) {
            this.chain_id = chain_id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getIcon_link() {
            return icon_link;
        }

        public void setIcon_link(String icon_link) {
            this.icon_link = icon_link;
        }
    }
}
