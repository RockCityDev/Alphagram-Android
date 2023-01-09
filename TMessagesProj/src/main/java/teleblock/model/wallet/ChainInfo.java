package teleblock.model.wallet;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2022/8/30
 * 描述：
 */
public class ChainInfo {

    public String chainId;
    public String chainName;
    public NativeCurrencyEntity nativeCurrency;
    public List<String> rpcUrls;
    public List<String> blockExplorerUrls;

    public static class NativeCurrencyEntity {
        public String name;
        public String symbol;
        public int decimals = 18;

        public NativeCurrencyEntity(String symbol) {
            this.symbol = symbol;
        }
    }

    public ChainInfo(String chainId, String chainName, String symbol, String rpcUrl) {
        this.chainId = Numeric.toHexStringWithPrefix(new BigDecimal(chainId).toBigInteger());
        if (!"ETH".equals(symbol)) {
            this.chainName = chainName;
            this.nativeCurrency = new NativeCurrencyEntity(symbol);
            List<String> rpcUrls = new ArrayList<>();
            rpcUrls.add(rpcUrl);
            this.rpcUrls = rpcUrls;
        }
    }
}