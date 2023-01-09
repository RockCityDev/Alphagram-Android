package teleblock.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.List;

import teleblock.model.ChannelWithTagEntity;


public class ChannelMoreTagRvAdapter extends BaseQuickAdapter<ChannelWithTagEntity, BaseViewHolder> {
    Context context;
    public boolean secretModel;

    public ChannelMoreTagRvAdapter(Context context) {
        super(R.layout.view_channel_more_dialog_tag_item);
        this.context = context;
    }

    public void setDataList(List<ChannelWithTagEntity> list, boolean secretModel) {
        this.secretModel = secretModel;
        setList(list);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ChannelWithTagEntity entity) {
        LinearLayout item_view = baseViewHolder.findView(R.id.item_view);
        TextView tv_item_text = baseViewHolder.findView(R.id.tv_item_text);
        ImageView iv_item_img = baseViewHolder.findView(R.id.iv_item_img);
        tv_item_text.setText(entity.tagName);

        if (secretModel) {
            String secretStr = LocaleController.getString("channel_recommend_tag_secret", R.string.channel_recommend_tag_secret);
            if (secretStr.equals(entity.tagName)) {
                item_view.setBackgroundResource(R.drawable.ic_channel_tag_item_select_bg);
                iv_item_img.setImageResource(R.drawable.ic_add_tag_xx_wirte);
                tv_item_text.setTextColor(Color.parseColor("#ffffff"));
            } else {
                item_view.setBackgroundResource(R.drawable.ic_channel_tag_item_bg);
                iv_item_img.setImageResource(R.drawable.ic_add_tag_jj_gray);
                tv_item_text.setTextColor(Color.parseColor("#60000000"));
            }
        } else {
            if (entity.tagId != 0) {
                item_view.setBackgroundResource(R.drawable.ic_channel_tag_item_select_bg);
                iv_item_img.setImageResource(R.drawable.ic_add_tag_xx_wirte);
                tv_item_text.setTextColor(Color.parseColor("#ffffff"));
            } else {
                item_view.setBackgroundResource(R.drawable.ic_channel_tag_item_bg);
                iv_item_img.setImageResource(R.drawable.ic_add_tag_jj);
                tv_item_text.setTextColor(Color.parseColor("#000000"));
            }
        }
    }
}
