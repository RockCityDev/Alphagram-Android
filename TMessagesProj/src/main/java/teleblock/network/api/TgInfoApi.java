package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * 创建日期：2022/7/25
 * 描述：更新用户的tg数量
 */
public class TgInfoApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/user/tgInfo";
    }

    private int tg_group_num;
    private int tg_channel_num;
    private int tg_user_num;
    private int tg_bot_num;

    public TgInfoApi setTg_group_num(int tg_group_num) {
        this.tg_group_num = tg_group_num;
        return this;
    }

    public TgInfoApi setTg_channel_num(int tg_channel_num) {
        this.tg_channel_num = tg_channel_num;
        return this;
    }

    public TgInfoApi setTg_user_num(int tg_user_num) {
        this.tg_user_num = tg_user_num;
        return this;
    }

    public TgInfoApi setTg_bot_num(int tg_bot_num) {
        this.tg_bot_num = tg_bot_num;
        return this;
    }
}