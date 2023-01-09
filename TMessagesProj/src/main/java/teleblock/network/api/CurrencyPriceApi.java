package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/10/21
 * Author:Perry
 * Description：根据coinId获取币的行情
 */
public class CurrencyPriceApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/web3/currency/price";
    }

    private String coin_id;

    public CurrencyPriceApi setCoin_id(String coin_id) {
        this.coin_id = coin_id;
        return this;
    }
}
