package teleblock.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BackupImageView;

import java.util.List;

import teleblock.model.CollectFileEntity;
import teleblock.util.SystemUtil;

/**
 * 我的收藏列表
 */
public class MyCollectRvAdapter extends BaseDelegateMultiAdapter<CollectFileEntity, BaseViewHolder> implements LoadMoreModule {
    Context context;
    boolean delete;

    public MyCollectRvAdapter(Context context) {
        this.context = context;
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<CollectFileEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends CollectFileEntity> data, int position) {
                return position % 3;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(0, R.layout.view_mycollect_item_style1)
                .addItemType(1, R.layout.view_mycolloct_item_style2)
                .addItemType(2, R.layout.view_mycollect_item_style3);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CollectFileEntity entity) {
        try {
            showItem(baseViewHolder, entity);
        } catch (Exception e) {
        }
    }

    public boolean isDeleteModel(){
        return this.delete;
    }

    public void deleteModel(boolean delete) {
        this.delete = delete;
        this.notifyDataSetChanged();
    }

    public void deleteItem(int id) {
        int position = -1;
        for (int i = 0; i < getData().size(); i++) {
            CollectFileEntity entity = getItem(i);
            if (entity.message.getId() == id) {
                position = i;
                break;
            }
        }
        if (position > -1) {
            getData().remove(position);
            notifyItemRemoved(position);
        }
    }

    private void showItem(BaseViewHolder baseViewHolder, CollectFileEntity entity) {
        FrameLayout imageLayout = baseViewHolder.findView(R.id.imageLayout);
        //视频封面
        BackupImageView ivThumb = new BackupImageView(context);
        if (entity.message.getDocument() != null && entity.message.getDocument().thumbs != null) {
            TLRPC.PhotoSize bigthumb = FileLoader.getClosestPhotoSizeWithSize(entity.message.getDocument().thumbs, 320);
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(entity.message.getDocument().thumbs, 40);
            if (thumb == bigthumb) {
                bigthumb = null;
            }
            ivThumb.setRoundRadius(AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
            ivThumb.getImageReceiver().setNeedsQualityThumb(bigthumb == null);
            ivThumb.getImageReceiver().setShouldGenerateQualityThumb(bigthumb == null);
            ivThumb.setImage(ImageLocation.getForDocument(bigthumb, entity.message.getDocument()), "168_198", ImageLocation.getForDocument(thumb, entity.message.getDocument()), "168_198_b", null, 0, 1, entity.message.getMessageObject());
            imageLayout.addView(ivThumb);
        }

        View holdView = baseViewHolder.findView(R.id.holdView);
        ImageView ivItemPlay = baseViewHolder.findView(R.id.iv_itemplay);
        ImageView ivCheck = baseViewHolder.findView(R.id.iv_check);
        if (delete) {
            ivItemPlay.setVisibility(View.GONE);
            holdView.setVisibility(View.VISIBLE);
            ivCheck.setVisibility(View.VISIBLE);
        } else {
            ivItemPlay.setVisibility(View.VISIBLE);
            holdView.setVisibility(View.GONE);
            ivCheck.setVisibility(View.GONE);
        }
        if (entity.deleteSelect) {
            ivCheck.setImageResource(R.drawable.ic_check_yes);
        } else {
            ivCheck.setImageResource(R.drawable.ic_check_no2);
        }

        baseViewHolder.setText(R.id.tv_size, SystemUtil.getSizeFormat(entity.message.getSize()));
        if (entity.message.getMediaDuration() > 0) {
            baseViewHolder.setVisible(R.id.tv_length, true);
            baseViewHolder.setText(R.id.tv_length, SystemUtil.timeTransfer(entity.message.getMediaDuration()));
        } else {
            baseViewHolder.setVisible(R.id.tv_length, false);
        }
        baseViewHolder.setText(R.id.tv_from, entity.message.getFromName());
    }
}
