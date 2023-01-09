package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/6/30
 * Author:Perry
 * Description：登录请求
 */
public final class LoginApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/passport/login";
    }

    private String device;
    private String countrycode;
    private String simcode;
    private long tg_user_id;
    private int is_tg_new;

    public LoginApi setIs_tg_new(int is_tg_new) {
        this.is_tg_new = is_tg_new;
        return this;
    }

    public LoginApi setTg_user_id(long tg_user_id) {
        this.tg_user_id = tg_user_id;
        return this;
    }

    public LoginApi setDevice(String device) {
        this.device = device;
        return this;
    }

    public LoginApi setCountrycode(String countrycode) {
        this.countrycode = countrycode;
        return this;
    }

    public LoginApi setSimcode(String simcode) {
        this.simcode = simcode;
        return this;
    }
}