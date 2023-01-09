package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/10/8
 * Author:Perry
 * Description：转账 上报
 */
public class AddTransferRecordApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/web3/transfer";
    }

    private String payment_tg_user_id;
    private String payment_account;

    private String receipt_tg_user_id;
    private String receipt_account;

    private long chain_id;
    private String chain_name;

    private long currency_id;
    private String currency_name;

    private String amount;
    private String tx_hash;

    public AddTransferRecordApi setPayment_tg_user_id(String payment_tg_user_id) {
        this.payment_tg_user_id = payment_tg_user_id;
        return this;
    }

    public AddTransferRecordApi setPayment_account(String payment_account) {
        this.payment_account = payment_account;
        return this;
    }

    public AddTransferRecordApi setReceipt_tg_user_id(String receipt_tg_user_id) {
        this.receipt_tg_user_id = receipt_tg_user_id;
        return this;
    }

    public AddTransferRecordApi setReceipt_account(String receipt_account) {
        this.receipt_account = receipt_account;
        return this;
    }

    public AddTransferRecordApi setChain_id(long chain_id) {
        this.chain_id = chain_id;
        return this;
    }

    public AddTransferRecordApi setChain_name(String chain_name) {
        this.chain_name = chain_name;
        return this;
    }

    public AddTransferRecordApi setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
        return this;
    }

    public AddTransferRecordApi setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public AddTransferRecordApi setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
        return this;
    }

    public AddTransferRecordApi setCurrency_id(long currency_id) {
        this.currency_id = currency_id;
        return this;
    }
}