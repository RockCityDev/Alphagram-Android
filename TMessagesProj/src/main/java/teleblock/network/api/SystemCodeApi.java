package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/7/1
 * Author:Perry
 * Description：获取验证码
 */
public class SystemCodeApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/system/getcode";
    }

    private String phone;

    public SystemCodeApi setPhone(String phone) {
        this.phone = phone;
        return this;
    }
}
