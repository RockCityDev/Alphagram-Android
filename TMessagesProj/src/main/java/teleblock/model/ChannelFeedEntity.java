package teleblock.model;

import java.io.Serializable;

/**
 * Created by LSD on 2021/5/4.
 * Desc
 */
public class ChannelFeedEntity implements Serializable {
    public String from;
    public int tagId;
    public long chatId = -1;

    public int position;
    public String title;

    public int tabIndex = -1;
}
