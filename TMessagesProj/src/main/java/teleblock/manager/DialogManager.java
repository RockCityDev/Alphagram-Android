package teleblock.manager;

import android.text.TextUtils;

import androidx.collection.LongSparseArray;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ChatInfoEntity;
import teleblock.util.MMKVUtil;
import timber.log.Timber;

/**
 * 对话管理
 */
public class DialogManager extends BaseController implements NotificationCenter.NotificationCenterDelegate {

    private final static long TIME_INTERVAL = 1000;

    private static volatile DialogManager[] Instance = new DialogManager[UserConfig.MAX_ACCOUNT_COUNT];

    private long lastContactTime;
    private long lastPinnedTime;
    private long lastNonContactTime;
    public List<TLRPC.Dialog> contactDialogs = new ArrayList<>();
    public List<TLRPC.Dialog> nonContactDialogs = new ArrayList<>();
    public LongSparseArray<List<TLRPC.Dialog>> relatedMeDialogs = new LongSparseArray<>();
    private ThreadUtils.SimpleTask<List<TLRPC.Dialog>> contactTask;
    private ThreadUtils.SimpleTask<List<TLRPC.Dialog>> pinnedTask;
    private ThreadUtils.SimpleTask<List<TLRPC.Dialog>> nonContactTask;
    private ThreadUtils.SimpleTask<List<ChatInfoEntity>> loadChatTask;
    public int contactUnRead;
    public int nonContactUnRead;
    public int relatedMeUnRead;

    public LongSparseArray<ChatInfoEntity> loadChatInfos = new LongSparseArray<>();
    private Set<ChatInfoLoadListener> chatInfoLoadListeners = new HashSet<>();
    public LongSparseArray<Boolean> loadChatAdmins = new LongSparseArray<>();
    private Map<Long, Boolean> requestMembers = new HashMap<>();
    private Map<Long, Boolean> requestAdmins = new HashMap<>();
    private ArrayList<Integer> queries = new ArrayList<>();
    public int finishLoadChatDialogNum;

    public int groupCount;
    public int channelCount;
    public int botCount;

