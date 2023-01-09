package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

/**
 * 群详情
 * 用我们的群详情去轻轻
 */
public class ShopInfoApi implements IRequestApi {

    private long group_id;

    @Override
    public String getApi() {
        return "/web3/group/info";
    }

    public ShopInfoApi setGroup_id(long group_id) {
        this.group_id = group_id;
        return this;
    }
}
