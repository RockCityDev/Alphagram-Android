package teleblock.ui.adapter;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.helper.RTextViewHelper;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.AdapterHorizontalPopularHotchannelItemBinding;
import org.telegram.messenger.databinding.AdapterPopularHotchannelMoreItemBinding;
import org.telegram.tgnet.TLRPC;

import java.util.List;

import teleblock.manager.ChatManager;
import teleblock.model.HotRecommendData;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/7/14
 * Author:Perry
 * Description：钱包页热门推荐
 */
public class WalletHotRecommendAdapter extends BaseDelegateMultiAdapter<HotRecommendData, BaseViewHolder> {

    //普通推荐channel布局
    private static final int CHANNEL_TYPE = 1;
    //更多频道类型
    private static final int MORE_CHANNEL_TYPE = 2;

    public WalletHotRecommendAdapter() {
        //设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<HotRecommendData>() {
            @Override
            public int getItemType(@NonNull List<? extends HotRecommendData> list, int i) {
                if (list.get(i).getAvatarList().size() == 0) {
                    return CHANNEL_TYPE;
                } else {
                    return MORE_CHANNEL_TYPE;
                }
            }
        });

        getMultiTypeDelegate()
                .addItemType(CHANNEL_TYPE, R.layout.adapter_horizontal_popular_hotchannel_item)
                .addItemType(MORE_CHANNEL_TYPE, R.layout.adapter_popular_hotchannel_more_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, HotRecommendData hotRecommendData) {
        switch (viewHolder.getItemViewType()) {
            case CHANNEL_TYPE:
                showChannelItem(viewHolder, hotRecommendData);
                break;
            default:
                showMoreChannelItem(viewHolder, hotRecommendData);
                break;
        }
    }

    /**
     * 显示更多频道布局
     * @param viewHolder
     * @param hotRecommendData
     */
    private void showMoreChannelItem(BaseViewHolder viewHolder, HotRecommendData hotRecommendData) {
        AdapterPopularHotchannelMoreItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterPopularHotchannelMoreItemBinding::bind);
        binding.tvExploreMore.setText(LocaleController.getString("view_community_recommendtab_findandjoin", R.string.view_community_recommendtab_findandjoin));
        binding.tvAddTips.setText(LocaleController.getString("view_community_recommendtab_findandjoin", R.string.view_community_recommendtab_findandjoin));
        binding.tvShowallChannel.setText(LocaleController.getString("view_community_recommendtab_showall_recommend", R.string.view_community_recommendtab_showall_recommend));

        if (hotRecommendData.getAvatarList().size() == 2) {
            GlideHelper.displayRoundImage(getContext(), binding.ivMoreChannelAvatar1, hotRecommendData.getAvatarList().get(0), 10);
            GlideHelper.displayRoundImage(getContext(), binding.ivMoreChannelAvatar2, hotRecommendData.getAvatarList().get(1), 10);
        }
    }

    /**
     * 显示channel基本信息
     * @param viewHolder
     * @param data
     */
    private void showChannelItem(BaseViewHolder viewHolder, HotRecommendData data) {
        AdapterHorizontalPopularHotchannelItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterHorizontalPopularHotchannelItemBinding::bind);
        //动态修改间距
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) binding.rfl.getLayoutParams();
        layoutParams.rightMargin = SizeUtils.dp2px(5f);
        if (viewHolder.getAdapterPosition() == 0) {
            layoutParams.leftMargin = SizeUtils.dp2px(16f);
        } else {
            layoutParams.leftMargin = SizeUtils.dp2px(0f);
        }
        binding.rfl.setLayoutParams(layoutParams);

        //名称
        binding.tvChannelName.setText(data.getChat_title());
        //按钮文字
        binding.tvAddChannel.setText(data.getChat_type() == 1 ?
                LocaleController.getString("view_community_recommendtab_addchannel", R.string.view_community_recommendtab_addchannel):
                LocaleController.getString("view_community_recommendtab_addgroup", R.string.view_community_recommendtab_addgroup));
        //加载头像
        GlideHelper.displayImage(getContext(), binding.ivChannelAvatar, data.getAvatar());

        String stringFormat;
        if (data.getChat_type() == 1) { //频道
            stringFormat = LocaleController.getString("channel_subscription_num", R.string.channel_subscription_num);
        } else {
            stringFormat = LocaleController.getString("group_subscription_num", R.string.group_subscription_num);
        }
        binding.tvSubscriptionNum.setText(String.format(stringFormat, data.getFollows()));

        ChatManager.getInstance(UserConfig.selectedAccount).getChatMessage(
                data.getChat_id(), data.getChat_link(),
                new ChatManager.ChatManagerRunable() {
                    @Override
                    public void chat(TLRPC.Chat chat) {
                        //是否加入了
                        binding.tvAddChannel.setVisibility(View.VISIBLE);
                        data.setIfInChat(ChatObject.isNotInChat(chat));
                        RTextViewHelper helper = binding.tvAddChannel.getHelper();
                        helper.setBackgroundColorNormal(data.isIfInChat() ? Color.parseColor("#03BDFF") : Color.parseColor("#B7B7B7"));
                    }

                    @Override
                    public void chatFull(TLRPC.ChatFull chatFull) {
                    }

                    @Override
                    public void chatInvalid() {
                    }
                }
        );
    }
}