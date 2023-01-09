package teleblock.manager;

import android.util.SparseArray;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import teleblock.database.KKVideoMessageDB;
import teleblock.model.DBDeleteMsgEntity;
import teleblock.model.DeleteMessageEntity;
import teleblock.util.MMKVUtil;


public class DeletedMessageManager {

    public interface DeletedMessageLoadListener {
        void onLoad(List<DeleteMessageEntity> list);
    }

    public interface DeletedMessageDeleteListener {
        void onComplete();
    }

    private static DeletedMessageManager instance;
    private final Executor taskExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<DBDeleteMsgEntity> messageTasks = new LinkedBlockingDeque<>();

    public static DeletedMessageManager getInstance() {
        if (instance == null) {
            synchronized (DeletedMessageManager.class) {
                if (instance == null) {
                    instance = new DeletedMessageManager();
                }
            }
        }
        return instance;
    }

    private DeletedMessageManager() {
        new Thread(() -> {
            while (true) {
                executeTask();
            }
        }).start();
    }

    private void executeTask() {
        try {
            DBDeleteMsgEntity dbDeleteMsgEntity = messageTasks.take();
            if (dbDeleteMsgEntity != null && dbDeleteMsgEntity.data != null) {
                KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertDeletedMessage(dbDeleteMsgEntity);
                if (MMKVUtil.deleteMessageSwitch()) {
                    KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteDeletedMessageByTime();//定时删除
                }
            }
            Thread.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void insertDeleteMessage(List<DBDeleteMsgEntity> list) {
        if (list.size() == 0) return;
        for (DBDeleteMsgEntity entity : list) {
            try {
                messageTasks.put(entity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadDeleteMessageList(boolean isGroup, int groupType, DeletedMessageLoadListener listener) {
        loadDeleteMessageList(isGroup, groupType, 0, listener);
    }

    public void loadDeleteMessageList(long dialogId, DeletedMessageLoadListener listener) {
        loadDeleteMessageList(false, -1, dialogId, listener);
    }

    public void loadDeleteMessageList(boolean isGroup, int groupType, long dialogId, DeletedMessageLoadListener listener) {
        final SparseArray<File> paths = ImageLoader.getInstance().createMediaPaths();
        File[] folders = new File[]{
                paths.get(FileLoader.MEDIA_DIR_IMAGE),
                paths.get(FileLoader.MEDIA_DIR_AUDIO),
                paths.get(FileLoader.MEDIA_DIR_VIDEO),
                paths.get(FileLoader.MEDIA_DIR_DOCUMENT),
                paths.get(FileLoader.MEDIA_DIR_CACHE)
        };
        taskExecutor.execute(() -> {
            List<DeleteMessageEntity> deleteMessageEntityList = new ArrayList<>();
            List<TLRPC.Message> list = new ArrayList<>();
            if (isGroup) {
                list = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryDeleteMessageByGroup(groupType);
            } else {
                list = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryDeleteMessageById(dialogId);
            }
            for (TLRPC.Message message : list) {
                MessageObject messageObject = new MessageObject(UserConfig.selectedAccount, message, false, false);
                String attachFileName = FileLoader.getAttachFileName(messageObject.getDocument());
                for (File folder : folders) {
                    File attachFile = new File(folder, attachFileName);
                    if (attachFile.exists()) {
                        messageObject.attachPathExists = true;
                    }
                }
                deleteMessageEntityList.add(new DeleteMessageEntity(messageObject));
            }
            AndroidUtilities.runOnUIThread(() -> listener.onLoad(deleteMessageEntityList));
        });
    }

    public void deleteDeletedMessage(List<DeleteMessageEntity> list, DeletedMessageDeleteListener listener) {
        taskExecutor.execute(() -> {
            for (DeleteMessageEntity entity : list) {
                KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteDeletedMessage(entity.getMessageId());
            }
            if (listener != null) {
                listener.onComplete();
            }
        });
    }

    public void deleteAllDeletedMessage(DeletedMessageDeleteListener listener) {
        taskExecutor.execute(() -> {
            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteDeletedMessageAll();
            if (listener != null) {
                listener.onComplete();
            }
        });
    }
}
