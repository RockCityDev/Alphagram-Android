package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.annotation.HttpIgnore;
import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/7/6
 * Author:Perry
 * Description：请求主题列表
 */
public class ThemeListApi implements IRequestApi {

    @HttpIgnore
    private int type;

    public ThemeListApi(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String getApi() {
        String api = "/theme/hot";
        if (type == 0) {
            api = "/theme/hot";
        } else if (type == 1) {
            api = "/theme/list";
        } else if (type == 2) {
            api = "/theme/video";
        }
        return api;
    }


    private int page;

    public ThemeListApi setPage(int page) {
        this.page = page;
        return this;
    }
}
