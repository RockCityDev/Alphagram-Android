package teleblock.ui.adapter;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.helper.RTextViewHelper;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpHottopicItemBinding;

import teleblock.model.HottopicEntity;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：热门话题适配器
 */
public class HotTopicAdp extends BaseQuickAdapter<HottopicEntity, BaseViewHolder> {

    public int clickPosition = 0;

    public HotTopicAdp() {
        super(R.layout.adp_hottopic_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, HottopicEntity data) {
        AdpHottopicItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpHottopicItemBinding::bind);
        //获取名称
        binding.tvName.setText(data.getName());

        //动态修改间距
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) binding.tvName.getLayoutParams();
        layoutParams.rightMargin = SizeUtils.dp2px(6f);
        if (viewHolder.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == 1) {
            layoutParams.leftMargin = SizeUtils.dp2px(16f);
        } else {
            layoutParams.leftMargin = SizeUtils.dp2px(0f);
        }

        binding.tvName.setLayoutParams(layoutParams);

        RTextViewHelper helper = binding.tvName.getHelper();
        if (data.getSelectorStatus()) { //选中状态
            helper.setBackgroundColorNormal(ContextCompat.getColor(getContext(), R.color.theme_color));
            helper.setBorderColorNormal(ContextCompat.getColor(getContext(), R.color.theme_color));
            helper.setTextColorNormal(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            helper.setBackgroundColorNormal(ContextCompat.getColor(getContext(), R.color.white));
            helper.setBorderColorNormal(Color.parseColor("#E6E6E6"));
            helper.setTextColorNormal(Color.parseColor("#56565C"));
        }
    }

    /**
     * tab选中状态的逻辑处理
     * @param position
     */
    public void tabSelectorLogicalProcessing(int position, Runnable runnable) {
        if (clickPosition != position) {
            getData().get(clickPosition).setSelectorStatus(false);
            notifyItemChanged(clickPosition);

            getData().get(position).setSelectorStatus(true);
            notifyItemChanged(position);
            clickPosition = position;

            runnable.run();
        }
    }
}
