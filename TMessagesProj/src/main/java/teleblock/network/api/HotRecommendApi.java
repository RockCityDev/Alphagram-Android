package teleblock.network.api;


import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpRename;
import com.hjq.http.config.IRequestServer;
import com.hjq.http.config.IRequestType;
import com.hjq.http.model.BodyType;

import java.util.List;

import jnr.ffi.annotations.In;
import teleblock.network.BaseRequestLoadmore;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：获取热门推荐数据
 */
public class HotRecommendApi extends BaseRequestLoadmore implements IRequestType {

    //请求类型 1channel 2group 3bot
    private int chat_type;
    //根据标题搜索
    private String chat_title;
    //根据标签搜索
    private List<Integer> tag_ids;

    private String language_id;

    public HotRecommendApi setLanguage_id(String language_id) {
        this.language_id = language_id;
        return this;
    }

    @Override
    public String getApi() {
        return "/tgchat/recommend";
    }

    public HotRecommendApi(int page, int pageSize) {
        super(page, pageSize);
    }

    public HotRecommendApi setChat_type(int chat_type) {
        this.chat_type = chat_type;
        return this;
    }

    public HotRecommendApi setChat_title(String chat_title) {
        this.chat_title = chat_title;
        return this;
    }

    public HotRecommendApi setTag_ids(List<Integer> tag_ids) {
        this.tag_ids = tag_ids;
        return this;
    }

    @NonNull
    @Override
    public BodyType getBodyType() {
        return BodyType.JSON;
    }
}
