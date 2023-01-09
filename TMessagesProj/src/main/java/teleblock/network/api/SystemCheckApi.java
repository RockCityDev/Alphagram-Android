package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;


/**
 * Time:2022/7/1
 * Author:Perry
 * Description：请求系统配置
 */
public class SystemCheckApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/system/check";
    }

    private String countrycode;
    private String simcode;

    public SystemCheckApi setCountrycode(String countrycode) {
        this.countrycode = countrycode;
        return this;
    }

    public SystemCheckApi setSimcode(String simcode) {
        this.simcode = simcode;
        return this;
    }
}
