package teleblock.model;

import java.util.List;

/**
 * 创建日期：2022/8/25
 * 描述：群详情数据
 */
public class ShopInfoEntity {

    public int id;
    public long chat_id;
    public String type;
    public String title;
    public String description;
    public String avatar;
    public int creator_id;
    public int ship;
    public int join_type;
    public String receipt_account;
    public int wallet_id;
    public String wallet_name;
    public int chain_id;
    public String chain_name;
    public int token_id;
    public String token_name;
    public String amount;
    public String amount_to_wei;
    public int currency_id;
    public String currency_name;
    public String token_address;
    public Object created_at;
    public Object updated_at;
    public List<OrderInfoEntity> order_info;
    public int decimal;
    public String chat_link;

    public static class OrderInfoEntity {
        public String tx_hash;
        public ShipEntity ship;

        public static class ShipEntity {
            public String url;
        }
    }
}