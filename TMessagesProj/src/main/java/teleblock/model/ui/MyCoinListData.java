package teleblock.model.ui;

import java.math.BigDecimal;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：我的货币列表数据实体类
 */
public class MyCoinListData {

    private String symbol;
    private String icon;
    private int iconRes;
    private BigDecimal price;
    private BigDecimal balance;
    private int decimal;
    private boolean is_main_currency;
    private String contractAddress;

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setIs_main_currency(boolean is_main_currency) {
        this.is_main_currency = is_main_currency;
    }

    public boolean isIs_main_currency() {
        return is_main_currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
}
