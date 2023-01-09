package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * 获取主币余额
 * <p>
 * https://jsapi.apiary.io/apis/viewblock/reference/0/address/get-an-address.html
 */
public class TTBalanceApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.viewblock.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/v1/thundercore/addresses/" + address;
    }

    private String address;

    public TTBalanceApi setAddress(String address) {
        this.address = address;
        return this;
    }
}
