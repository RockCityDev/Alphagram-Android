package teleblock.model;

/**
 * 创建日期：2022/8/25
 * 描述：
 */
public class OrderResultEntity {

    public int id;
    public String order_no;
    public int group_id;
    public int user_id;
    public int status;
    public int expire;
    public int amount;
    public ShipEntity ship;
    public String wallet_network;
    public String currency_type;
    public String tx_hash;
    public String payment_account;
    public String created_at;
    public String updated_at;

    public static class ShipEntity {
        public String url;
    }
}