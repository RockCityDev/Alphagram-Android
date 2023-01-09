package teleblock.video;
import android.text.TextUtils;

import org.telegram.messenger.MessageObject;

import teleblock.file.KKFileMessage;
import teleblock.file.KKFileTypes;

/**
 * 视频消息封装类
 */
public class KKVideoMessage extends KKFileMessage {

    private String dateObject;

    public KKVideoMessage(String dateObject){
        this(null, null);
        this.dateObject = dateObject;
    }

    public KKVideoMessage(MessageObject messageObject, KKFileDownloadStatus downloadStatus) {
        super(messageObject, downloadStatus, KKFileTypes.IGNORE);
    }

    /***
     *
     * @return 是否是日期分组消息
     */
    public boolean isDateMessage() {
        return !TextUtils.isEmpty(this.dateObject);
    }

    /***
     *
     * @return 分组日期
     */
    public String getDateObject(){
        return this.dateObject;
    }

    /**
     *
     * @return 视频长度，单位为秒
     */
    public int getVideoDuration() {
        return messageObject.getDuration();
    }
}
