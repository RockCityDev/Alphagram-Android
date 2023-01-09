package teleblock.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdapterTranferSelectorChaintypeBinding;

import java.util.List;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：转账逻辑选择链类型的适配器
 */
public class TranferSelectorChainTypeAdapter
        extends BaseQuickAdapter<WalletNetworkConfigEntity.WalletNetworkConfigChainType, BaseViewHolder> {

    private WalletNetworkConfigEntity.WalletNetworkConfigChainType currentChainType;

    public TranferSelectorChainTypeAdapter() {
        super(R.layout.adapter_tranfer_selector_chaintype);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, WalletNetworkConfigEntity.WalletNetworkConfigChainType data) {
        AdapterTranferSelectorChaintypeBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterTranferSelectorChaintypeBinding::bind);

        //名称
        binding.tvName.setText(data.getName());
        //图标
        GlideHelper.displayImage(getContext(), binding.ivIcon, data.getIcon());

        if (currentChainType != null && data.getId() == currentChainType.getId()) { //选中状态
            binding.ivSlectorState.setImageResource(R.drawable.radio_bg_selector);
        } else {
            binding.ivSlectorState.setImageResource(R.drawable.radio_bg_nomber);
        }
    }

    public void setCurrentChainTypeData(WalletNetworkConfigEntity.WalletNetworkConfigChainType mWalletNetworkConfigChainType) {
        this.currentChainType = mWalletNetworkConfigChainType;
        notifyDataSetChanged();
    }
}
