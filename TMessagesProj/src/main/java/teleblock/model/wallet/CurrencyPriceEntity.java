package teleblock.model.wallet;

/**
 * Time:2022/10/21
 * Author:Perry
 * Description：coinId获取币的行情
 */
public class CurrencyPriceEntity {

    public long lastTime; // 最后请求时间
    public String coinName; // 币种名称

    private double usd;
    private double usd_24h_change;

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public double getUsd() {
        return usd;
    }

    public void setUsd(double usd) {
        this.usd = usd;
    }

    public double getUsd_24h_change() {
        return usd_24h_change;
    }

    public void setUsd_24h_change(double usd_24h_change) {
        this.usd_24h_change = usd_24h_change;
    }
}
