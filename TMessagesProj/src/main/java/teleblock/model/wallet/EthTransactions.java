package teleblock.model.wallet;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class EthTransactions {

    public String status;
    public String message;
    public List<ResultEntity> result;

    public class ResultEntity {

        public String blockNumber;
        public String timeStamp;
        public String hash;
        public String nonce;
        public String blockHash;
        public String transactionIndex;
        public String from;
        public String to;
        public String value;
        public String gas;
        public String gasPrice;
        public String isError;
        public String txreceipt_status;
        public String input;
        public String contractAddress;
        public String cumulativeGasUsed;
        public String gasUsed;
        public String confirmations;
        public String tokenSymbol;
        public int tokenDecimal;
    }

    public static List<TransactionInfo> parse(String result) {
        List<TransactionInfo> list = new ArrayList<>();
        try {
            EthTransactions ethTransactions = new Gson().fromJson(result, EthTransactions.class);
            if (result == null) return list;
            for (ResultEntity info : ethTransactions.result) {
                TransactionInfo transaction = new TransactionInfo();
                transaction.hash = info.hash;
                transaction.blockHash = null == info.blockHash ? "" : info.blockHash;
                transaction.from = info.from;
                transaction.to = info.to;
                transaction.blockNumber = info.blockNumber;
                transaction.timestamp = info.timeStamp;
                transaction.value = info.value;
                transaction.gasPrice = info.gasPrice;
                transaction.gas = info.gas;
                transaction.contractAddress = info.contractAddress;
                transaction.tokenSymbol = info.tokenSymbol;
                transaction.tokenDecimal = info.tokenDecimal;
                transaction.isError = !"0".equals(info.isError);
                list.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
