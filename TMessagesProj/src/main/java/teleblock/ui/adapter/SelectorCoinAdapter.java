package teleblock.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdapterTranferSelectorChaintypeBinding;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/10/26
 * Author:Perry
 * Description：选择币种列表适配器
 */
public class SelectorCoinAdapter extends BaseQuickAdapter<WalletNetworkConfigEntity.WalletNetworkConfigEntityItem, BaseViewHolder> {

    private WalletNetworkConfigEntity.WalletNetworkConfigEntityItem currentCoinData;

    public SelectorCoinAdapter() {
        super(R.layout.adapter_tranfer_selector_chaintype);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, WalletNetworkConfigEntity.WalletNetworkConfigEntityItem itemData) {
        AdapterTranferSelectorChaintypeBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterTranferSelectorChaintypeBinding::bind);

        //名称
        binding.tvName.setText(itemData.getName());
        //图标
        if (StringUtils.isEmpty(itemData.getIcon())) {
            binding.ivIcon.setVisibility(View.INVISIBLE);
        } else {
            binding.ivIcon.setVisibility(View.VISIBLE);
            GlideHelper.displayImage(getContext(), binding.ivIcon, itemData.getIcon());
        }

        if (currentCoinData != null && itemData.getId() == currentCoinData.getId()) { //选中状态
            binding.ivSlectorState.setImageResource(R.drawable.radio_bg_selector);
        } else {
            binding.ivSlectorState.setImageResource(R.drawable.radio_bg_nomber);
        }
    }

    public void setCurrentCoinData(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem currentCoinData) {
        this.currentCoinData = currentCoinData;
        notifyDataSetChanged();
    }
}
