package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpHorizontalMorerecommendItemBinding;
import org.telegram.messenger.databinding.AdpHorizontalRecommendItemBinding;

import java.util.List;
import teleblock.model.HotRecommendData;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：热门推荐adp
 */
public class HotRecommendAdp extends BaseDelegateMultiAdapter<HotRecommendData, BaseViewHolder> {
    //普通推荐l布局
    public static final int RECOMMEND_TYPE = 1;
    //更多推荐类型
    public static final int MORE_RECOMMENDL_TYPE = 2;

    public HotRecommendAdp() {
        setMultiTypeDelegate(new BaseMultiTypeDelegate<HotRecommendData>() {
            @Override
            public int getItemType(@NonNull List<? extends HotRecommendData> list, int i) {
                if (list.get(i).getAvatarList().size() == 0) {
                    return RECOMMEND_TYPE;
                } else {
                    return MORE_RECOMMENDL_TYPE;
                }
            }
        });

        getMultiTypeDelegate()
                .addItemType(RECOMMEND_TYPE, R.layout.adp_horizontal_recommend_item)
                .addItemType(MORE_RECOMMENDL_TYPE, R.layout.adp_horizontal_morerecommend_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, HotRecommendData hotRecommendData) {
        switch (viewHolder.getItemViewType()) {
            case MORE_RECOMMENDL_TYPE:
                showAllRecomedItems(viewHolder, hotRecommendData);
                break;
            default:
                showRecomedItems(viewHolder, hotRecommendData);
                break;
        }
    }

    /**
     * 显示单个item
     * @param viewHolder
     * @param data
     */
    private void showRecomedItems(BaseViewHolder viewHolder, HotRecommendData data) {
        AdpHorizontalRecommendItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpHorizontalRecommendItemBinding::bind);

        //动态修改间距
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) binding.cl.getLayoutParams();
        layoutParams.rightMargin = SizeUtils.dp2px(8f);
        if (viewHolder.getAdapterPosition() == 0) {
            layoutParams.leftMargin = SizeUtils.dp2px(16f);
        } else {
            layoutParams.leftMargin = SizeUtils.dp2px(0f);
        }
        binding.cl.setLayoutParams(layoutParams);
        //名称
        binding.tvName.setText(data.getChat_title());
        // 头像
        GlideHelper.displayImage(getContext(), binding.ivAvatar, data.getAvatar(), R.drawable.icon_group_recommend);
        String stringFormat;
        if (data.getChat_type() == 1) { //频道
            stringFormat = LocaleController.getString("channel_subscription_num", R.string.channel_subscription_num);
        } else {
            stringFormat = LocaleController.getString("group_subscription_num", R.string.group_subscription_num);
        }
        String formatStr = String.format(stringFormat, data.getFollows());
        binding.tvNum.setText(formatStr);
    }

    /**
     * 显示所有布局
     * @param viewHolder
     * @param data
     */
    private void showAllRecomedItems(BaseViewHolder viewHolder, HotRecommendData data) {
        AdpHorizontalMorerecommendItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpHorizontalMorerecommendItemBinding::bind);
        binding.tvName.setText(LocaleController.getString("view_community_recommendtab_morerecommend", R.string.view_community_recommendtab_morerecommend));
        binding.tvFindorjoin.setText(LocaleController.getString("view_community_recommendtab_findandjoin", R.string.view_community_recommendtab_findandjoin));

        if (data.getAvatarList().size() == 2) {
            GlideHelper.displayImage(getContext(), binding.iv1, data.getAvatarList().get(0), R.drawable.icon_group_recommend);
            GlideHelper.displayImage(getContext(), binding.iv2, data.getAvatarList().get(1), R.drawable.icon_group_recommend);
        }
    }
}
