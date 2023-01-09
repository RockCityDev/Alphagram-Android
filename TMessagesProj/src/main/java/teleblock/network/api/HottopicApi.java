package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：热门话题请求类
 */
public class HottopicApi implements IRequestApi {
    @NonNull
    @Override
    public String getApi() {
        return "/web3/hot/tags";
    }

    private String language_id;

    public HottopicApi setLanguage_id(String language_id) {
        this.language_id = language_id;
        return this;
    }
}
