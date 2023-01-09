package teleblock.model.ui;

/**
 * Time:2022/9/19
 * Author:Perry
 * Description：选择gas费用数据
 */
public class SelectorGasFeeData {

    private String title;
    private String price;
    private boolean isSelector;

    public SelectorGasFeeData(String title, String price, boolean isSelector) {
        this.title = title;
        this.price = price;
        this.isSelector = isSelector;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public boolean isSelector() {
        return isSelector;
    }

    public void setSelector(boolean selector) {
        isSelector = selector;
    }
}
