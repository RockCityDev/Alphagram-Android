package teleblock.model;

import java.util.HashMap;

public class TranslateEntity {

    private String code;
    private String language;

    public TranslateEntity(String code, String language) {
        this.code = code;
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static class Params {
        public String url;
        public HashMap<String, String> header;
        public String data;
    }
}