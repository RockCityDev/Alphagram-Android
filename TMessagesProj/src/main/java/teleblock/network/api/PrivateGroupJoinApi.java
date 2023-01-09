package teleblock.network.api;
import teleblock.network.BaseRequestLoadmore;

/**
 * Description：我已进入的群
 */
public class PrivateGroupJoinApi extends BaseRequestLoadmore {

    @Override
    public String getApi() {
        return "/web3/group/join";
    }

    public PrivateGroupJoinApi(int page, int pageSize) {
        super(page, pageSize);
    }
}
