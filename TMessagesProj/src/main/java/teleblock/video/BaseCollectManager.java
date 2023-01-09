package teleblock.video;

import android.util.SparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import teleblock.database.KKVideoMessageDB;
import teleblock.file.KKFileMessage;
import teleblock.util.TGLog;


/**
 * 消息收藏状态管理
 */
public abstract class BaseCollectManager<T extends KKFileMessage> implements KKVideoDownloadListener {

    private final Executor taskExecutor = Executors.newSingleThreadExecutor();
    private final List<WeakReference<KKVideoDownloadListener>> downloadListeners = new ArrayList<>();

    /**
     * 收藏的消息回传接口
     */
    public interface Listener<T extends KKFileMessage> {
        /**
         * @param videoMessages 收藏的消息
         */
        public void onCollectMessagesLoad(List<T> videoMessages);
    }

    protected abstract T createKKMessage(KKMessage message, MessageObject messageObject, KKFileDownloadStatus status);

    /***
     * 加载收藏消息
     * @param listener
     */
    public void loadCollectMessageList(Listener<T> listener) {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<T> kkMessages = loadCollectMessageListSync();
                listener.onCollectMessagesLoad(kkMessages);
            }
        });
    }

    /***
     * 同步加载收藏消息
     */
    public List<T> loadCollectMessageListSync() {
        final SparseArray<File> paths = ImageLoader.getInstance().createMediaPaths();
        File[] folders = new File[]{
                paths.get(FileLoader.MEDIA_DIR_IMAGE),
                paths.get(FileLoader.MEDIA_DIR_AUDIO),
                paths.get(FileLoader.MEDIA_DIR_VIDEO),
                paths.get(FileLoader.MEDIA_DIR_DOCUMENT),
                paths.get(FileLoader.MEDIA_DIR_CACHE)
        };
        List<KKMessage> messages = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).loadCollectMessages();
        List<T> kkMessages = new ArrayList<>();

        for (KKMessage message : messages) {
            MessageObject messageObject = new MessageObject(UserConfig.selectedAccount, message.getMessage(), false, false);
            String attachFileName = FileLoader.getAttachFileName(messageObject.getDocument());
            for (File folder : folders) {
                File attachFile = new File(folder, attachFileName);
                if (attachFile.exists()) {
                    messageObject.attachPathExists = true;
                }
            }
            KKFileDownloadStatusManager.getInstance().addFileDownloadListener(messageObject, BaseCollectManager.this);
            KKFileDownloadStatus status = KKFileDownloadStatusManager.getInstance().addWatch(messageObject);
            kkMessages.add(createKKMessage(message, messageObject, status));
        }
        TGLog.debug("collectMessages Size" + kkMessages.size());
        return kkMessages;
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        for (WeakReference<KKVideoDownloadListener> wr : downloadListeners) {
            KKVideoDownloadListener downloadListener = wr.get();
            if (downloadListener != null) {
                downloadListener.updateVideoDownloadStatus(fileName, fileDownloadStatus);
            }
        }
    }

    /***
     * 添加下载状态更新回调
     * @param downloadListener
     */
    public void addDownloadFilesListener(KKVideoDownloadListener downloadListener) {
        downloadListeners.add(new WeakReference<>(downloadListener));
    }

    /**
     * 记录MessageObject对应的视频下载状态
     *
     * @param kkFileMessage
     */
    public void collectMessage(final T kkFileMessage) {
        collectMessage(kkFileMessage.getMessageObject());
    }

    public void collectMessage(MessageObject messageObject) {
        collectMessage(messageObject, null);
    }

    public void collectMessage(MessageObject messageObject, Runnable callback) {
        if (messageObject == null) return;
        final long collectTime = System.currentTimeMillis();
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                KKVideoMessageDB.getInstance(UserConfig.selectedAccount).collectMessage(messageObject.messageOwner, collectTime, FileLoader.getAttachFileName(messageObject.getDocument()));
                if (callback != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.run();
                        }
                    });
                }
            }
        });
    }

    /***
     * 移除收藏
     * @param kkFileMessage
     */
    public void removeCollect(final T kkFileMessage) {
        removeCollect(kkFileMessage.getMessageObject(), null);
    }

    public void removeCollect(MessageObject messageObject) {
        removeCollect(messageObject, null);
    }

    public void removeCollect(MessageObject messageObject, Runnable callback) {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                KKVideoMessageDB.getInstance(UserConfig.selectedAccount).removeCollectMessageByMessageId(messageObject.getId(), messageObject.getDialogId());
                if (callback != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.run();
                        }
                    });
                }
            }
        });
    }

    //收藏状态
    public boolean getCollectStatus(final T kkFileMessage) {
        return getCollectStatus(kkFileMessage.getMessageObject());
    }

    public boolean getCollectStatus(MessageObject messageObject) {
        if (messageObject == null) return false;
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).collectStatus(messageObject.getId(), messageObject.getDialogId());
    }
}
