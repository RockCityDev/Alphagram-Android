package teleblock.network.api;
import teleblock.network.BaseRequestLoadmore;

/**
 * Description：付费群
 */
public class PrivateGroupApi extends BaseRequestLoadmore {
    public int hot_tag_id;
    private long chain_id;

    @Override
    public String getApi() {
        return "/web3/group/list";
    }

    public PrivateGroupApi(int page, int pageSize) {
        super(page, pageSize);
    }

    public PrivateGroupApi setChain_id(long chain_id) {
        this.chain_id = chain_id;
        return this;
    }
}
