package teleblock.model.wallet;

/**
 * 创建日期：2022/10/19
 * 描述：
 */
public class OasisToken {

    private String contractAddress;
    private String name;
    private String symbol;
    private String decimals;
    private String balance;
    private String type;
    private double price;
    private String image;
    private int imageRes;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimals() {
        return decimals == null ? 0 : Integer.parseInt(decimals);
    }

    public void setDecimals(int decimals) {
        this.decimals = String.valueOf(decimals);
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public static NFTInfo parse(OasisToken oasisToken) {
        NFTInfo nftInfo = new NFTInfo();
        nftInfo.asset_name = oasisToken.getName();
        nftInfo.contract_address = oasisToken.getContractAddress();
        nftInfo.symbol = oasisToken.getSymbol();
        nftInfo.token_standard = oasisToken.getType();
        nftInfo.blockchain = "Oasis";
        return nftInfo;
    }
}