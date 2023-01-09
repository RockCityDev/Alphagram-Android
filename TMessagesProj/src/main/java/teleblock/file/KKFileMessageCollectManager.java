package teleblock.file;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import teleblock.video.BaseCollectManager;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKMessage;


public class KKFileMessageCollectManager extends BaseCollectManager<KKFileMessage> {

    private static KKFileMessageCollectManager instance = null;

    private KKFileMessageCollectManager() {
    }

    public static KKFileMessageCollectManager getInstance() {
        if (instance == null) {
            synchronized (KKFileMessageCollectManager.class) {
                if (instance == null) {
                    instance = new KKFileMessageCollectManager();
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
}
