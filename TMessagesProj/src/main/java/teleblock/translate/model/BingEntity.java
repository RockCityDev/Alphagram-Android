package teleblock.translate.model;

import java.util.List;

/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class BingEntity {
    public List<TranslationsEntity> translations;

    public static class TranslationsEntity {
        public String text;
        public String to;
    }

    public static class RequestEntity{
        public String Text;
    }
}
