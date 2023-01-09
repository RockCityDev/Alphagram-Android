package teleblock.network.api;


import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestType;
import com.hjq.http.model.BodyType;

import java.util.List;

/**
 * 根据TGUserId获取NFT相关数据
 */
public class TgUseridApi implements IRequestApi, IRequestType {

    @Override
    public String getApi() {
        return "/nftInfo/tguserid";
    }

    private List<Long> tg_user_id;

    public TgUseridApi setTg_user_id(List<Long> tg_user_id) {
        this.tg_user_id = tg_user_id;
        return this;
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
