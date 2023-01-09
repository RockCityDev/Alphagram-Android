package teleblock.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ContextLinkCell;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;

public class GifHAdapter extends BaseQuickAdapter<GifStickerEntity, BaseViewHolder> {
    public GifHAdapter() {
        super(R.layout.view_sticker_gif_only_item_style);
    }

    @NonNull
    @Override
    protected BaseViewHolder createBaseViewHolder(@NonNull View view) {
        FrameLayout gif_frame = view.findViewById(R.id.gif_frame);
        ContextLinkCell cell = new ContextLinkCell(getContext());
        cell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cell.setCanPreviewGif(true);
        gif_frame.addView(cell);
        return super.createBaseViewHolder(view);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, GifStickerEntity gifStickerEntity) {
        FrameLayout gif_frame = baseViewHolder.getView(R.id.gif_frame);
        ContextLinkCell cell = (ContextLinkCell) gif_frame.getChildAt(0);
        cell.setGif(gifStickerEntity.data, false);

        ImageView imageView = baseViewHolder.getView(R.id.iv_collect);
        if (gifStickerEntity.collect) {
            imageView.setImageResource(R.drawable.ic_collection_on);
        } else {
            boolean collect = StickerManager.getInstance().isStickerCollected(gifStickerEntity.data);
            if (collect) {
                gifStickerEntity.collect = true;
                imageView.setImageResource(R.drawable.ic_collection_on);
            } else {
                imageView.setImageResource(R.drawable.ic_collection_off);
            }
        }
        imageView.setOnClickListener(view -> {
            if (gifStickerEntity.collect) {
                StickerManager.getInstance().deleteGifStickerCollect(gifStickerEntity.data);
                gifStickerEntity.collect = false;
                imageView.setImageResource(R.drawable.ic_collection_off);
            } else {
                StickerManager.getInstance().collectGifSticker(gifStickerEntity.data, gifStickerEntity.type);
                gifStickerEntity.collect = true;
                imageView.setImageResource(R.drawable.ic_collection_on);
            }
            EventBus.getDefault().post(new MessageEvent(EventBusTags.STICKER_GIF_COLLECT_CHANGE));
        });
    }
}
