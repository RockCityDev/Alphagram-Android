package teleblock.ui.adapter;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpCurrencyOperaBtnBinding;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/10/11
 * Author:Perry
 * Description：币圈社群 功能区
 */
public class CurrencyOperaBtnAdp extends BaseQuickAdapter<WalletNetworkConfigEntity.WalletNetworkConfigChainTypeBtn, BaseViewHolder> {

    public CurrencyOperaBtnAdp() {
        super(R.layout.adp_currency_opera_btn);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, WalletNetworkConfigEntity.WalletNetworkConfigChainTypeBtn data) {
        AdpCurrencyOperaBtnBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpCurrencyOperaBtnBinding::bind);
        binding.tvName.setText(data.getName());
        if (!StringUtils.isEmpty(data.getIcon_link())) {
            GlideHelper.displayImage(getContext(), binding.ivIcon, data.getIcon_link());
        } else {
            binding.ivIcon.setImageResource(
                    getContext().getResources().getIdentifier("icon_official_" + data.getIcon(), "drawable", getContext().getPackageName())
            );
        }
    }
}
