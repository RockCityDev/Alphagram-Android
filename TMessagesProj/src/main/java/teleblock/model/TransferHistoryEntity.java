package teleblock.model;

public class TransferHistoryEntity {
    public int id;
    public String payment_tg_user_id;
    public String payment_account;
    public String receipt_tg_user_id;
    public String receipt_account;
    public String tx_hash;
    public String amount;
    public int chain_id;
    public String chain_name;
    public int currency_id;
    public String currency_name;
    public String created_at;
    public String updated_at;
}
