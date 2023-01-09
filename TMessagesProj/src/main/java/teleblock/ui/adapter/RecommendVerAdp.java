package teleblock.ui.adapter;


import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdapterVerticalRecommendItemBinding;

import teleblock.model.HotRecommendData;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：社群推荐列表适配器
 */
public class RecommendVerAdp extends BaseQuickAdapter<HotRecommendData, BaseViewHolder> implements LoadMoreModule {

    public RecommendVerAdp() {
        super(R.layout.adapter_vertical_recommend_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, HotRecommendData data) {
        AdapterVerticalRecommendItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterVerticalRecommendItemBinding::bind);
        //名称
        binding.tvName.setText(data.getChat_title());
        //加载头像
        GlideHelper.displayImage(getContext(), binding.flAvatar, data.getAvatar(), R.drawable.icon_group_recommend);

        String stringFormat;
        if (data.getChat_type() == 1) { //频道
            stringFormat = LocaleController.getString("channel_subscription_num", R.string.channel_subscription_num);
            binding.tvSubscriptionNum.setText(String.format(stringFormat, data.getFollows()));
        } else {
            stringFormat = LocaleController.getString("group_subscription_num", R.string.group_subscription_num);
            binding.tvSubscriptionNum.setText(String.format(stringFormat, data.getFollows())
                    + " " + String.format(LocaleController.getString("online_num", R.string.online_num), data.getOnline()));
        }

        //最后一次发言时间
//        binding.tvTime.setText(TimeUtil.gethhDate(data.getTimestamp()));
    }
}