package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/10/14
 * Author:Perry
 * Description：解绑钱包
 */
public class UnBindWalletApi implements IRequestApi {
    @NonNull
    @Override
    public String getApi() {
        return "/user/unbind/wallet";
    }
}
