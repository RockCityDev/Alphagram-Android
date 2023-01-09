package teleblock.model;

import com.google.gson.annotations.SerializedName;

import teleblock.config.KKChatConfig;

/**
 * Created by LSD on 2021/7/1.
 * Desc
 */
public class MessageLink {
    public String keyword = "";
    public long dialogId;
    public String chatTitle;
    public int msgId;
    public long time;

    @SerializedName("u")
    public String link;

    @SerializedName("t")
    public String type;

    @SerializedName("n")
    public String name;

    @SerializedName("a")
    public String avatar;

    @SerializedName("f")
    public int follows;

    public void setType(KKChatConfig.ChatType type) {
        switch (type) {
            case BOT:
                this.type = "3";
                break;
            case GROUP:
                this.type = "2";
                break;
            case CHANNEL:
                this.type = "1";
                break;
        }
    }

    public int getItemType() {
        return 0;
    }
}
