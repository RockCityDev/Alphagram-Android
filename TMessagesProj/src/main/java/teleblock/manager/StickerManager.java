package teleblock.manager;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import teleblock.database.KKVideoMessageDB;
import teleblock.model.ChatInfoEntity;
import teleblock.model.GifStickerEntity;
import teleblock.util.EventUtil;
import timber.log.Timber;


public class StickerManager {
    private final Executor taskExecutor = Executors.newSingleThreadExecutor();
    private static StickerManager instance;

    public interface StickerLoadListener {
        void onLoad(List<GifStickerEntity> list);
    }

    public static StickerManager getInstance() {
        if (instance == null) {
            synchronized (StickerManager.class) {
                if (instance == null) {
                    instance = new StickerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 通过sticker获取pack详情
     */
    public void loadStickerAndCollect(TLRPC.InputStickerSet inputStickerSet, ChatActivity.ChatActivityAdapter chatAdapter) {
        TLRPC.TL_messages_getStickerSet req = new TLRPC.TL_messages_getStickerSet();
        req.stickerset = inputStickerSet;
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_messages_stickerSet stickerSet = (TLRPC.TL_messages_stickerSet) response;
                collectStickerSet(stickerSet);
                chatAdapter.notifyDataSetChanged();
            }
        }));
    }

    /**
     * 保存sticker包
     */
    public void collectStickerSet(TLRPC.TL_messages_stickerSet stickerSet) {
        taskExecutor.execute(() -> {
            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertStickerSet(stickerSet);
        });
    }

    /**
     * 收藏收藏gif、sticker
     */
    public void collectGifSticker(TLRPC.Document document, int type) {
        if (document == null) return;
        if (type == 2) {//gif
            EventUtil.track(ApplicationLoader.applicationContext, EventUtil.Even.GIF收藏, new HashMap<>());
        } else if (type == 3) {//sticker
            EventUtil.track(ApplicationLoader.applicationContext, EventUtil.Even.表情收藏, new HashMap<>());
        }
        taskExecutor.execute(() -> {
            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertGifSticker(document, type);
            AndroidUtilities.runOnUIThread(() -> {
                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.loadGifStickerData);
            });
        });
    }

    /**
     * 删除gif、sticker收藏
     */
    public void deleteGifStickerCollect(TLRPC.Document document) {
        if (document == null) return;
        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteSticker(document.id);
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.loadGifStickerData);
    }

    /**
     * 查询sticker是否收藏
     */
    public boolean isStickerCollected(TLRPC.Document document) {
        if (document == null) return false;
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryStickerExists(document.id);
    }

    /**
     * 加载sticker收藏列表
     */
    public void loadStickerCollectList(StickerLoadListener listener) {
        taskExecutor.execute(() -> {
            List<GifStickerEntity> list = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryStickers();
            AndroidUtilities.runOnUIThread(() -> listener.onLoad(list));
        });
    }

    /**
     * 监听处理StickerGif消息
     *
     * @param dialog_id
     * @param arr
     */
    public void watchStickerGifMessage(long dialog_id, ArrayList<MessageObject> arr) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                for (MessageObject messageObject : arr) {
                    dealStickerGifMessage(dialog_id, messageObject);
                }
                return null;
            }

            @Override
            public void onSuccess(Object result) {
            }

            @Override
            public void onFail(Throwable t) {
            }
        });
    }

    public void dealStickerGifMessage(long dialog_id, MessageObject messageObject) {
        TLRPC.Document document = messageObject.getDocument();
        if (MessageObject.isStickerDocument(document) || MessageObject.isAnimatedStickerDocument(document, true)) {
            //sticker
            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertMessageStickerGif(Math.abs(dialog_id), document, GifStickerEntity.TYPE_STICKER);
        } else if (MessageObject.isGifDocument(document, messageObject.hasValidGroupId())) {
            //gif
            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertMessageStickerGif(Math.abs(dialog_id), document, GifStickerEntity.TYPE_GIF);
        }
    }

    /***
     * 加载抓取的StickerGif
     * @param page
     * @param size
     * @param type
     * @param listener
     */
    public void loadStickerGifMessageList(int page, int size, int type, StickerLoadListener listener) {
        taskExecutor.execute(() -> {
            List<GifStickerEntity> list = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryMessageStickerGifList(page, size, type);
            AndroidUtilities.runOnUIThread(() -> listener.onLoad(list));
        });
    }

}
