package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/6/29
 * Author:Perry
 * Description：请求coin关键词
 */
public class CurrencyKeywordsApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/currency/keywords";
    }

}
