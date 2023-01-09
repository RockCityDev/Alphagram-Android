package teleblock.ui.adapter;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ruffian.library.widget.RFrameLayout;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

import teleblock.model.MoreFuncEntity;

public class MoreFuncRvAdapter extends BaseDelegateMultiAdapter<MoreFuncEntity, BaseViewHolder> {
    Context context;

    public MoreFuncRvAdapter(Context context) {
        this.context = context;
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<MoreFuncEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends MoreFuncEntity> data, int position) {
                return 0;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate().addItemType(0, R.layout.view_more_func_item);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, MoreFuncEntity entity) {
        switch (baseViewHolder.getItemViewType()) {
            case 0:
                baseViewHolder.setText(R.id.tv_func, entity.text);
                RFrameLayout iconFrame = baseViewHolder.findView(R.id.icon_frame);
                iconFrame.getHelper().setBackgroundColorNormal(Theme.getColor(Theme.key_chats_actionBackground));
                ImageView imageView = baseViewHolder.findView(R.id.iv_func);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                if (entity.smallIcon) {
                    params.width = AndroidUtilities.dp(30);
                    params.height = AndroidUtilities.dp(30);
                } else {
                    params.width = AndroidUtilities.dp(52);
                    params.height = AndroidUtilities.dp(52);
                }
                if (entity.id == 1) {
                    imageView.setBackgroundResource(entity.iconRes);
                } else {
                    Glide.with(context).load(entity.iconRes).into(imageView);
                }
                break;
        }
    }
}
