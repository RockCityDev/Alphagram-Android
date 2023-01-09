package teleblock.model.wallet;

public class TransactionInfo {

    public String blockHash;    //  区块哈希值
    public String blockNumber;  // 区块高度
    public String from;     // 交易发送方
    public String to;   // 交易接收方
    public String gas;  // 交易发起者提供的gas
    public String gasPrice;     // 交易发起者配置的gas价格，单位是wei
    public String hash;     // 交易hash
    public String input;    // 交易附带的数据
    public String nonce;    // 交易的发起者在之前进行过的交易数量
    public String transactionIndex;     // 整数。交易在区块中的序号
    public String value;       // 交易附带的货币量，单位为Wei。
    public String timestamp;    // 时间戳
    public String contractAddress;      // 合约地址
    public String tokenSymbol;      // 代币符号
    public int tokenDecimal;    // 代币精度
    public boolean isError = false; //是否失败
    public String functionName; // 方法名称


}
