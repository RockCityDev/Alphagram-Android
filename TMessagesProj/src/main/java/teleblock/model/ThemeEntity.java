package teleblock.model;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by LSD on 2021/9/8.
 * Desc
 */
public class ThemeEntity {
    public int perpage;
    public int page;
    public int total;
    public List<ItemEntity> item;

    public static class ItemEntity {
        public int id;
        public String title;
        public String avatar;
        public String url;//主题URL
        public String video;//动态主题视频地址
        public int used;

        public int avatarId;
        public int type;//0：服务器配置普通主题,1：本地 ，2：视频背景主题，3：单纯视频背景
        public String videoPath;//视频下载本地路径
        public int videoBackgroudType;//1：自己选的，2：推荐的（type=3的时候用）

        public int getType() {
            if (!TextUtils.isEmpty(video)) return 2;
            else return type;
        }
    }
}
