package teleblock.model;

import java.util.List;

/**
 * Created by LSD on 2021/10/6.
 * Desc
 */
public class VideoWallpaperEntity {
    public int id;
    public String title;
    public String avatar;
    public String path;
    public String video;

    //横向的
    public List<VideoWallpaperEntity> list;
}
