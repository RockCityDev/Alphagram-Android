package teleblock.ui.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpTranferSelectorCointypeBinding;

import teleblock.model.ui.MyCoinListData;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/9/30
 * Author:Perry
 * Description：选择币类型适配器
 */
public class TranferSelectorCoinTypeAdapter extends BaseQuickAdapter<MyCoinListData, BaseViewHolder> {

    public TranferSelectorCoinTypeAdapter() {
        super(R.layout.adp_tranfer_selector_cointype);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, MyCoinListData myCoinListData) {
        AdpTranferSelectorCointypeBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpTranferSelectorCointypeBinding::bind);

        //币种图标
        if (!TextUtils.isEmpty(myCoinListData.getIcon())) {
            GlideHelper.displayImage(getContext(), binding.ivIcon, myCoinListData.getIcon());
        } else {
            GlideHelper.displayImage(getContext(), binding.ivIcon, myCoinListData.getIconRes());
        }
        //名称
        binding.tvName.setText(myCoinListData.getSymbol());
        //账户余额
        binding.tvBalance.setText(WalletUtil.bigDecimalScale(myCoinListData.getBalance(), 10).toPlainString());
        //单价
        binding.tvPrice.setText("$" + WalletUtil.bigDecimalScale(myCoinListData.getBalance(), 6));
        //账户余额 美元
        binding.tvBalanceUsd.setText(WalletUtil.toCoinPriceUSD(myCoinListData.getBalance(), myCoinListData.getPrice(), 6));
    }
}
