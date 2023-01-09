package teleblock.translate.trans;

import android.content.Context;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import teleblock.translate.callback.TranslateCallBack;
import teleblock.translate.utils.FormatUtil;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.util.TGLog;

/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public abstract class AbsTranslator {
    private Method def = Method.POST;
    public Map<String, String> langMap = new HashMap<>();//可以自定义支持的语言
    public LoadingDialog loadingDialog;
    private String baseUrl;

    public AbsTranslator(Context context, String baseUrl) {
        this.baseUrl = baseUrl;
        setLangSupport();

        String str = LocaleController.getString("translate_dialog_loading", R.string.translate_dialog_loading);
        try {
            loadingDialog = new LoadingDialog(context, str);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Method {
        GET, POST, POST_JSON
    }

    public Method getMethod() {
        return def;
    }

    abstract void setLangSupport();

    abstract Map<String, String> getParams(String from, String to, String content);

    protected String getPostContent(String content) {
        return "";
    }

    abstract Map<String, String> getHeaders();

    abstract String parses(String text);

    /***
     * 翻译
     * @param from
     * @param to
     * @param content
     */
    public void run(String from, String to, String content, TranslateCallBack tBack) {
        if (content.length() > 5000) {
            content = content.substring(0, 5000);
        }
        if (loadingDialog != null && !loadingDialog.isShowing()) loadingDialog.show();
        Map<String, String> headers = getHeaders();
        Map<String, String> params = getParams(from, to, content);
        Callback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
                TGLog.erro(e.getMessage());
                if (tBack != null) tBack.onFail();
            }

            @Override
            public void onResponse(String response, int id) {
                if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
                TGLog.debug(response);
                String result = "";
                try {
                    result = parses(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    TGLog.erro(e.getMessage());
                }
                tBack.onSuccess(result);
            }
        };
        if (getMethod() == Method.POST) {
            OkHttpUtils.post().url(baseUrl).headers(headers).params(params).build().execute(callback);
        } else if (getMethod() == Method.POST_JSON) {
            String url = baseUrl + "?" + FormatUtil.paramsFormat(params);
            OkHttpUtils.postString().url(url).headers(headers).content(getPostContent(content)).mediaType(MediaType.parse("application/json")).build().execute(callback);
        } else if (getMethod() == Method.GET) {
            OkHttpUtils.get().url(baseUrl).headers(headers).params(params).build().execute(callback);
        }
    }
}
