package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/8/12
 * Author:Perry
 * Description：社群数据状态修改
 */
public class RecommendStatusChangeApi implements IRequestApi {

    private String chat_id;

    @NonNull
    @Override
    public String getApi() {
        return "/tgchat/status";
    }

    public RecommendStatusChangeApi setChat_id(String chat_id) {
        this.chat_id = chat_id;
        return this;
    }
}
