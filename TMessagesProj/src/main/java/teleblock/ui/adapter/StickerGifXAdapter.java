package teleblock.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.StickerEmojiCell;

import teleblock.model.GifStickerEntity;


public class StickerGifXAdapter extends BaseMultiItemQuickAdapter<GifStickerEntity, BaseViewHolder> {
    int position = -1;
    private Context context;

    public StickerGifXAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        position++;
        int height = ConvertUtils.dp2px(97);
        switch (viewType) {
            case GifStickerEntity.TYPE_GIF:
                ContextLinkCell cell = new ContextLinkCell(context);
                cell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                cell.setCanPreviewGif(true);
                return new BaseViewHolder(cell);
            case GifStickerEntity.TYPE_STICKER:
                StickerEmojiCell stickerEmojiCell = new StickerEmojiCell(context, true);
                stickerEmojiCell.setStickerSize(height);
                stickerEmojiCell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                return new BaseViewHolder(stickerEmojiCell);
            default:
                return super.onCreateDefViewHolder(parent, viewType);
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, GifStickerEntity entity) {
        switch (holder.getItemViewType()) {
            case GifStickerEntity.TYPE_GIF:
                ContextLinkCell cell = (ContextLinkCell) holder.itemView;
                cell.setGif(entity.data, false);
                break;
            case GifStickerEntity.TYPE_STICKER:
                TLRPC.Document sticker = entity.data;
                StickerEmojiCell stickerEmojiCell = (StickerEmojiCell) holder.itemView;
                stickerEmojiCell.setSticker(sticker, entity.stickerSet, false);
                stickerEmojiCell.setRecent(false);
                break;
        }
    }
}
