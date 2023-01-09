package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

/**
 * 支付成功上报-创建订单
 */
public class OrderPostApi implements IRequestApi {

    private String tx_hash;
    private long group_id;
    private String payment_account;

    @Override
    public String getApi() {
        return "/web3/order/post";
    }

    public OrderPostApi setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
        return this;
    }

    public OrderPostApi setGroup_id(long group_id) {
        this.group_id = group_id;
        return this;
    }

    public OrderPostApi setPayment_account(String payment_account) {
        this.payment_account = payment_account;
        return this;
    }
}
