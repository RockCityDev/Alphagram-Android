package teleblock.model.wallet;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import teleblock.util.JsonUtil;

/**
 * 创建日期：2022/10/8
 * 描述：
 */
public class TTTransactions {

    public String hash;
    public String blockHeight;
    public String from;
    public String to;
    public String value;
    public String fee;
    public String timestamp;
    public String direction;

    public static List<TransactionInfo> parse(String result) {
        List<TransactionInfo> list = new ArrayList<>();
        try {
            List<TTTransactions> ttTransactionsList = JsonUtil.parseJsonToList(result, TTTransactions.class);
            if (result == null) return list;
            for (TTTransactions info : ttTransactionsList) {
                TransactionInfo transaction = new TransactionInfo();
                transaction.hash = info.hash;
                transaction.from = info.from;
                transaction.to = info.to;
                transaction.blockNumber = info.blockHeight;
                transaction.timestamp = (Long.parseLong(info.timestamp) / 1000) + "";
                transaction.value = info.value;
                list.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}