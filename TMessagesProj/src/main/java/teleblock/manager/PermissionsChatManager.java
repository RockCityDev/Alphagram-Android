package teleblock.manager;

import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.PermissChatEntity;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/8/5
 * Author:Perry
 * Description：黑白名单会话列表管理
 */
public class PermissionsChatManager {

    private static PermissionsChatManager mPermissionsChatManager;

    public static PermissionsChatManager getInstance() {
        if (mPermissionsChatManager == null) {
            synchronized (PermissionsChatManager.class) {
                if (mPermissionsChatManager == null) {
                    mPermissionsChatManager = new PermissionsChatManager();
                }
            }
        }
        return mPermissionsChatManager;
    }

    /**
     * 获取过滤掉了黑名单或者白名单的所有会话数据
     * @param filterBlackChats
     * @return
     */
    public List<PermissChatEntity> chatListFilter(boolean filterBlackChats) {
        List<PermissChatEntity> chatList = new ArrayList<>();
        //选中的dialogID列表
        List<Long> ids;
        //所有的会话列表数据
        ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(MessagesController.getInstance(UserConfig.selectedAccount).getAllDialogs());

        //从mmkv里面获取之前存储的数据
        if (filterBlackChats) {
            ids = MMKVUtil.whiteChatList();
        } else {
            ids = MMKVUtil.blackChatList();
        }

        for (TLRPC.Dialog dialog : dialogs) {
            if (ids.isEmpty()) {
                setChatData(dialog, chatList);
            } else {
                boolean find = false;
                for (Long dialogId : ids) {
                    if (dialog.id == dialogId) {
                        find = true;
                        break;
                    }
                }

                if (!find) {
                    setChatData(dialog, chatList);
                }
            }
        }

        return chatList;
    }

    /**
     * 获取黑名单或者白名单数据
     * @param filterBlackChats
     * @return
     */
    public List<PermissChatEntity> permissionsChatList(boolean filterBlackChats) {
        List<PermissChatEntity> chatList = new ArrayList<>();
        //选中的dialogID列表
        List<Long> ids;
        //所有的会话列表数据
        ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(MessagesController.getInstance(UserConfig.selectedAccount).getAllDialogs());

        if (filterBlackChats) {
            ids = MMKVUtil.blackChatList();
        } else {
            ids = MMKVUtil.whiteChatList();
        }

        for (TLRPC.Dialog dialog : dialogs) {
            for (Long chatId : ids) {
                if (dialog.id == chatId) {
                    setChatData(dialog, chatList);
                }
            }
        }
        return chatList;
    }

    /**
     * 设置会话数据给集合
     * @param dialog
     * @param chatList
     */
    private void setChatData(TLRPC.Dialog dialog, List<PermissChatEntity> chatList) {
        PermissChatEntity chatEntity = new PermissChatEntity();
        if (DialogObject.isUserDialog(dialog.id)) {
            TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(dialog.id);
            if (!UserObject.isDeleted(user)) {
                if (!getUserName(user).trim().isEmpty()) {
                    chatEntity.setDialogId(dialog.id);
                    chatEntity.setUserChat(true);
                    chatEntity.setUser(user);
                    chatEntity.setChatName(getUserName(user));
                    chatList.add(chatEntity);
                }
            }
        } else if (DialogObject.isChatDialog(dialog.id)) {
            TLRPC.Chat chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialog.id);
            chatEntity.setDialogId(dialog.id);
            chatEntity.setUserChat(false);
            chatEntity.setChat(chat);
            chatEntity.setChatName(chat.title);
            chatList.add(chatEntity);
        }
    }

    /**
     * 获取用户昵称
     * @param user
     * @return
     */
    private String getUserName(TLRPC.User user) {
        String userName = user.username;
        String fristName = user.first_name;
        String lastName = user.last_name;

        StringBuilder name = new StringBuilder();

        if (null != fristName) {
            name.append(fristName);
        }
        if (null != lastName) {
            name.append(lastName);
        }

        if (null != userName) {
            if (name.toString().isEmpty()) {
                name.append(userName);
            }
        }

        return name.toString();
    }
}
