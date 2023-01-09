package teleblock.ui.adapter;


import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewPrivateGroupItemBinding;
import org.telegram.messenger.databinding.ViewPrivateGroupTagItemBinding;

import java.util.List;

import teleblock.model.PrivateGroupEntity;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：社群推荐列表适配器
 */
public class PrivateGroupRvAdapter extends BaseQuickAdapter<PrivateGroupEntity, BaseViewHolder> implements LoadMoreModule {

    public PrivateGroupRvAdapter() {
        super(R.layout.view_private_group_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, PrivateGroupEntity data) {
        ViewPrivateGroupItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, ViewPrivateGroupItemBinding::bind);
        //获取链图标
        int chainImgResourse = getContext().getResources().getIdentifier("user_chain_logo_" + data.getChain_id(), "drawable", getContext().getPackageName());

        //默认ui状态
        binding.tvNftTitle.setVisibility(View.GONE);
        binding.tvPrice.getHelper().setIconNormalLeft(null);

        binding.tvJoin.setText(LocaleController.getString("vip_group_join", R.string.vip_group_join));
        //名称
        binding.tvName.setText(data.getTitle());
        //群头像
        GlideHelper.displayImage(getContext(), binding.flAvatar, data.getAvatar(), R.drawable.icon_group_recommend);
        //描述
        if (!TextUtils.isEmpty(data.getDescription())) {
            binding.tvDesc.setVisibility(View.VISIBLE);
            binding.tvDesc.setText(data.getDescription());
        } else {
            binding.tvDesc.setVisibility(View.GONE);
        }
        //加群人数
        binding.tvSubscriptionNum.setText(String.format(LocaleController.getString("vip_group_groupmember", R.string.vip_group_groupmember), data.getShip()));

        if (data.getJoin_type() == 2) { // 条件入群
            if (data.getToken_name().equals("ERC20")) {
                binding.tvPrice.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(chainImgResourse));
                binding.tvPrice.setText(String.format(LocaleController.getString("vip_group_numberofholdings", R.string.vip_group_numberofholdings), data.getAmount(), data.getCurrency_name()));
            } else {
                binding.tvNftTitle.setVisibility(View.VISIBLE);
                binding.tvPrice.setText(WalletUtil.formatAddress(data.getToken_address()) + " NFT");
            }
        } else if (data.getJoin_type() == 3) { // 付费入群
            binding.tvPrice.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(chainImgResourse));
            binding.tvPrice.setText(data.getAmount() + " " + data.getCurrency_name());
        } else { //无限制
            binding.tvPrice.setText(LocaleController.getString("create_group_tips_unlimit", R.string.create_group_tips_unlimit));
        }

//        List<String> tagList = data.getTags();
//        if (tagList != null) {
//            if (tagList.size() > 3) {
//                tagList = tagList.subList(0, 3);
//            }
//            binding.layoutTab.removeAllViews();
//            for (String tag : tagList) {
//                View view = LayoutInflater.from(getContext()).inflate(R.layout.view_private_group_tag_item, null);
//                ViewPrivateGroupTagItemBinding tagBind = ViewPrivateGroupTagItemBinding.bind(view);
//                tagBind.tvTag.setText(tag);
//                binding.layoutTab.addView(tagBind.getRoot());
//            }
//        }
    }
}