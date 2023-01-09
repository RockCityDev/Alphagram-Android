package teleblock.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ContextLinkCell;

import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;

public class GifOnlyAdapter extends BaseDelegateMultiAdapter<GifStickerEntity, BaseViewHolder> implements LoadMoreModule {
    public GifOnlyAdapter() {
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<GifStickerEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends GifStickerEntity> data, int position) {
                return position % 6;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(0, R.layout.view_sticker_gif_only_item_style1)
                .addItemType(1, R.layout.view_sticker_gif_only_item_style2)
                .addItemType(2, R.layout.view_sticker_gif_only_item_style3)
                .addItemType(3, R.layout.view_sticker_gif_only_item_style4)
                .addItemType(4, R.layout.view_sticker_gif_only_item_style5)
                .addItemType(5, R.layout.view_sticker_gif_only_item_style6);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, GifStickerEntity gifStickerEntity) {
        FrameLayout gif_frame = baseViewHolder.getView(R.id.gif_frame);
        gif_frame.removeAllViews();
        ContextLinkCell cell = new ContextLinkCell(getContext());
        cell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cell.setCanPreviewGif(true);
        cell.setGif(gifStickerEntity.data, false);
        gif_frame.addView(cell);

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
