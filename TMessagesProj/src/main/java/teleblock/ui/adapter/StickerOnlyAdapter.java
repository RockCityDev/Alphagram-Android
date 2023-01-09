package teleblock.ui.adapter;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.StickerEmojiCell;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;

public class StickerOnlyAdapter extends BaseMultiItemQuickAdapter<GifStickerEntity, BaseViewHolder> implements LoadMoreModule {

    @NonNull
    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        int height = ConvertUtils.dp2px(97);
        StickerEmojiCell stickerEmojiCell = new StickerEmojiCell(getContext(), true);
        stickerEmojiCell.setStickerSize(height);

        ImageView imageView = new ImageView(getContext());
        imageView.setTag("collect");
        imageView.setPadding(ConvertUtils.dp2px(4), ConvertUtils.dp2px(4), ConvertUtils.dp2px(4), ConvertUtils.dp2px(4));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        stickerEmojiCell.addView(imageView, params);

        stickerEmojiCell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return new BaseViewHolder(stickerEmojiCell);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, GifStickerEntity entity) {
        TLRPC.Document sticker = entity.data;
        StickerEmojiCell stickerEmojiCell = (StickerEmojiCell) holder.itemView;
        stickerEmojiCell.setSticker(sticker, entity.stickerSet, false);
        stickerEmojiCell.setRecent(false);

        ImageView imageView = stickerEmojiCell.findViewWithTag("collect");
        if (entity.collect) {
            imageView.setImageResource(R.drawable.ic_collection_on);
        } else {
            boolean collect = StickerManager.getInstance().isStickerCollected(entity.data);
            if (collect) {
                entity.collect = true;
                imageView.setImageResource(R.drawable.ic_collection_on);
            } else {
                imageView.setImageResource(R.drawable.ic_collection_off);
            }
        }
        imageView.setOnClickListener(view -> {
            if (entity.collect) {
                StickerManager.getInstance().deleteGifStickerCollect(entity.data);
                entity.collect = false;
                imageView.setImageResource(R.drawable.ic_collection_off);
            } else {
                StickerManager.getInstance().collectGifSticker(entity.data, entity.type);
                entity.collect = true;
                imageView.setImageResource(R.drawable.ic_collection_on);
            }
            EventBus.getDefault().post(new MessageEvent(EventBusTags.STICKER_GIF_COLLECT_CHANGE));
        });
    }
}
