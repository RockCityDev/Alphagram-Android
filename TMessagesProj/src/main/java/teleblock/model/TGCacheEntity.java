package teleblock.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by LSD on 2021/9/29.
 * Desc
 */
public class TGCacheEntity implements Serializable {
    public static String[] TYPE_IMAGE = new String[]{"jpg", "png", "webp", "gif"};
    public static String[] TYPE_VIDEO = new String[]{"mp4", "mkv", "mov"};
    public static String[] TYPE_AUDIO = new String[]{"mp3", "wav", "wam", "ogg"};
    public static String[] TYPE_FILE = new String[]{"apk", "pdf", "doc", "txt"};
    public static String[] TYPE_OTHER = new String[]{""};
    public static String TYPE_NO_MEDIA = "nomedia";

    public int type; //image=1; video=2; file=3; audio=4; other=5;
    public String name;
    public String path;
    public long size;
    public long time;

    public boolean checked;//选中删除用

    public void setCacheType(String extName) {
        if (TYPE_NO_MEDIA.equals(extName)) {
            this.type = -1;
        } else if (Arrays.asList(TYPE_IMAGE).contains(extName)) {
            this.type = 1;
        } else if (Arrays.asList(TYPE_VIDEO).contains(extName)) {
            this.type = 2;
        } else if (Arrays.asList(TYPE_AUDIO).contains(extName)) {
            this.type = 3;
        } else if (Arrays.asList(TYPE_FILE).contains(extName)) {
            this.type = 4;
        } else {
            this.type = 5;
        }
    }
}
