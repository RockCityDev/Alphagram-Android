package teleblock.telegram.channels;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.util.List;

import teleblock.video.KKFileDownloadStatus;

/**
 * Created by LSD on 2021/6/2.
 * Desc
 */
public class JoinTimeMessage extends ChannelMessage {
    public List<ChannelMessage> joinTimeMessages;
    public boolean messageExpand = false;

    public JoinTimeMessage(TLRPC.Chat chat, MessageObject messageObject, KKFileDownloadStatus downloadStatus) {
        super(chat, messageObject, downloadStatus);
    }

    @Override
    public int getMessageType() {
        return ExtendMessageType.JOIN_TIME;
    }
}
