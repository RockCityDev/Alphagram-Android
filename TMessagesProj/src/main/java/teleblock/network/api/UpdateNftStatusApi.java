package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/10/12
 * Author:Perry
 * Description：更改nft头像是否显示的状态
 */
public class UpdateNftStatusApi implements IRequestApi {

    //1=打开，0=关闭
    private int status;

    @NonNull
    @Override
    public String getApi() {
        return "/update/web3/status";
    }

    public UpdateNftStatusApi setStatus(int status) {
        this.status = status;
        return this;
    }
}
