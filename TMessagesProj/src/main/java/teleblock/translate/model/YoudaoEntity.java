package teleblock.translate.model;

import java.util.List;

/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class YoudaoEntity {

    public String type;
    public int errorCode;
    public int elapsedTime;
    public List<List<TranslateResultEntity>> translateResult;

    public static class TranslateResultEntity {
        public String src;
        public String tgt;
    }
}
