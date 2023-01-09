package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

/**
 * 获取所有 NFT 的信息
 */
public class PolygonAssentsApi implements IRequestApi, IRequestHost {

    @NonNull
    @Override
    public String getHost() {
        return "https://api.opensea.io";
    }

    @NonNull
    @Override
    public String getApi() {
        return "/api/v2/assets/matic";
    }

    private String owner_address; // 资产所有者的地址
    private String order_direction; // 可以asc用于上升或desc下降
    private String limit; // 限制。默认为 20，上限为 50。
    private String cursor; // 指向要检索的页面的游标
    private boolean include_orders; // 确定是否应在响应中包含订单信息的标志

    public PolygonAssentsApi setOwner_address(String owner_address) {
        this.owner_address = owner_address;
        return this;
    }

    public PolygonAssentsApi setOrder_direction(String order_direction) {
        this.order_direction = order_direction;
        return this;
    }

    public PolygonAssentsApi setLimit(String limit) {
        this.limit = limit;
        return this;
    }

    public PolygonAssentsApi setCursor(String cursor) {
        this.cursor = cursor;
        return this;
    }

    public PolygonAssentsApi setInclude_orders(boolean include_orders) {
        this.include_orders = include_orders;
        return this;
    }
}
