package teleblock.model.wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2022/10/19
 * 描述：
 */
public class TTTokensPrice {

    private boolean success;
    private DataBean data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<TokenListBean> tokenList;

        public List<TokenListBean> getTokenList() {
            return tokenList == null ? new ArrayList<>() : tokenList;
        }

        public void setTokenList(List<TokenListBean> tokenList) {
            this.tokenList = tokenList;
        }

        public static class TokenListBean {
            private String name;
            private String symbol;
            private double price;
            private String priceDelta24H;
            private String tokenAddress;
            private double tradingVol24H;
            private double totalValueLocked;
            private String website;
            private String blockExplorer;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSymbol() {
                return symbol;
            }

            public void setSymbol(String symbol) {
                this.symbol = symbol;
            }

            public double getPrice() {
                return price;
            }

            public void setPrice(double price) {
                this.price = price;
            }

            public String getPriceDelta24H() {
                return priceDelta24H;
            }

            public void setPriceDelta24H(String priceDelta24H) {
                this.priceDelta24H = priceDelta24H;
            }

            public String getTokenAddress() {
                return tokenAddress;
            }

            public void setTokenAddress(String tokenAddress) {
                this.tokenAddress = tokenAddress;
            }

            public double getTradingVol24H() {
                return tradingVol24H;
            }

            public void setTradingVol24H(double tradingVol24H) {
                this.tradingVol24H = tradingVol24H;
            }

            public double getTotalValueLocked() {
                return totalValueLocked;
            }

            public void setTotalValueLocked(double totalValueLocked) {
                this.totalValueLocked = totalValueLocked;
            }

            public String getWebsite() {
                return website;
            }

            public void setWebsite(String website) {
                this.website = website;
            }

            public String getBlockExplorer() {
                return blockExplorer;
            }

            public void setBlockExplorer(String blockExplorer) {
                this.blockExplorer = blockExplorer;
            }
        }
    }
}