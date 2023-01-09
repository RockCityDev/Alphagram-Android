package teleblock.model;

import org.telegram.tgnet.TLRPC;

/**
 * 创建日期：2022/7/7
 * 描述：
 */
public class ChatInfoEntity {

    public long chatId;
    public boolean isAdmin;
    public int participants_count;

    public String loadAdmin;
    public String loadMember;

    public ChatInfoEntity(long id) {
        chatId = id;
        loadAdmin = "正在获取";
        loadMember = "正在获取";
    }
}