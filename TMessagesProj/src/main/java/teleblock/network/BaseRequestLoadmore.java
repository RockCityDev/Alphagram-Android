package teleblock.network;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：请求更多
 */
public abstract class BaseRequestLoadmore implements IRequestApi {

    protected int page;
    protected int pageSize;

    public BaseRequestLoadmore(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }
}
