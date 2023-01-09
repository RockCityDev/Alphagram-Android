package teleblock.model.ui;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：选择的捐献价格数据
 */
public class SelectorSponsorPriceData {

    //价格
    private String price;
    //图标
    private String icon;
    private boolean isSelector;

    public SelectorSponsorPriceData(String price, String icon, boolean isSelector) {
        this.price = price;
        this.icon = icon;
        this.isSelector = isSelector;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isSelector() {
        return isSelector;
    }

    public void setSelector(boolean selector) {
        isSelector = selector;
    }
}
