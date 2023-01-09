package teleblock.network;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestServer;

import teleblock.config.AppConfig;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：网络请求server
 */
public class RequestServer implements IRequestServer {

    @NonNull
    @Override
    public String getHost() {
        return AppConfig.NetworkConfig.API_BASE_URL;
    }
}
