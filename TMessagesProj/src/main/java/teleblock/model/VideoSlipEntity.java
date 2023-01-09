package teleblock.model;

import java.io.Serializable;
import java.util.List;

import teleblock.file.KKFileMessage;

/**
 * Created by LSD on 2021/5/4.
 * Desc
 */
public class VideoSlipEntity implements Serializable {
    public String from;
    public long dialogId;
    public String title;

    public boolean hasMessage;

    public List<KKFileMessage> messageList;
    public int position;
    public int page;
}
