package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/9/6
 * Author:Perry
 * Description：请求群或者频道的详情数据
 */
public class ChatInforApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/web3/group/infobychatid";
    }

    private long chat_id;

    public ChatInforApi setChat_id(long chat_id) {
        this.chat_id = chat_id;
        return this;
    }
}
