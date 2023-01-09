package teleblock.network.api.blockchain.polygon;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestHost;

import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/16
 * Author:Perry
 * Description：polygon gas费用查询
 */
public class PolygonGasfeeApi implements IRequestApi, IRequestHost {
    
    @NonNull
    @Override
    public String getApi() {
        return "";
    }

    @NonNull
    @Override
    public String getHost() {
        return "https://api.polygonscan.com/api";
    }

    private String module = "gastracker";
    private String action = "gasoracle";
    private String apikey = MMKVUtil.getSystemMsg().polygon_api_key;
}
