package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

public class TransferHistoryApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "/user/transfer";
    }
}