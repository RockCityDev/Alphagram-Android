package teleblock.model.wallet;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

import teleblock.util.WalletUtil;

/**
 * 创建日期：2022/8/23
 * 描述：
 */
public class TokenBalance {

    public String symbol;
    public String address;
    public int decimals;
    public String image;
    public int imageRes;
    public double price;
    public String balance;
    public String balanceRaw;
    public double balanceUSD;


    public static List<TokenBalance> parse(Balances balances) {
        List<TokenBalance> tokenBalances = new ArrayList<>();
        TokenBalance tokenBalance = new TokenBalance();
        if (!CollectionUtils.isEmpty(balances.products)) {
            Balances.ProductsEntity product = balances.products.get(0);
            for (Balances.ProductsEntity.AssetsEntity asset : product.assets) {
                tokenBalance = new TokenBalance();
                tokenBalance.symbol = asset.symbol;
                tokenBalance.address = asset.address;
                tokenBalance.decimals = asset.decimals;
                if (!CollectionUtils.isEmpty(asset.displayProps.images)) {
                    tokenBalance.image = asset.displayProps.images.get(0);
                }
                tokenBalance.price = asset.price;
                tokenBalance.balance = asset.balance;
                tokenBalance.balanceRaw = asset.balanceRaw;
                tokenBalance.balanceUSD = asset.balanceUSD;
                tokenBalances.add(tokenBalance);
            }
        }
        return tokenBalances;
    }

    public static TokenBalance parse(TTToken ttToken) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = ttToken.getSymbol();
        tokenBalance.address = ttToken.getContractAddress();
        tokenBalance.decimals = ttToken.getDecimals();
        tokenBalance.image = ttToken.getImage();
        tokenBalance.balance = WalletUtil.parseToken(ttToken.getBalance(), ttToken.getDecimals());
        tokenBalance.price = ttToken.getPrice();
        if (!TextUtils.isEmpty(tokenBalance.balance) && tokenBalance.price > 0) {
            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
        }
        return tokenBalance;
    }

    public static TokenBalance parse(OasisToken oasisToken) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = oasisToken.getSymbol();
        tokenBalance.address = oasisToken.getContractAddress();
        tokenBalance.decimals = oasisToken.getDecimals();
        tokenBalance.image = oasisToken.getImage();
        tokenBalance.imageRes = oasisToken.getImageRes() == 0 ? R.drawable.token_holder : oasisToken.getImageRes();
        tokenBalance.balance = WalletUtil.parseToken(oasisToken.getBalance(), oasisToken.getDecimals());
        tokenBalance.price = oasisToken.getPrice();
        if (!TextUtils.isEmpty(tokenBalance.balance) && tokenBalance.price > 0) {
            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
        }
        return tokenBalance;
    }
}