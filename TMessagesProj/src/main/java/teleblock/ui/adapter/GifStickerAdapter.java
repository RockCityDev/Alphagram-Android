package teleblock.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.StickerEmojiCell;

import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;


public class GifStickerAdapter extends BaseMultiItemQuickAdapter<GifStickerEntity, BaseViewHolder> implements LoadMoreModule {

    private Context context;
    private int size;

    public GifStickerAdapter(Context context) {
        this.context = context;
        size = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50)) / 4;
        loadData();
    }

    @NonNull
    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case GifStickerEntity.TYPE_GIF:
                ContextLinkCell cell = new ContextLinkCell(context);
                cell.setLayoutParams(new ViewGroup.LayoutParams(size, size));
                cell.setCanPreviewGif(true);
                return new BaseViewHolder(cell);
            case GifStickerEntity.TYPE_STICKER:
                StickerEmojiCell stickerEmojiCell = new StickerEmojiCell(context, true);
                stickerEmojiCell.setStickerSize(size);
                stickerEmojiCell.setLayoutParams(new ViewGroup.LayoutParams(size, size));
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

    public void loadData() {
        StickerManager.getInstance().loadStickerCollectList(list -> {
            setList(list);
        });
    }
}