    public static DialogManager getInstance(int num) {
        DialogManager localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (DialogManager.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new DialogManager(num);
                }
            }
        }
        return localInstance;
    }

    public DialogManager(int num) {
        super(num);
//        getNotificationCenter().addObserver(this, NotificationCenter.didLoadChatAdmins);

    }

    public void addChatInfoLoadListener(ChatInfoLoadListener listener) {
        chatInfoLoadListeners.add(listener);
    }

    public void removeChatInfoLoadListener(ChatInfoLoadListener listener) {
        chatInfoLoadListeners.remove(listener);
    }

    public void updateAllDialogs(boolean force) {
        getContactDialogs(null, force);
        getRelatedMeDialogs(null, force);
        getNonContactDialogs(null, force);
    }

    public void getAllChats() {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ChatInfoEntity>>() {

            @Override
            public List<ChatInfoEntity> doInBackground() throws Throwable {
                ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                TLRPC.TL_messages_getAllChats req = new TLRPC.TL_messages_getAllChats();
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (error == null) {
                        TLRPC.TL_messages_chats TLRPC_Chats = (TLRPC.TL_messages_chats) response;
                        chats.addAll(TLRPC_Chats.chats);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (TLRPC.Chat chat : chats) {
                            stringBuilder.append(chat.title).append("、");
                        }
                        Timber.i("getAllChats-->" + stringBuilder);
                    }
                });
                return null;
            }

            @Override
            public void onSuccess(List<ChatInfoEntity> result) {
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                ToastUtils.showLong(t.getMessage());
                Timber.e(t);
            }
        });
    }

    /**
     * 加载我参与的和我管理的所需数据
     */
    public void loadChatDialogs(Callback<List<ChatInfoEntity>> callback) {
        if (loadChatTask != null && !loadChatTask.isDone()) {
            return;
        }
        ThreadUtils.executeByCached(loadChatTask = new ThreadUtils.SimpleTask<List<ChatInfoEntity>>() {

            @Override
            public List<ChatInfoEntity> doInBackground() throws Throwable {
                ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(getMessagesController().getAllDialogs());
                Integer[] memberReqIds = new Integer[dialogs.size()];
                Integer[] adminReqIds = new Integer[dialogs.size()];
                groupCount = channelCount = 0;
                for (TLRPC.Dialog dialog : dialogs) {
                    if (DialogObject.isChatDialog(dialog.id)) {
                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialog.id);
                        if (chat == null) {
                            Timber.i("chat==null-->" + dialog.id);
                            continue;
                        }
                        boolean isChannel = ChatObject.isChannel(chat) && !chat.megagroup;
                        if (isChannel) {
                            channelCount++;
                        } else {
                            groupCount++;
                        }
                        ChatInfoEntity chatInfo = loadChatInfos.get(chat.id);
                        if (chatInfo == null) {
                            chatInfo = new ChatInfoEntity(chat.id);
                            loadChatInfos.put(chat.id, chatInfo);
                        }
                        // 我参与的群组
                        if (TextUtils.equals("正在获取", chatInfo.loadMember)) {
                            TLRPC.ChatFull chatFull = MessagesController.getInstance(currentAccount).getChatFull(chat.id);
                            if (isChannel) { // 过滤频道
                                chatInfo.loadMember = 0 + "人";
                                chatInfo.participants_count = 0;
                                loadChatInfos.put(chat.id, chatInfo);
                            } else if (chat.participants_count != 0) {
                                chatInfo.loadMember = chat.participants_count + "人";
                                chatInfo.participants_count = chat.participants_count;
                                loadChatInfos.put(chat.id, chatInfo);
                            } else if (chatFull != null && chatFull.participants_count != 0) {
                                chatInfo.loadMember = chatFull.participants_count + "人";
                                chatInfo.participants_count = chatFull.participants_count;
                                loadChatInfos.put(chat.id, chatInfo);
                            } else if (!Boolean.TRUE.equals(requestMembers.get(chat.id))) {
                                requestMembers.put(chat.id, true);
                                TLObject request;
                                if (ChatObject.isChannel(chat)) {
                                    TLRPC.TL_channels_getFullChannel req = new TLRPC.TL_channels_getFullChannel();
                                    req.channel = MessagesController.getInputChannel(chat);
                                    request = req;
                                } else {
                                    TLRPC.TL_messages_getFullChat req = new TLRPC.TL_messages_getFullChat();
                                    req.chat_id = chat.id;
                                    request = req;
                                }
                                ChatInfoEntity finalChatInfo = chatInfo;
                                int num = dialogs.indexOf(dialog);
                                memberReqIds[num] = getConnectionsManager().sendRequest(request, (response, error) -> {
                                    queries.remove(memberReqIds[num]);
                                    if (error == null) {
                                        AndroidUtilities.runOnUIThread(() -> {
                                            TLRPC.TL_messages_chatFull res = (TLRPC.TL_messages_chatFull) response;
                                            MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, true, true);
                                            MessagesStorage.getInstance(UserConfig.selectedAccount).updateChatInfo(res.full_chat, false);
                                            MessagesController.getInstance(UserConfig.selectedAccount).putChatFull(res.full_chat);

                                            requestMembers.put(chat.id, false);
                                            finalChatInfo.loadMember = res.full_chat.participants_count + "人";
                                            finalChatInfo.participants_count = res.full_chat.participants_count;
                                            loadChatInfos.put(chat.id, finalChatInfo);
                                            for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
                                                listener.updateChatLoad(finalChatInfo);
                                            }
                                        });
                                    } else { // 报错不再请求
                                        finalChatInfo.loadMember = error.text;
                                        finalChatInfo.participants_count = 0;
                                        loadChatInfos.put(chat.id, finalChatInfo);
                                        for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
                                            listener.updateChatLoad(finalChatInfo);
                                        }
                                    }
                                });
                                queries.add(memberReqIds[num]);
                            }
                        }
                        // 我管理的群组和频道
//                        if (TextUtils.equals("正在获取", chatInfo.loadAdmin)) {
//                            if (!Boolean.TRUE.equals(requestAdmins.get(chat.id))) {
//                                requestAdmins.put(chat.id, true);
//                                TLRPC.InputChannel inputChannel = getMessagesController().getInputChannel(chat.id);
//                                if (inputChannel.channel_id != 0) {
//                                    TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
//                                    req.channel = inputChannel;
//                                    req.limit = 100;
//                                    req.filter = new TLRPC.TL_channelParticipantsAdmins();
//                                    ChatInfoEntity finalChatInfo = chatInfo;
//                                    int num = dialogs.indexOf(dialog);
//                                    adminReqIds[num] = getConnectionsManager().sendRequest(req, (response, error) -> {
//                                        queries.remove(adminReqIds[num]);
//                                        if (error != null) { // 报错不再请求
//                                            finalChatInfo.loadAdmin = error.text;
//                                            finalChatInfo.isAdmin = false;
//                                            loadChatInfos.put(chat.id, finalChatInfo);
//                                            for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
//                                                listener.updateChatLoad(finalChatInfo);
//                                            }
//                                        } else if (response instanceof TLRPC.TL_channels_channelParticipants) {
//                                            TLRPC.TL_channels_channelParticipants participants = (TLRPC.TL_channels_channelParticipants) response;
//                                            requestAdmins.put(chat.id, false);
//                                            finalChatInfo.loadAdmin = "不是";
//                                            finalChatInfo.isAdmin = false;
//                                            loadChatInfos.put(chat.id, finalChatInfo);
//                                            for (int a = 0; a < participants.participants.size(); a++) {
//                                                TLRPC.ChannelParticipant participant = participants.participants.get(a);
//                                                if (MessageObject.getPeerId(participant.peer) == getUserConfig().clientUserId) {
//                                                    finalChatInfo.loadAdmin = "是";
//                                                    finalChatInfo.isAdmin = true;
//                                                    loadChatInfos.put(chat.id, finalChatInfo);
//                                                    break;
//                                                }
//                                            }
//                                            for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
//                                                listener.updateChatLoad(finalChatInfo);
//                                            }
//                                        }
//                                    });
//                                    queries.add(adminReqIds[num]);
//                                } else {
//                                    TLRPC.TL_messages_getFullChat req = new TLRPC.TL_messages_getFullChat();
//                                    req.chat_id = chat.id;
//                                    ChatInfoEntity finalChatInfo = chatInfo;
//                                    int num = dialogs.indexOf(dialog);
//                                    adminReqIds[num] = getConnectionsManager().sendRequest(req, (response, error) -> {
//                                        queries.remove(adminReqIds[num]);
//                                        if (error == null) {
//                                            AndroidUtilities.runOnUIThread(() -> {
//                                                TLRPC.TL_messages_chatFull res = (TLRPC.TL_messages_chatFull) response;
//                                                MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, true, true);
//                                                MessagesStorage.getInstance(UserConfig.selectedAccount).updateChatInfo(res.full_chat, false);
//                                                MessagesController.getInstance(UserConfig.selectedAccount).putChatFull(res.full_chat);
//
//                                                requestAdmins.put(chat.id, false);
//                                                finalChatInfo.loadAdmin = "不是";
//                                                finalChatInfo.isAdmin = false;
//                                                loadChatInfos.put(chat.id, finalChatInfo);
//                                                for (int a = 0, N = res.full_chat.participants.participants.size(); a < N; a++) {
//                                                    TLRPC.ChatParticipant chatParticipant = res.full_chat.participants.participants.get(a);
//                                                    if (chatParticipant instanceof TLRPC.TL_chatParticipantAdmin ||
//                                                            chatParticipant instanceof TLRPC.TL_chatParticipantCreator) {
//                                                        if (chatParticipant.user_id == getUserConfig().clientUserId) {
//                                                            finalChatInfo.loadAdmin = "是";
//                                                            finalChatInfo.isAdmin = true;
//                                                            loadChatInfos.put(chat.id, finalChatInfo);
//                                                            break;
//                                                        }
//                                                    }
//                                                }
//                                                for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
//                                                    listener.updateChatLoad(finalChatInfo);
//                                                }
//                                            });
//                                        } else { // 报错不再请求
//                                            finalChatInfo.loadAdmin = error.text;
//                                            finalChatInfo.isAdmin = false;
//                                            loadChatInfos.put(chat.id, finalChatInfo);
//                                            for (ChatInfoLoadListener listener : chatInfoLoadListeners) {
//                                                listener.updateChatLoad(finalChatInfo);
//                                            }
//                                        }
//                                    });
//                                    queries.add(adminReqIds[num]);
//                                }
//                            }
//                        }
                    }
                }
                finishLoadChatDialogNum = 0;
                List<ChatInfoEntity> loadChatList = new ArrayList<>();
                for (int a = 0, size = loadChatInfos.size(); a < size; a++) {
                    ChatInfoEntity chatInfo = loadChatInfos.valueAt(a);
                    loadChatList.add(chatInfo);
                    if (!TextUtils.equals("正在获取", chatInfo.loadMember)) {
//                        if (!TextUtils.equals("正在获取", chatInfo.loadAdmin)) {
                        finishLoadChatDialogNum++;
//                        }
                    }
                }
                return loadChatList;
            }

            @Override
            public void onSuccess(List<ChatInfoEntity> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                ToastUtils.showLong(t.getMessage());
                Timber.e(t);
            }
        });
    }

    public void cancelRequest() {
        for (int a = 0, N = queries.size(); a < N; a++) {
            getConnectionsManager().cancelRequest(queries.get(a), true);
        }
//        requestMembers.clear();
//        requestAdmins.clear();
    }

    /**
     * 获取联系人对话列表
     */
    public void getContactDialogs(Callback<List<TLRPC.Dialog>> callback, boolean force) {
        if (!force && Math.abs(TimeUtils.getTimeSpanByNow(lastContactTime, TimeConstants.MSEC)) < TIME_INTERVAL) {
            return;
        }
        if (!force && contactTask != null && !contactTask.isDone()) {
            return;
        }
        lastContactTime = System.currentTimeMillis();
        ThreadUtils.executeByCached(contactTask = new ThreadUtils.SimpleTask<List<TLRPC.Dialog>>() {
            @Override
            public List<TLRPC.Dialog> doInBackground() throws Throwable {
                ArrayList<TLRPC.Dialog> dialogList = new ArrayList<>();
                ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(getMessagesController().getAllDialogs());
                botCount = 0;
                for (TLRPC.Dialog dialog : dialogs) {
                    if (DialogObject.isUserDialog(dialog.id)) {
                        TLRPC.User user = getMessagesController().getUser(dialog.id);
                        if (user != null) {
                            if (user.bot) {
                                botCount++;
                            } else if (user.contact || user.id == 777000) {
                                dialogList.add(dialog);
                            }
                        }
                    } else if (DialogObject.isEncryptedDialog(dialog.id)) {
                        int encryptedChatId = DialogObject.getEncryptedChatId(dialog.id);
                        TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(encryptedChatId);
                        if (encryptedChat == null || !getMessagesController().getUser(encryptedChat.user_id).contact) {
                            continue;
                        }
                        dialogList.add(dialog);
                    }
                }
                Collections.sort(dialogList, dialogComparator);
                return dialogList;
            }

            @Override
            public void onSuccess(List<TLRPC.Dialog> result) {
                contactDialogs = result;
                if (callback != null) {
                    callback.onSuccess(result);
                }
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_DIALOGS_DATA, "1"));

                contactUnRead = CollectionUtils.countMatches(result, new CollectionUtils.Predicate<TLRPC.Dialog>() {
                    @Override
                    public boolean evaluate(TLRPC.Dialog item) {
                        return item.unread_count > 0;
                    }
                });
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_UNREAD_COUNT, "1", contactUnRead));
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    /**
     * 获取置顶对话列表
     */
    public void getPinnedDialogs(Callback<List<TLRPC.Dialog>> callback, boolean force) {
        if (!force && Math.abs(TimeUtils.getTimeSpanByNow(lastPinnedTime, TimeConstants.MSEC)) < TIME_INTERVAL) {
            return;
        }
        if (!force && pinnedTask != null && !pinnedTask.isDone()) {
            return;
        }
        lastPinnedTime = System.currentTimeMillis();
        ThreadUtils.executeByCached(pinnedTask = new ThreadUtils.SimpleTask<List<TLRPC.Dialog>>() {
            @Override
            public List<TLRPC.Dialog> doInBackground() throws Throwable {
                ArrayList<TLRPC.Dialog> dialogList = new ArrayList<>();
                ArrayList<Long> dialogIds = new ArrayList<>();
                // 获取“全部”对话列表
                ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(getMessagesController().getDialogs(0));
                Timber.i("dialogList0-->" + dialogs.size());
                if (dialogs.isEmpty()) { // 过滤取不到数据的情况
                    return null;
                }
                for (TLRPC.Dialog dialog : dialogs) {
                    if (dialog instanceof TLRPC.TL_dialogFolder) {//判断是不是归档
                        if (MMKVUtil.ifOpenArchive()) {//是否开启加入归档列表
                            dialogList.add(dialog);
                            dialogIds.add(dialog.id);
                        }
                    } else if (dialog.pinned) {//判断是不是置顶群
                        if (MMKVUtil.ifOpenTopping()) {//是否开启加入置顶列表
                            dialogList.add(dialog);
                            dialogIds.add(dialog.id);
                        }
                    }
                }
                Timber.i("dialogList1-->" + dialogList.size());
                // 获取所有tab对话列表
                ArrayList<MessagesController.DialogFilter> filters = getMessagesController().dialogFilters;
                for (MessagesController.DialogFilter filter : filters) {
                    for (int i = 0; i < filter.pinnedDialogs.size(); i++) {
                        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(filter.pinnedDialogs.keyAt(i));
                        if (dialog != null && !dialogIds.contains(dialog.id)) {
                            dialogList.add(dialog);
                            dialogIds.add(dialog.id);
                        }
                    }
                }
                Timber.i("dialogList2-->" + dialogList.size());
                return dialogList;
            }

            @Override
            public void onSuccess(List<TLRPC.Dialog> result) {
                if (callback != null && result != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    /**
     * 获取和我相关对话列表
     */
    public void getRelatedMeDialogs(Callback<LongSparseArray<List<TLRPC.Dialog>>> callback, boolean force) {
        getPinnedDialogs(new DialogManager.Callback<List<TLRPC.Dialog>>() {
            @Override
            public void onSuccess(List<TLRPC.Dialog> pinnedDialogs) {
                loadChatDialogs(new DialogManager.Callback<List<ChatInfoEntity>>() {
                    @Override
                    public void onSuccess(List<ChatInfoEntity> data) {
                        List<TLRPC.Dialog> dialogs1 = new ArrayList<>();
                        List<TLRPC.Dialog> dialogs2 = new ArrayList<>(pinnedDialogs);
                        List<TLRPC.Dialog> dialogs3 = new ArrayList<>();
                        for (ChatInfoEntity chatInfo : data) {
                            if (chatInfo.isAdmin) {
                                TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(-chatInfo.chatId);
                                if (dialog != null) dialogs1.add(dialog);
                            } else if (chatInfo.participants_count != 0) {
                                if (MMKVUtil.ifOpenPeopleFilter()) {//是否开启群人数过滤
                                    if (chatInfo.participants_count <= MMKVUtil.groupFilterPeopleNum()) {//判断群人数是否符合条件
                                        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(-chatInfo.chatId);
                                        if (dialog != null) dialogs3.add(dialog);
                                    }
                                } else {
                                    TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(-chatInfo.chatId);
                                    if (dialog != null) dialogs3.add(dialog);
                                }
                            }
                        }

                        Collections.sort(dialogs1, dialogComparator);
                        relatedMeDialogs.put(1, dialogs1);
                        for (TLRPC.Dialog dialog : dialogs1) {
                            for (TLRPC.Dialog pinnedDialog : pinnedDialogs) {
                                if (dialog.id == pinnedDialog.id) {
                                    dialogs2.remove(pinnedDialog);
                                    break;
                                }
                            }
                        }
//                        Collections.sort(dialogs2, dialogComparator);
                        relatedMeDialogs.put(2, dialogs2);
                        List<TLRPC.Dialog> newDialogs3 = new ArrayList<>(dialogs3);
                        for (TLRPC.Dialog dialog : dialogs2) {
                            for (TLRPC.Dialog dialog3 : dialogs3) {
                                if (dialog.id == dialog3.id) {
                                    newDialogs3.remove(dialog3);
                                    break;
                                }
                            }
                        }

                        //白名单数据
                        for (Long dialogId : MMKVUtil.whiteChatList()) {
                            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
                            if (dialog != null) newDialogs3.add(dialog);
                        }

                        //删除掉黑名单的数据
                        for (Long dialogId : MMKVUtil.blackChatList()) {
                            for (TLRPC.Dialog dialog3 : newDialogs3) {
                                if (dialog3.id == dialogId) {
                                    newDialogs3.remove(dialog3);
                                    break;
                                }
                            }
                        }

                        Collections.sort(newDialogs3, dialogComparator);
                        relatedMeDialogs.put(3, newDialogs3);
                        if (callback != null) {
                            callback.onSuccess(relatedMeDialogs);
                        }
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_DIALOGS_DATA, "2"));

                        int adminNum = CollectionUtils.countMatches(dialogs1, new CollectionUtils.Predicate<TLRPC.Dialog>() {
                            @Override
                            public boolean evaluate(TLRPC.Dialog item) {
                                return item.unread_count > 0;
                            }
                        });
                        int pinnedNum = CollectionUtils.countMatches(dialogs2, new CollectionUtils.Predicate<TLRPC.Dialog>() {
                            @Override
                            public boolean evaluate(TLRPC.Dialog item) {
                                if (item instanceof TLRPC.TL_dialogFolder) {
                                    return MessagesStorage.getInstance(currentAccount).getArchiveUnreadCount() > 0;
                                }
                                return item.unread_count > 0;
                            }
                        });
                        int participantNum = CollectionUtils.countMatches(newDialogs3, new CollectionUtils.Predicate<TLRPC.Dialog>() {
                            @Override
                            public boolean evaluate(TLRPC.Dialog item) {
                                return item.unread_count > 0;
                            }
                        });
                        relatedMeUnRead = adminNum + pinnedNum + participantNum;
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_UNREAD_COUNT, "2", relatedMeUnRead));
                    }
                });
            }
        }, force);
    }

    /**
     * 获取非联系人对话列表
     */
    public void getNonContactDialogs(Callback<List<TLRPC.Dialog>> callback, boolean force) {
        if (!force && Math.abs(TimeUtils.getTimeSpanByNow(lastNonContactTime, TimeConstants.MSEC)) < TIME_INTERVAL) {
            return;
        }
        if (!force && nonContactTask != null && !nonContactTask.isDone()) {
            lastNonContactTime = System.currentTimeMillis();
            return;
        }
        ThreadUtils.executeByCached(nonContactTask = new ThreadUtils.SimpleTask<List<TLRPC.Dialog>>() {
            @Override
            public List<TLRPC.Dialog> doInBackground() throws Throwable {
                ArrayList<TLRPC.Dialog> dialogList = new ArrayList<>();
                ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(getMessagesController().getAllDialogs());
                for (TLRPC.Dialog dialog : dialogs) {
                    if (DialogObject.isUserDialog(dialog.id)) {
                        TLRPC.User user = getMessagesController().getUser(dialog.id);
                        if (user == null || user.contact || user.bot || user.id == 777000) {
                            continue;
                        }
                        dialogList.add(dialog);
                    } else if (DialogObject.isEncryptedDialog(dialog.id)) {
                        int encryptedChatId = DialogObject.getEncryptedChatId(dialog.id);
                        TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(encryptedChatId);
                        if (encryptedChat == null || getMessagesController().getUser(encryptedChat.user_id).contact) {
                            continue;
                        }
                        dialogList.add(dialog);
                    }
                }
                Collections.sort(dialogList, dialogComparator);
                return dialogList;
            }

            @Override
            public void onSuccess(List<TLRPC.Dialog> result) {
                nonContactDialogs = result;
                if (callback != null) {
                    callback.onSuccess(result);
                }
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_DIALOGS_DATA, "3"));

                nonContactUnRead = CollectionUtils.countMatches(result, new CollectionUtils.Predicate<TLRPC.Dialog>() {
                    @Override
                    public boolean evaluate(TLRPC.Dialog item) {
                        return item.unread_count > 0;
                    }
                });
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_UNREAD_COUNT, "3", nonContactUnRead));
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    private Comparator<TLRPC.Dialog> dialogComparator = (dialog1, dialog2) -> {
        MediaDataController mediaDataController = getMediaDataController();
        long date1 = DialogObject.getLastMessageOrDraftDate(dialog1, mediaDataController.getDraft(dialog1.id, 0));
        long date2 = DialogObject.getLastMessageOrDraftDate(dialog2, mediaDataController.getDraft(dialog2.id, 0));
        if (date1 < date2) {
            return 1;
        } else if (date1 > date2) {
            return -1;
        }
        return 0;
    };

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
//        if (id == NotificationCenter.didLoadChatAdmins) {
//            long chatId = (Long) args[0];
//            loadChatAdmins.put(chatId, true);
//            TLRPC.ChannelParticipant p = getMessagesController().getAdminInChannel(getUserConfig().clientUserId, chatId);
//            if (p != null) { // 自己是管理员
//                TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(-chatId);
//                if (dialog != null) {
//                    relatedMe1Dialogs.put(dialog.id, dialog);
//                }
//            }
//        }
    }


    public abstract static class Callback<T> {

        public abstract void onSuccess(T data);

        public void onError(Throwable t) {
            ToastUtils.showLong(t.getMessage());
            Timber.e(t);
        }
    }

    public interface ChatInfoLoadListener {

        void updateChatLoad(ChatInfoEntity chatInfo);
    }
}
