package teleblock.translate;

import android.content.Context;
import android.text.TextUtils;

import teleblock.model.SystemEntity;
import teleblock.translate.trans.AbsTranslator;
import teleblock.translate.trans.BingTranslator;
import teleblock.translate.trans.GoogleTranslator;
import teleblock.translate.trans.YoudaoTranslator;
import teleblock.util.MMKVUtil;


/**
 * Created by LSD on 2021/12/23.
 * Desc
 */
public class TranslatorFactory {

    public static AbsTranslator getTranslator(Context context) {
        SystemEntity systemEntity = MMKVUtil.getSystemMsg();
        if (systemEntity != null) {
            SystemEntity.Translate translate = systemEntity.translate;
            if (translate != null && !TextUtils.isEmpty(translate.key) && translate.bing) {
                return new BingTranslator(context, translate.api, translate.key, translate.region);
            }
            return new GoogleTranslator(context);
        }
        return new YoudaoTranslator(context);
    }
}
