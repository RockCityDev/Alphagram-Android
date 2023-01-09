package teleblock.network.api.blockchain.thundercore;

import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpIgnore;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 获取地址下的所有交易记录
 * <p>
 * https://jsapi.apiary.io/apis/viewblock/reference/0/address-transactions/get-address-transactions.html
 */
public class TTTransactionsApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.viewblock.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/v1/thundercore/addresses/" + address + "/txs";
    }

    @HttpIgnore
    private String address;
    private int page;

    public TTTransactionsApi setAddress(String address) {
        this.address = address;
        return this;
    }

    public TTTransactionsApi setPage(int page) {
        this.page = page;
        return this;
    }
}
