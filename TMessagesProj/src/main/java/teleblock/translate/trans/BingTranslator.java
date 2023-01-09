package teleblock.translate.trans;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.translate.model.BingEntity;


/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class BingTranslator extends AbsTranslator {
    private String key = "";
    private String region = "";

    public BingTranslator(Context context, String url, String key, String region) {
        super(context, url);
        this.key = key;
        this.region = region;
    }

    @Override
    public Method getMethod() {
        return Method.POST_JSON;
    }

    @Override
    void setLangSupport() {
        langMap.put("auto", "");
        langMap.put("zh-TW", "zh-Hant");
        langMap.put("zh-CN", "zh-Hans");
    }

    @Override
    Map<String, String> getParams(String from, String to, String content) {
        Map<String, String> params = new HashMap<>();
        params.put("from", langMap.get(from) != null ? langMap.get(from) : from);
        params.put("to", langMap.get(to) != null ? langMap.get(to) : to);
        params.put("api-version", "3.0");
        return params;
    }

    @Override
    protected String getPostContent(String content) {
        BingEntity.RequestEntity request = new BingEntity.RequestEntity();
        request.Text = content;
        List<BingEntity.RequestEntity> list = new ArrayList<>();
        list.add(request);
        String rContent = new Gson().toJson(list);
        return rContent;
    }

    @Override
    Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", key);
        headers.put("Ocp-Apim-Subscription-Region", region);
        return headers;
    }

    @Override
    String parses(String text) {
        String data = "";
        Type type = new TypeToken<ArrayList<BingEntity>>() {
        }.getType();
        List<BingEntity> entityList = new Gson().fromJson(text, type);
        for (BingEntity bingEntity : entityList) {
            for (BingEntity.TranslationsEntity trs : bingEntity.translations) {
                data += trs.text;
            }
        }
        return data;
    }
}
