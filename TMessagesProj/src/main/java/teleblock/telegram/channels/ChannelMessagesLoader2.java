package teleblock.telegram.channels;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import teleblock.util.TGLog;

/***
 * 优化了些，没必要的方法
 */
public class ChannelMessagesLoader2 implements NotificationCenter.NotificationCenterDelegate {
    private final TLRPC.Chat chat;
    private final int classGuid;
    private int lastLoadIndex = 1;
    private int[] maxMessageId = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
    private int[] maxDate = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
    private int[] minDate = new int[2];
    private boolean[] cacheEndReached = new boolean[2];
    private int replyMaxReadId = 0;
    private int requestCount = 10;//每次加载个数
    private boolean requestFail = false;

    private boolean hasMore = true;
    private List<MessageObject> currentMessages = new ArrayList<>();
    private int currentIndex = 0;
    private CountDownLatch loadingCountDownLatch = null;
    private MessageObject lastMessageObject;

    public ChannelMessagesLoader2(TLRPC.Chat chat) {
        this.chat = chat;
        this.classGuid = ConnectionsManager.generateClassGuid();
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.messagesDidLoad);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.loadingMessagesFailed);
    }

    public TLRPC.Chat getChat() {
        return this.chat;
    }

    public void addIndex() {
        currentIndex++;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public boolean isRequestFail() {
        return requestFail;
    }

    public synchronized MessageObject getNextMessage() {
        if (!hasMore) {
            return null;
        }
        if (currentIndex < currentMessages.size()) {
            lastMessageObject = currentMessages.get(currentIndex);
            return lastMessageObject;
        } else {
            TGLog.debug("getNextMessage, loadMore => " + chat.title);
            long start = System.currentTimeMillis();
            if (loadingCountDownLatch != null) {
                return null;
            }
            requestFail = false;
            currentIndex = 0;
            currentMessages.clear();
            loadingCountDownLatch = new CountDownLatch(1);
            loadMore();
            try {
                loadingCountDownLatch.await();
                loadingCountDownLatch = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentIndex < currentMessages.size()) {
                //其实最早一条消息是创建频道的消息，id=1
                if (lastMessageObject != null && currentMessages.get(currentIndex).messageOwner.id == lastMessageObject.messageOwner.id && lastMessageObject.messageOwner.id == 1) {
                    hasMore = false;
                    TGLog.debug("noMore Messages =>" + chat.title);
                    return null;
                }
                lastMessageObject = currentMessages.get(currentIndex);
                long end = System.currentTimeMillis();

                TGLog.debug(chat.title + "加载消息耗时" + (end - start) + "毫秒");
                return lastMessageObject;
            } else {
                if (requestFail) {
                    return null;
                }
                hasMore = false;
                return null;
            }
        }
    }

    private void loadMore() {
        TGLog.debug("maxMessageId[0] = " + maxMessageId[0] + " , !cacheEndReached[0]=" + !cacheEndReached[0] + " , minDate[0]=" + minDate[0]);
        getMessagesController().loadMessages(-chat.id, 0, false, requestCount, maxMessageId[0], 0, !cacheEndReached[0], minDate[0], classGuid, 0, 0, 0, 0, replyMaxReadId, lastLoadIndex++,false);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.messagesDidLoad) {
            int guid = (Integer) args[10];
            if (guid == classGuid) {
                ArrayList<MessageObject> messArr = (ArrayList<MessageObject>) args[2];
                long did = (Long) args[0];
                int loadIndex = did == -chat.id ? 0 : 1;
                int load_type = (Integer) args[8];
                boolean isCache = (Boolean) args[3];
                int count = (Integer) args[1];

                if (load_type == 1) {
                    Collections.reverse(messArr);
                }
                if ((load_type == 1 || load_type == 3) && loadIndex == 1) {
                    cacheEndReached[0] = true;
                }
                TLRPC.MessageAction dropPhotoAction = null;
                for (int a = 0; a < messArr.size(); a++) {
                    MessageObject obj = messArr.get(a);
                    int messageId = obj.getId();
                    if (obj.messageOwner instanceof TLRPC.TL_messageEmpty) {
                        continue;
                    }
                    if (loadIndex == 0 && messageId == 1) {
                        cacheEndReached[loadIndex] = true;
                    }

                    if (messageId > 0) {
                        maxMessageId[loadIndex] = Math.min(messageId, maxMessageId[loadIndex]);
                    }

                    if (obj.messageOwner.date != 0) {
                        maxDate[loadIndex] = Math.max(maxDate[loadIndex], obj.messageOwner.date);
                        if (minDate[loadIndex] == 0 || obj.messageOwner.date < minDate[loadIndex]) {
                            minDate[loadIndex] = obj.messageOwner.date;
                        }
                    }

                    TLRPC.MessageAction action = obj.messageOwner.action;
                    if (obj.type < 0 || loadIndex == 1 && action instanceof TLRPC.TL_messageActionChatMigrateTo) {
                        continue;
                    }
                    if (chat != null && chat.creator && (action instanceof TLRPC.TL_messageActionChatCreate || dropPhotoAction != null && action == dropPhotoAction)) {
                        continue;
                    }
                    if (obj.messageOwner.action instanceof TLRPC.TL_messageActionChannelMigrateFrom) {
                        continue;
                    }
                }

                if (load_type == 1) {
                } else {
                    if (messArr.size() < count && load_type != 3 && load_type != 4) {
                        if (isCache) {
                            if (load_type != 2) {
                                cacheEndReached[loadIndex] = true;
                            }
                        }
                    }
                }

                if (messArr != null && messArr.size() > 0) {
                    TGLog.debug("MessagesDidLoad,  size= " + messArr.size() + "，title= " + chat.title);
                    TGLog.debug("new top MessageId = " + messArr.get(0).messageOwner.id);
                    currentMessages.addAll(messArr);
                }

                if (loadingCountDownLatch != null) {
                    loadingCountDownLatch.countDown();
                }
            }
        } else if (id == NotificationCenter.loadingMessagesFailed) {
            if (args != null && args.length > 0) {
                int guid = (Integer) args[0];
                if (guid == classGuid) {
                    requestFail = true;
                    if (loadingCountDownLatch != null) {
                        loadingCountDownLatch.countDown();
                    }
                }
            }
        }
    }

    private MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

}
