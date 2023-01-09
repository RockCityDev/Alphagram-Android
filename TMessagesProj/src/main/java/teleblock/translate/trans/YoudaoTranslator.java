package teleblock.translate.trans;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.translate.model.YoudaoEntity;


/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class YoudaoTranslator extends AbsTranslator {
    private static String url = "http://fanyi.youdao.com/translate";

    public YoudaoTranslator(Context context) {
        super(context, url);
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    void setLangSupport() {
    }

    @Override
    Map<String, String> getParams(String from, String to, String content) {
        Map<String, String> params = new HashMap<>();
        params.put("i", content);
        params.put("from", from);
        params.put("to", to);
        params.put("doctype", "json");
        return params;
    }

    @Override
    Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        return headers;
    }

    @Override
    String parses(String text) {
        YoudaoEntity entity = new Gson().fromJson(text, YoudaoEntity.class);
        String data = "";
        if (entity != null) {
            for (List<YoudaoEntity.TranslateResultEntity> trsList : entity.translateResult) {
                for (YoudaoEntity.TranslateResultEntity trs : trsList) {
                    if (TextUtils.isEmpty(trs.src) && TextUtils.isEmpty(trs.tgt)) {
                        data += "\n";
                    } else {
                        data += trs.tgt;
                    }
                }
            }
        }
        return data;
    }
}
