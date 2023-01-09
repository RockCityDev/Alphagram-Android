package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

/**
 * 支付结果查询
 */
public class OrderResultApi implements IRequestApi {

    private String tx_hash;

    @Override
    public String getApi() {
        return "/web3/order/result";
    }

    public OrderResultApi setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
        return this;
    }

}
