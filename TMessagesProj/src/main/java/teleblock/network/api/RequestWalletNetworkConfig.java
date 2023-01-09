package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/8/30
 * Author:Perry
 * Description：请求钱包网络配置
 */
public class RequestWalletNetworkConfig implements IRequestApi {
    @NonNull
    @Override
    public String getApi() {
        return "/web3/config";
    }
}
