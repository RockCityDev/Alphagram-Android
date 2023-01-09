package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * Time:2022/7/6
 * Author:Perry
 * Description：app埋点
 */
public class AppTrackApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/app/track";
    }

    private String key;
    private String name;
    private String event_key;
    private String event_name;
    private String data;

    public AppTrackApi setKey(String key) {
        this.key = key;
        return this;
    }

    public AppTrackApi setName(String name) {
        this.name = name;
        return this;
    }

    public AppTrackApi setEvent_key(String event_key) {
        this.event_key = event_key;
        return this;
    }

    public AppTrackApi setEvent_name(String event_name) {
        this.event_name = event_name;
        return this;
    }

    public AppTrackApi setData(String data) {
        this.data = data;
        return this;
    }
}
