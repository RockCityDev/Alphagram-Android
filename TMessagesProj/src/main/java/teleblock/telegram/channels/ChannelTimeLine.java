package teleblock.telegram.channels;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import teleblock.chat.TGChatManager;
import teleblock.file.KKFileTypes;
import teleblock.model.ChannelTagEntity;
import teleblock.util.TGLog;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKFileDownloadStatusManager;
import teleblock.video.KKVideoDownloadListener;


public class ChannelTimeLine implements KKVideoDownloadListener {
    private final List<ChannelMessagesLoader2> messagesLoaders = new ArrayList<>();
    private KKVideoDownloadListener downloadListener;
    private ChannelMessageLoadListener channelMessageLoadListener;

    public ChannelTimeLine() {
    }

    /***
     * 获取chanel消息
     *
     * @param channelIds  0：所有channel
     * @return
     */
    public List<ChannelMessage> refresh(List<Long> channelIds) {
        try {
            long start = System.currentTimeMillis();
            TGLog.debug("fetchChannelIds Start");
            messagesLoaders.clear();
            ArrayList<TLRPC.Chat> channels = fetchChannelIds(channelIds);
            for (TLRPC.Chat channel : channels) {
                messagesLoaders.add(new ChannelMessagesLoader2(channel));
            }
            long end = System.currentTimeMillis();
            TGLog.debug("fetchChannelIds end 耗时" + (end - start) + "毫秒'");
            return loadMoreMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void removeChannel(long channelId) {
        int index = -1;
        for (int i = 0; i < messagesLoaders.size(); i++) {
            ChannelMessagesLoader2 channelMessagesLoader = messagesLoaders.get(i);
            if (channelMessagesLoader.getChat().id == channelId) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            messagesLoaders.remove(index);
        }
    }

    private ChannelMessagesLoader2 latestMessageLoader = null;
    private MessageObject latestMessageObject = null;
    private Object latestMessageLock = new Object();

    public List<ChannelMessage> loadMoreMessages() {
        final List<ChannelMessage> result = new ArrayList<>();
        long start = System.currentTimeMillis();
        while (result.size() < 20) {
            TGLog.debug("===========loadMoreMessages, result size=" + result.size());
            synchronized (latestMessageLock) {
                latestMessageLoader = null;
                latestMessageObject = null;
            }
            if (messagesLoaders == null || messagesLoaders.size() == 0) return result;
            final CountDownLatch countDownLatch = new CountDownLatch(messagesLoaders.size());
            List<ChannelMessagesLoader2> noMoreLoader = new ArrayList<>();
            for (ChannelMessagesLoader2 messagesLoader : messagesLoaders) {
                new Thread(() -> {
                    if (messagesLoader.getChat() != null) TGLog.debug("before getNextMessage for " + messagesLoader.getChat().title);
                    MessageObject messageObject = messagesLoader.getNextMessage();
                    if (messageObject != null) {
                        if (messagesLoader.getChat() != null)
                            TGLog.debug(
                                    "messageObject:" + messageObject.messageOwner.id
                                            + " 消息: " + messageObject.messageOwner.message
                                            + " contentSize:" + (messageObject.messageOwner.replies == null ? 0 : messageObject.messageOwner.replies.replies));
                        synchronized (latestMessageLock) {
                            if (latestMessageObject == null || messageObject.messageOwner.date > latestMessageObject.messageOwner.date) {
                                latestMessageObject = messageObject;
                                latestMessageLoader = messagesLoader;
                            }
                        }
                    } else {
                        if (messagesLoader.getChat() != null) TGLog.debug("messageObject return null for " + messagesLoader.getChat().title);
                        if (!messagesLoader.isHasMore()) {
                            noMoreLoader.add(messagesLoader);
                        } else if (messagesLoader.isRequestFail()) {
                            noMoreLoader.add(messagesLoader);
                            //if (channelMessageLoadListener != null) channelMessageLoadListener.onMessageLoadError();
                        }
                    }
                    countDownLatch.countDown();
                }).start();
            }
            try {
                countDownLatch.await();
                if (latestMessageObject != null && latestMessageLoader != null) {
                    KKFileDownloadStatus status = null;
                    if (KKFileTypes.shouldWatchFileType(FileLoader.getAttachFileName(latestMessageObject.getDocument()))) {
                        KKFileDownloadStatusManager.getInstance().addFileDownloadListener(latestMessageObject, ChannelTimeLine.this);
                        status = KKFileDownloadStatusManager.getInstance().addWatch(latestMessageObject);
                    }
                    result.add(new ChannelMessage(latestMessageLoader.getChat(), latestMessageObject, status));
                    latestMessageLoader.addIndex();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (ChannelMessagesLoader2 loader : noMoreLoader) {
                messagesLoaders.remove(loader);
            }
        }
        long end = System.currentTimeMillis();
        TGLog.debug("加载一页数据共耗时" + (end - start) + "毫秒");
        return result;
    }

    private ArrayList<TLRPC.Chat> fetchChannelIds(List<Long> channelIds) {
        ArrayList<TLRPC.Chat> chats = TGChatManager.getInstance().getAllChats();
        ArrayList<TLRPC.Chat> channels = new ArrayList<>();
        for (TLRPC.Chat chat : chats) {
            if (chat != null && ChatObject.isChannel(chat) && !ChatObject.isMegagroup(chat)) {
                if (channelIds.size() == 1 && channelIds.get(0) == -1) {//全部
                    //过滤私密频道
                    List<Long> ids = null;
                    String secretStr = LocaleController.getString("channel_recommend_tag_secret", R.string.channel_recommend_tag_secret);
                    ChannelTagEntity tagEntity = ChannelTagManager.getInstance().getChannelTagByName(secretStr);
                    if (tagEntity != null) {
                        ids = ChannelTagManager.getInstance().getChannelIdsByTag(tagEntity.tagId);
                    }
                    if (ids != null && ids.size() > 0) {
                        boolean has = false;
                        for (Long id : ids) {
                            if (Math.abs(id) == chat.id) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            channels.add(chat);
                        }
                    } else {
                        channels.add(chat);
                    }
                } else {
                    TLRPC.Chat mathChat = null;
                    for (Long id : channelIds) {
                        if (Math.abs(id) == chat.id) {
                            mathChat = chat;
                            break;
                        }
                    }
                    if (mathChat != null) {
                        channels.add(mathChat);
                    }
                }
            }
        }
        return channels;
    }

    /***
     * 添加下载状态更新回调
     * @param downloadListener
     */
    public void setDownloadFilesListener(KKVideoDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    /***
     * 加载失败
     * @param channelMessageLoadListener
     */
    public void setChannelMessageLoadListener(ChannelMessageLoadListener channelMessageLoadListener) {
        this.channelMessageLoadListener = channelMessageLoadListener;
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        if (downloadListener != null) {
            downloadListener.updateVideoDownloadStatus(fileName, fileDownloadStatus);
        }
    }
}
