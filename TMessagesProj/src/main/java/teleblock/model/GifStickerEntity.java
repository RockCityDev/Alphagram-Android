package teleblock.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.telegram.tgnet.TLRPC;

public class GifStickerEntity implements MultiItemEntity {

    public static final int STICKER_PACK = 1;
    public static final int TYPE_GIF = 2;
    public static final int TYPE_STICKER = 3;

    public int type; // 1：sticker-set, 2：gif, 3:sticker_document
    public long date;
    public long dialog_id;
    public long mid;
    public TLRPC.TL_messages_stickerSet stickerSet;
    public TLRPC.Document data;

    //判断有，后面加的
    public boolean collect;

    @Override
    public int getItemType() {
        return type;
    }
}
