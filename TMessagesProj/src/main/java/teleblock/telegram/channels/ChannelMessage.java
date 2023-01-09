package teleblock.telegram.channels;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import teleblock.video.KKFileDownloadStatus;

public class ChannelMessage {
    public TLRPC.Chat chat;
    public MessageObject messageObject;
    public KKFileDownloadStatus downloadStatus;

    public ChannelMessage(TLRPC.Chat chat, MessageObject messageObject, KKFileDownloadStatus downloadStatus) {
        this.chat = chat;
        this.messageObject = messageObject;
        this.downloadStatus = downloadStatus;
    }

    /***
     * 消息类型
     * @return
     */
    public int getMessageType() {
        if (messageObject != null) {
            return messageObject.type;
        }
        return -1;
    }

    /**
     * @return 获取文件描述
     */
    public String getMessage() {
        if (messageObject != null && messageObject.caption != null) {
            return messageObject.caption.toString();
        }
        return "";
    }

    /***
     * 消息分组id
     *
     * @return
     */
    public long getMessageGroupedId() {
        if (messageObject != null && messageObject.messageOwner != null) {
            return messageObject.messageOwner.grouped_id;
        }
        return 0;
    }

    /***
     * @return 文字消息
     */
    public String getMessageText() {
        if (messageObject != null && messageObject.messageText != null) {
            return messageObject.messageText.toString();
        }
        return "";
    }

    //getMessageId
    public long getMessageId() {
        if (messageObject != null) {
            return messageObject.getId();
        }
        return -1;
    }

    /**
     * @return DialogId
     */
    public long getDialogId() {
        if (messageObject != null) {
            return messageObject.getDialogId();
        }
        return -1;
    }

    /**
     * @return 群名称。也可能是单聊的用户名等名称
     */
    public String getFromName() {
        String fromName = null;
        TLRPC.User user = messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(messageObject.messageOwner.from_id.user_id) : null;
        TLRPC.Chat chatFrom = messageObject.messageOwner.from_id.chat_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getChat(messageObject.messageOwner.peer_id.chat_id) : null;
        if (chatFrom == null) {
            chatFrom = messageObject.messageOwner.from_id.channel_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getChat(messageObject.messageOwner.peer_id.channel_id) : null;
        }
        TLRPC.Chat chatTo = messageObject.messageOwner.peer_id.channel_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getChat(messageObject.messageOwner.peer_id.channel_id) : null;
        if (chatTo == null) {
            chatTo = messageObject.messageOwner.peer_id.chat_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getChat(messageObject.messageOwner.peer_id.chat_id) : null;
        }
        if (user != null && chatTo != null) {
            fromName = chatTo.title;
        } else if (user != null) {
            fromName = ContactsController.formatName(user.first_name, user.last_name);
        } else if (chatFrom != null) {
            fromName = chatFrom.title;
        }
        return fromName == null ? "" : fromName;
    }

    /***
     * 消息时间
     *
     * @return
     */
    public int getMessageDate() {
        return messageObject.messageOwner.date;
    }

    /**
     * @return MessageObject
     */
    public MessageObject getMessageObject() {
        return messageObject;
    }

    /**
     * @return Document
     */
    public TLRPC.Document getDocument() {
        return this.messageObject.getDocument();
    }

    /***
     * 回复个数
     *
     * @return
     */
    public int getRepliesCount() {
        if (this.messageObject == null) return 0;
        return this.messageObject.getRepliesCount();
    }

    /***
     * 回复数据
     * @return
     */
    public ArrayList<TLRPC.Peer> getRecentRepliers() {
        if (this.messageObject.messageOwner.replies != null) {
            return this.messageObject.messageOwner.replies.recent_repliers;
        }
        return null;
    }

    /**
     * @return 文件大小
     */
    public long getSize() {
        if (this.messageObject.getDocument() == null) return 0;
        return this.messageObject.getDocument().size;
    }

    /**
     * @return 媒体文件长度，单位为秒
     */
    public int getMediaDuration() {
        if (this.messageObject == null) return 0;
        return messageObject.getDuration();
    }

    /**
     * @return 文件名称
     */
    public String getFileName() {
        if (this.messageObject == null) return "";
        return FileLoader.getDocumentFileName(messageObject.getDocument());
    }

    /**
     * @return 文件下载本地文件名
     */
    public String getDownloadFileName() {
        if (this.messageObject == null) return "";
        return FileLoader.getAttachFileName(messageObject.getDocument());
    }


    /***
     * 查看人数
     * @return
     */
    public int getViewNumber() {
        if (this.messageObject.messageOwner == null) return 0;
        return this.messageObject.messageOwner.views;
    }

    /***
     * chat
     * @return
     */
    public TLRPC.Chat getChat() {
        return chat;
    }
}
