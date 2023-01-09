package teleblock.ui.adapter;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpSelectorGassfeeBinding;

import java.math.BigDecimal;

import teleblock.model.ui.SelectorGasFeeData;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/19
 * Author:Perry
 * Description：选择gass费用适配器
 */
public class SelectorGassFeeAdapter extends BaseQuickAdapter<SelectorGasFeeData, BaseViewHolder> {

    private BigDecimal dollerBigDecimal;
    private String gasLimit;
    private String symbol;
    private BigDecimal coinPrice;
    private BigDecimal mainCoinPrice;

    public SelectorGassFeeAdapter() {
        super(R.layout.adp_selector_gassfee);
    }

    public void setGasData(BigDecimal dollerBigDecimal, String gasLimit, String symbol, BigDecimal coinPrice, BigDecimal mainCoinPrice) {
        this.dollerBigDecimal = dollerBigDecimal;
        this.gasLimit = gasLimit;
        this.symbol = symbol;
        this.coinPrice = coinPrice;
        this.mainCoinPrice = mainCoinPrice;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, SelectorGasFeeData data) {
        if (dollerBigDecimal == null) {
            return;
        }

        AdpSelectorGassfeeBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpSelectorGassfeeBinding::bind);

        //标题
        binding.tvTitle.setText(data.getTitle());
        //gas费用价格
        binding.tvPrice.setText(WalletUtil.gasFree(data.getPrice(), gasLimit, mainCoinPrice, coinPrice) + " " + symbol);
        //gas费用美元
        binding.tvDollerPrice.setText(
                WalletUtil.toCoinPriceUSD(
                        WalletUtil.gasFree(data.getPrice(), gasLimit, mainCoinPrice, coinPrice)
                        , dollerBigDecimal
                        , 6
                )
        );

        if (data.isSelector()) {
            binding.rcl.getHelper().setBackgroundColorNormal(ContextCompat.getColor(getContext(), R.color.theme_color));
            binding.tvTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            binding.tvPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            binding.tvDollerPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            binding.rcl.getHelper().setBackgroundColorNormal(Color.parseColor("#F7F8F9"));
            binding.tvTitle.setTextColor(Color.parseColor("#1A1A1D"));
            binding.tvPrice.setTextColor(Color.parseColor("#1A1A1D"));
            binding.tvDollerPrice.setTextColor(Color.parseColor("#1A1A1D"));;
        }
    }

    /**
     * 点击改变ui状态
     * @param position
     */
    public void selectorUi(int position) {
        for (SelectorGasFeeData itemData : getData()) {
            itemData.setSelector(false);
        }

        getData().get(position).setSelector(true);
        notifyDataSetChanged();
    }
}
