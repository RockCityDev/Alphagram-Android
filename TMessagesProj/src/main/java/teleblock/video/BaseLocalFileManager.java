package teleblock.video;

import android.text.TextUtils;
import android.util.SparseArray;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import teleblock.database.KKVideoMessageDB;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileTypes;
import teleblock.util.TGLog;

/**
 * 用户手动下载状态管理
 */
public abstract class BaseLocalFileManager<T extends KKFileMessage> implements KKVideoDownloadListener {

    private final Executor taskExecutor = Executors.newSingleThreadExecutor();
    private List<T> currentMessages = new ArrayList<>();
    private boolean pendingCheckUpdate = false;
    private Set<String> downloadedSets = new HashSet<>();

    public void removeLocalRecord(String fileName) {
        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).removeMessageByAttachFileName(fileName);
    }

    protected abstract T createKKMessage(KKMessage message, MessageObject messageObject, KKFileDownloadStatus status);

    //消息保存来源 ""：所有的，"user":手动下载的
    public String getMessageFlag() {
        return "";
    }

    private void checkUpdate(final boolean force) {
        checkUpdate(force, "");
    }

    public void checkUpdate(final boolean force, String messageFlag) {
        if (!force && pendingCheckUpdate) {
            TGLog.debug("pendingCheckUpdate, return");
            return;
        } else {
            pendingCheckUpdate = true;
        }
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TGLog.debug("execute checkUpdate");
                pendingCheckUpdate = false;
                final SparseArray<File> paths = ImageLoader.getInstance().createMediaPaths();
                File[] folders = new File[]{
                        paths.get(FileLoader.MEDIA_DIR_IMAGE),
                        paths.get(FileLoader.MEDIA_DIR_AUDIO),
                        paths.get(FileLoader.MEDIA_DIR_VIDEO),
                        paths.get(FileLoader.MEDIA_DIR_DOCUMENT),
                        paths.get(FileLoader.MEDIA_DIR_CACHE)
                };
                List<KKMessage> messages = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).loadMessages(messageFlag);
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
                    KKFileDownloadStatus status = KKFileDownloadStatusManager.getInstance().addWatch(messageObject);
                    kkMessages.add(createKKMessage(message, messageObject, status));
                    if (status != null && status.getStatus() != KKFileDownloadStatus.Status.DOWNLOADED) {
                        KKFileDownloadStatusManager.getInstance().addFileDownloadListener(messageObject, BaseLocalFileManager.this);
                    }
                }
                boolean needNotify = false;
                if (kkMessages.size() != currentMessages.size()) {
                    needNotify = true;
                } else {
                    int size = kkMessages.size();
                    for (int i = 0; i < size; i++) {
                        if (kkMessages.get(i).getId() != currentMessages.get(i).getId()) {
                            needNotify = true;
                            break;
                        }
                        if (kkMessages.get(i).getDownloadStatus() != null) {
                            TGLog.debug("new status:" + kkMessages.get(i).getDownloadStatus().getStatus() + ", current status:" + currentMessages.get(i).getDownloadStatus().getStatus());
                            if (kkMessages.get(i).getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED && !downloadedSets.contains(kkMessages.get(i).getFileName())) {
                                downloadedSets.add(kkMessages.get(i).getFileName());
                                needNotify = true;
                                break;
                            }
                        }
                    }
                }
                TGLog.debug("needNotify:" + needNotify);
                if (force || needNotify) {
                    currentMessages = kkMessages;
                    notifyListeners(messageFlag);
                }
            }
        });
    }

    private void notifyListeners(String messageFlag) {
        for (WeakReference<Listener<T>> listener : listeners) {
            Listener l = listener.get();
            if (l != null) {
                l.onLocalVideoFilesUpdate(currentMessages, messageFlag);
            }
        }
    }

    //带标示的消息id
    public List<Integer> getFlagMessageIds(String flag) {
        if (TextUtils.isEmpty(flag)) {
            return new ArrayList<>();
        }
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).loadFlagMessageIds(flag);
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        for (WeakReference<KKVideoDownloadListener> wr : downloadListeners) {
            KKVideoDownloadListener downloadListener = wr.get();
            if (downloadListener != null) {
                downloadListener.updateVideoDownloadStatus(fileName, fileDownloadStatus);
            }
        }
        TGLog.debug("updateVideoStatus:" + fileDownloadStatus);
        if (fileDownloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
            if (!downloadedSets.contains(fileName)) {
                TGLog.debug("checkUpdate");
                checkUpdate(false);
            }
        }
    }

    /***
     * 重新获取数据
     */
    public void refresh() {
        checkUpdate(true);
    }

    /**
     * 已下载、下载中的所有视频消息列表更新接口
     */
    public interface Listener<T extends KKFileMessage> {
        /**
         * @param videoMessages 已下载、下载中的所有视频消息
         * @param messageFlag   消息保存来源
         */
        public void onLocalVideoFilesUpdate(List<T> videoMessages, String messageFlag);
    }

    List<WeakReference<Listener<T>>> listeners = new ArrayList<>();
    List<WeakReference<KKVideoDownloadListener>> downloadListeners = new ArrayList<>();

    /**
     * 添加本地视屏文件下载状态监听
     *
     * @param listener         已下载、下载中的所有视频消息列表更新接口
     * @param downloadListener 视频文件下载进度回调接口
     */
    public void addLocalVideoFilesListener(Listener<T> listener, KKVideoDownloadListener downloadListener) {
        listeners.add(new WeakReference<>(listener));
        downloadListeners.add(new WeakReference<>(downloadListener));
    }

    /**
     * 记录MessageObject对应的视频下载状态
     *
     * @param messageObject
     */
    public void watchMessageObject(final MessageObject messageObject) {
        watchMessageObject(messageObject, "");
    }

    public void watchMessageObject(final MessageObject messageObject, String messageFlag) {
        if (shouldWatchFileType(FileLoader.getAttachFileName(messageObject.getDocument()))) {
            final long downloadTime = System.currentTimeMillis();
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    KKVideoMessageDB.getInstance(UserConfig.selectedAccount).putMessage(messageObject.messageOwner, downloadTime, FileLoader.getAttachFileName(messageObject.getDocument()), messageFlag);
                    checkUpdate(false);
                }
            });
        }
    }

    /**
     * 更新消息保存来源
     */
    public void updateMessageFlag(MessageObject messageObject, String messageFlag) {
        final long downloadTime = System.currentTimeMillis();
        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).putMessage(messageObject.messageOwner, downloadTime, FileLoader.getAttachFileName(messageObject.getDocument()), messageFlag);
    }

    private boolean shouldWatchFileType(String localFileName) {
        for (KKFileTypes value : KKFileTypes.values()) {
            String suffix = value.getSuffix();
            if (suffix.startsWith(".")) {
                if ((localFileName.toLowerCase()).endsWith(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }
}
