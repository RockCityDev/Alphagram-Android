package teleblock.model;


import java.io.Serializable;
import java.util.List;

/**
 * Time:2022/7/1
 * Author:Perry
 * Description：系统配置类
 */
public class SystemEntity implements Serializable {
//    public String host;
//    public String telegram;
//    public Channel channel;
//    public String invite;
//    public SearchTagEntity search;
//    public String tester;
    public String[] testerphones;
    public Translate translate;
    public boolean channel_post;
    public String openseaapikey;
    public List<Language> language;
    public String bot_username;
    public String bot_nickname;
    public String zapper;
    public String eth_api_key;
    public String polygon_api_key;
    public String tt_api_key;
    public String wallet_address;
    public String h5_domain;
    public long bot_id;
    public String wallet_way;

//    public static class Channel implements Serializable {
//
//        public boolean enable;
//        public List<ListEntity> list;
//
//        public static class ListEntity implements Serializable {
//            public String name;
//            public int id;
//        }
//    }

    public static class Translate {
        public String name;
        public String api;
        public String key;
        public String region;
        public boolean bing;
    }

    public static class Language {
        public String key;
        public int value;
        public boolean selector;
    }
}
