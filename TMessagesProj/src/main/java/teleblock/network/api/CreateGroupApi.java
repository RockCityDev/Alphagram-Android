package teleblock.network.api;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpIgnore;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestType;
import com.hjq.http.model.BodyType;

import java.io.Serializable;

/**
 * Time:2022/8/30
 * Author:Perry
 * Description：创建群或者频道
 */
public class CreateGroupApi implements IRequestApi, IRequestType, Serializable {

    @HttpIgnore
    public final static String CREATE = "/web3/create/group";
    @HttpIgnore
    public final static String EDIT = "/web3/update/group";

    @HttpIgnore
    private String requstApi;

    public CreateGroupApi(int pageType) {
        if (pageType == 1) {
            this.requstApi = CREATE;
        } else {
            this.requstApi = EDIT;
        }
    }

    @NonNull
    @Override
    public String getApi() {
        return requstApi;
    }

    //修改的时候才会用到
    private int id;

    //群类：group，channel，supergroup
    private String type;

    //群基本信息
    private String chat_id;
    private String title;
    private String description;
    private String avatar;

    //入群方式:1=免费，2=资格审核，3=付费
    private int join_type;

    //钱包类型 join_type=3必须传
    private long wallet_id;
    private String wallet_name;

    //链类型  join_type !=1必须传
    private long chain_id;
    private String chain_name;

    //合约类型  join_type !=1必须传
    private long token_id;
    private String token_name;

    //付费金额 最小token join_type !=1必须传
    private String amount;

    //币种 join_type !=1必须传
    private long currency_id;
    private String currency_name;

    //NFT合约地址  token非erc20必传递
    private String token_address;

    //付费入群账户 join_type=3必传
    private String receipt_account;

    //邀请链接，join_type=1必传
    private String chat_link;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            this.description = "";
        } else {
            this.description = description;
        }
    }

    public void setChat_link(String chat_link) {
        this.chat_link = chat_link;
    }

    public String getChat_link() {
        return chat_link;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setJoin_type(int join_type) {
        this.join_type = join_type;
    }

    public void setWallet_id(long wallet_id) {
        this.wallet_id = wallet_id;
    }

    public void setWallet_name(String wallet_name) {
        this.wallet_name = wallet_name;
    }

    public void setChain_id(long chain_id) {
        this.chain_id = chain_id;
    }

    public void setChain_name(String chain_name) {
        this.chain_name = chain_name;
    }

    public void setToken_id(long token_id) {
        this.token_id = token_id;
    }

    public void setToken_name(String token_name) {
        this.token_name = token_name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setCurrency_id(long currency_id) {
        this.currency_id = currency_id;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public void setToken_address(String token_address) {
        this.token_address = token_address;
    }

    public void setReceipt_account(String receipt_account) {
        this.receipt_account = receipt_account;
    }

    /*获取值*/
    public String getType() {
        return type;
    }

    public String getChat_id() {
        return chat_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getJoin_type() {
        return join_type;
    }

    public long getWallet_id() {
        return wallet_id;
    }

    public String getWallet_name() {
        return wallet_name;
    }

    public long getChain_id() {
        return chain_id;
    }

    public String getChain_name() {
        return chain_name;
    }

    public long getToken_id() {
        return token_id;
    }

    public String getToken_name() {
        return token_name;
    }

    public String getAmount() {
        return amount;
    }

    public long getCurrency_id() {
        return currency_id;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public String getToken_address() {
        return token_address;
    }

    public String getReceipt_account() {
        return receipt_account;
    }

    /**
     * 获取参数的提交类型
     */
    @NonNull
    @Override
    public BodyType getBodyType() {
        return BodyType.JSON;
    }
}
