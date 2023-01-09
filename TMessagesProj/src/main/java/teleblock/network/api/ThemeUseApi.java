package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

public final class ThemeUseApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/theme/use";
    }

    private String id;

    public ThemeUseApi setId(String id) {
        this.id = id;
        return this;
    }
}