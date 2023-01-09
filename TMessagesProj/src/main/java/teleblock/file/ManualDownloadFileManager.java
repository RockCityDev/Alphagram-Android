package teleblock.file;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.Bulletin;

import teleblock.database.KKVideoMessageDB;
import teleblock.video.BaseLocalFileManager;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKMessage;


public class ManualDownloadFileManager extends BaseLocalFileManager<KKFileMessage> {

    private static ManualDownloadFileManager instance = null;

    private ManualDownloadFileManager() {
    }

    public static ManualDownloadFileManager getInstance() {
        if (instance == null) {
            synchronized (ManualDownloadFileManager.class) {
                if (instance == null) {
                    instance = new ManualDownloadFileManager();
                }
            }
        }
        return instance;
    }

    @Override
    protected KKFileMessage createKKMessage(KKMessage message, MessageObject messageObject, KKFileDownloadStatus status) {
        KKFileTypes type = KKFileTypes.UNKNOWN;
        TLRPC.Document document = messageObject.getDocument();
        if (document != null) {
            String suffix = FileLoader.getDocumentSuffix(document);
            type = KKFileTypes.parseFileType(suffix);
        }
        KKFileMessage kkFileMessage = new KKFileMessage(messageObject, status, type);
        kkFileMessage.setDownloadTime(message.getDownloadTime());
        return kkFileMessage;
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        super.updateVideoDownloadStatus(fileName, fileDownloadStatus);
        if (fileDownloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
            // 保存到相册
            if (KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryMessageFlag(fileName,"save_album")){
                MediaController.saveFile(fileDownloadStatus.getVideoFile().toString(), ApplicationLoader.applicationContext, 1, null, null, new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SAVE_GALLERY);
                    }
                });
            }
        }
    }
}
