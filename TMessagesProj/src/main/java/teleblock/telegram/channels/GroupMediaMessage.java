package teleblock.telegram.channels;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.util.List;

import teleblock.video.KKFileDownloadStatus;

/**
 * Created by LSD on 2021/6/2.
 * Desc
 */
public class GroupMediaMessage extends ChannelMessage {
    public List<ChannelMessage> groupMediaMessages;

    public GroupMediaMessage(TLRPC.Chat chat, MessageObject messageObject, KKFileDownloadStatus downloadStatus) {
        super(chat, messageObject, downloadStatus);
    }

    @Override
    public int getMessageType() {
        return ExtendMessageType.GROUP_MEDIA;
    }
}
