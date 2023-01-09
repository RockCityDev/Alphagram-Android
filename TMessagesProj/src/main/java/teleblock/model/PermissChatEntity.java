package teleblock.model;

import org.telegram.tgnet.TLRPC;

/**
 * Time:2022/8/8
 * Author:Perry
 * Description：黑白名单页面适配器实体类
 */
public class PermissChatEntity {

    private long dialogId;

    private String chatName;

    private boolean isUserChat;

    private TLRPC.User user;

    private TLRPC.Chat chat;

    private boolean ifChecked;

    public void setIfChecked(boolean ifChecked) {
        this.ifChecked = ifChecked;
    }

    public boolean isIfChecked() {
        return ifChecked;
    }

    public TLRPC.Chat getChat() {
        return chat;
    }

    public void setChat(TLRPC.Chat chat) {
        this.chat = chat;
    }

    public void setUser(TLRPC.User user) {
        this.user = user;
    }

    public TLRPC.User getUser() {
        return user;
    }

    public void setUserChat(boolean userChat) {
        isUserChat = userChat;
    }

    public boolean isUserChat() {
        return isUserChat;
    }

    public long getDialogId() {
        return dialogId;
    }

    public void setDialogId(long dialogId) {
        this.dialogId = dialogId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
