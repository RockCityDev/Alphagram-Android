package teleblock.model.wallet;

import com.blankj.utilcode.util.CollectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import teleblock.util.ParseJsonUtil;

/**
 * 创建日期：2022/8/24
 * 描述：
 */
public class Balances {

    public List<ProductsEntity> products;
    public List<MetaEntity> meta;
    public double value;

    public static class ProductsEntity {
        public String label;
        public List<AssetsEntity> assets;
        public List<?> meta;

        public static class AssetsEntity {
            public String type;
            public String appId;
            public String groupId;
            public int supply;
            public List<?> pricePerShare;
            public List<?> tokens;
            public String network;
            public double price;
            public String address;
            public int decimals;
            public String symbol;
            public DataPropsEntity dataProps;
            public DisplayPropsEntity displayProps;
            public String balance;
            public String balanceRaw;
            public double balanceUSD;

            public static class DataPropsEntity {
            }

            public static class DisplayPropsEntity {
                public String label;
                public List<String> images;
                public List<?> statsItems;
            }
        }
    }

    public static class MetaEntity {
        public String label;
        public double value;
        public String type;
    }


    public static Balances parse(String json, String address) {
        Balances balances = null;
        try {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(json);
            JsonElement jsonElement1 = jsonElement.getAsJsonObject().get("balances");
            balances = ParseJsonUtil.getObjectFromJson(jsonElement1.getAsJsonObject().get(address), Balances.class);
            if (balances != null) {
                MetaEntity meta = CollectionUtils.find(balances.meta, item -> "Total".equals(item.label));
                if (meta != null) {
                    balances.value = meta.value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balances;
    }
}