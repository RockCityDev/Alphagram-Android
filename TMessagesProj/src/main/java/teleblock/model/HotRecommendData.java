package teleblock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：热门推荐数据
 */
public class HotRecommendData {
    //1Group|2Channel|3Bot
    private int chat_type;
    private long chat_id;
    private String chat_link;
    private String chat_title;
    private String avatar;
    private int follows;
    private int online;
    private long timestamp;

    //自己加的值
    private boolean ifInChat;

    //头像组合
    private List<String> avatarList = new ArrayList<>();

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAvatarList(List<String> avatarList) {
        this.avatarList = avatarList;
    }

    public List<String> getAvatarList() {
        return avatarList;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getFollows() {
        return follows;
    }

    public void setFollows(int follows) {
        this.follows = follows;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public boolean isIfInChat() {
        return ifInChat;
    }

    public void setIfInChat(boolean ifInChat) {
        this.ifInChat = ifInChat;
    }

    public int getChat_type() {
        return chat_type;
    }

    public void setChat_type(int chat_type) {
        this.chat_type = chat_type;
    }

    public long getChat_id() {
        return chat_id;
    }

    public void setChat_id(long chat_id) {
        this.chat_id = chat_id;
    }

    public String getChat_link() {
        return chat_link;
    }

    public void setChat_link(String chat_link) {
        this.chat_link = chat_link;
    }

    public String getChat_title() {
        return chat_title;
    }

    public void setChat_title(String chat_title) {
        this.chat_title = chat_title;
    }
}
