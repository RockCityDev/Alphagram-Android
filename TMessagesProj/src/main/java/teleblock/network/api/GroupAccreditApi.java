package teleblock.network.api;


import com.hjq.http.config.IRequestApi;

/**
 * 资格入群获取链接
 */
public class GroupAccreditApi implements IRequestApi {

    private long group_id;
    private String payment_account;

    @Override
    public String getApi() {
        return "/web3/group/accredit";
    }

    public GroupAccreditApi setGroup_id(long group_id) {
        this.group_id = group_id;
        return this;
    }

    public GroupAccreditApi setPayment_account(String payment_account) {
        this.payment_account = payment_account;
        return this;
    }
}
