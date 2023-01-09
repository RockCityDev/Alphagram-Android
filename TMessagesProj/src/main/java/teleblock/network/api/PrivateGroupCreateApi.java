package teleblock.network.api;
import teleblock.network.BaseRequestLoadmore;

/**
 * Description：我已进入的群
 */
public class PrivateGroupCreateApi extends BaseRequestLoadmore {

    @Override
    public String getApi() {
        return "/web3/group/self";
    }

    public PrivateGroupCreateApi(int page, int pageSize) {
        super(page, pageSize);
    }
}
