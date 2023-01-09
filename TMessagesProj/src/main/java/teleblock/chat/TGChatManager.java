package teleblock.chat;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import teleblock.config.Constants;
import teleblock.manager.LoginManager;
import teleblock.model.PostChannelEntity;
import teleblock.model.SystemEntity;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TGLog;


public class TGChatManager implements NotificationCenter.NotificationCenterDelegate {
    private static TGChatManager instance;
    private ArrayList<TLRPC.Chat> allChats = new ArrayList<>();
    private boolean loadingData;

    private TGChatManager() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.chatListNeedReload);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.didUpdateConnectionState);
    }

    public void destroy() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.chatListNeedReload);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState);
    }

    public static TGChatManager getInstance() {
        if (instance == null) {
            synchronized (TGChatManager.class) {
                if (instance == null) {
                    instance = new TGChatManager();
                }
            }
        }
        return instance;
    }

    public ArrayList<TLRPC.Chat> getAllChats() {
        ArrayList<TLRPC.Chat> chats = new ArrayList<>();
        if (allChats != null && allChats.size() > 0) {
            chats = allChats;
        } else {
            try {
                chats = getChatsSync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return chats;
    }

    public TLRPC.Chat getChat(long chatId) {
        if (allChats.size() == 0) return null;
        for (TLRPC.Chat chat : allChats) {
            if (chat != null) {
                if (Math.abs(chatId) == chat.id) {
                    return chat;
                }
            }
        }
        return null;
    }

    private ArrayList<TLRPC.Chat> getChatsSync() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ArrayList<TLRPC.Chat> chats = new ArrayList<>();
        TLRPC.TL_messages_getAllChats req = new TLRPC.TL_messages_getAllChats();
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.TL_messages_chats TLRPC_Chats = (TLRPC.TL_messages_chats) response;
                for (TLRPC.Chat chat : TLRPC_Chats.chats) {
                    chats.add(chat);
                }
            }
            countDownLatch.countDown();
        });
        countDownLatch.await();
        allChats = chats;
        doPostChannel();
        return chats;
    }

    /***
     * 这里是要调接口获取的，看看tg是否可以拿到
     */
    public void getChatsAsync() {
        if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != ConnectionsManager.ConnectionStateConnected) return;
        if (loadingData) return;
        loadingData = true;
        ArrayList<TLRPC.Chat> chats = new ArrayList<>();
        TLRPC.TL_messages_getAllChats req = new TLRPC.TL_messages_getAllChats();
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {
            loadingData = false;
            if (error == null) {
                TLRPC.TL_messages_chats TLRPC_Chats = (TLRPC.TL_messages_chats) response;
                for (TLRPC.Chat chat : TLRPC_Chats.chats) {
                    chats.add(chat);
                }
                allChats = chats;
                doPostChannel();
                autoFollowChannels();
                loadStickerHistory();
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.chatListNeedReload) {
            TGLog.debug("chatListNeedReload");
            getChatsAsync();
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            if (state == ConnectionsManager.ConnectionStateConnected && allChats.size() == 0) {
                getChatsAsync();
            }
        }
    }


    private void loadStickerHistory() {
//        if (PaperUtil.loadStickerHistory()) return;
//        PaperUtil.loadStickerHistory(true);
//        new Thread(() -> {
//            new MessageStickerGifManager().loadHistoryData();
//        }).start();
    }

    private void doPostChannel() {
        SystemEntity systemEntity = MMKVUtil.getSystemMsg();
        if (systemEntity == null || !systemEntity.channel_post) return;//不收集
        List<PostChannelEntity> list = new ArrayList<>();
        int channelCount = 0;
        int groupCount = 0;
        for (TLRPC.Chat chat : allChats) {
            PostChannelEntity entity = new PostChannelEntity();
            entity.channel_id = chat.id + "";
            entity.username = chat.username;
            entity.title = chat.title;
            //KKLoger.d("TTT", "id = " + chat.id +",title = "+ chat.title + " ,isChannel = " + ChatObject.isChannel(chat) + " hash = " + chat.access_hash);
            if (ChatObject.isChannel(chat)) {
                entity.type = 1;//channel
                channelCount++;
            } else {
                entity.type = 2;//group
                groupCount++;
            }
            list.add(entity);
        }
        if (list.size() > 0) {
            Map<String, Object> cMap = new HashMap();
            cMap.put("tgUserId", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId() + "");
            cMap.put("tgUserPhone", UserConfig.getInstance(UserConfig.selectedAccount).getClientPhone() + "");
            cMap.put("type", "message_channel");
            cMap.put("count", channelCount + "");

            Map<String, Object> gMap = new HashMap();
            gMap.put("tgUserId", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId() + "");
            gMap.put("tgUserPhone", UserConfig.getInstance(UserConfig.selectedAccount).getClientPhone() + "");
            gMap.put("type", "message_group");
            gMap.put("count", groupCount + "");

//            Map<String, Object> aMap = new HashMap();
//            aMap.put("tgUserId", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId() + "");
//            aMap.put("tgUserPhone", UserConfig.getInstance(UserConfig.selectedAccount).getClientPhone() + "");
//            aMap.put("type", "message_all");
//            aMap.put("count", channelCount + groupCount + "");
//            EventUtil.post(ApplicationLoader.applicationContext, EventUtil.Even.用户关注数, aMap);

            Map<String, Object> gfMap = new HashMap();
            gfMap.put("tgUserId", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId() + "");
            gfMap.put("tgUserPhone", UserConfig.getInstance(UserConfig.selectedAccount).getClientPhone() + "");
            gfMap.put("all_data_count", list.size() + "");

//            Api.postChannel(new Gson().toJson(list), new NSCallback<SuccessComm>(ApplicationLoader.applicationContext, SuccessComm.class) {
//            });
        }
    }

    //自动关注
    private void autoFollowChannels() {
        if (!LoginManager.isNewer()) return;
        List<String> list = new ArrayList<>();
        String officeChannel = Constants.getOfficialChannel();
        String officeGroup = Constants.getOfficialGroup();
        if (!TextUtils.isEmpty(officeChannel)) {
            list.add(officeChannel);
        }
//        if (!TextUtils.isEmpty(officeGroup)) {
//            list.add(officeGroup);
//        }
        if (list.size() > 0) {
            for (String string : list) {
                TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                req.username = string;
                ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error != null) {
                        return;
                    }
                    final TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    MessagesController.getInstance(UserConfig.selectedAccount).putUsers(res.users, false);
                    MessagesController.getInstance(UserConfig.selectedAccount).putChats(res.chats, false);
                    MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, false, true);

                    long chat_id = res.chats.get(0).id;
                    int currentAccount = UserConfig.selectedAccount;
                    TLRPC.User user = AccountInstance.getInstance(currentAccount).getUserConfig().getCurrentUser();
                    MessagesController.getInstance(currentAccount).addUserToChat(chat_id, user, 0, null, null, null);
                }));
            }
        }
    }

    //一键清除未读消息
    public void clearAllUnReadMessages() {
        //有原生方法
        AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesStorage().readAllDialogs(-1);

        /*  AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesStorage().mainUnreadCount = 0;
        ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(UserConfig.selectedAccount).dialogFilters;
        for (MessagesController.DialogFilter filter : filters) {
            filter.unreadCount = 0;
        }
        //MessagesController.getInstance(UserConfig.selectedAccount).getChats();
        for (TLRPC.Chat chat : allChats) {
            int maxPositiveUnreadId = Integer.MIN_VALUE;
            int maxNegativeUnreadId = Integer.MAX_VALUE;
            int maxUnreadDate = Integer.MIN_VALUE;
            long dialog_id = -chat.id;
            MessageObject messageObject = KKChatManager.getInstance().getDialogNewMessage(dialog_id);
            if (messageObject != null) {
                maxPositiveUnreadId = Math.max(maxPositiveUnreadId, messageObject.getId());
                maxUnreadDate = Math.max(maxUnreadDate, messageObject.messageOwner.date);
                maxNegativeUnreadId = Math.min(maxNegativeUnreadId, messageObject.getId());
            }
            //清除@消息
            AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().markMentionsAsRead(dialog_id);
            AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().markDialogAsRead(dialog_id, maxPositiveUnreadId, maxNegativeUnreadId, maxUnreadDate, false, 0, 0, true, 0);
        }
        for (TLRPC.TL_contact contact : ContactsController.getInstance(UserConfig.selectedAccount).contacts) {
            int maxPositiveUnreadId = Integer.MIN_VALUE;
            int maxNegativeUnreadId = Integer.MAX_VALUE;
            int maxUnreadDate = Integer.MIN_VALUE;
            MessageObject messageObject = KKChatManager.getInstance().getDialogNewMessage(contact.user_id);
            if (messageObject != null) {
                maxPositiveUnreadId = Math.max(maxPositiveUnreadId, messageObject.getId());
                maxUnreadDate = Math.max(maxUnreadDate, messageObject.messageOwner.date);
                maxNegativeUnreadId = Math.min(maxNegativeUnreadId, messageObject.getId());
            }
            AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().markDialogAsRead(contact.user_id, maxPositiveUnreadId, maxNegativeUnreadId, maxUnreadDate, false, 0, 0, true, 0);
        }*/
    }
}
