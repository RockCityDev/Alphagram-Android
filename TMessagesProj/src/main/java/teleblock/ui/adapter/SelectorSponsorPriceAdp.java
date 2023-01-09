package teleblock.ui.adapter;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpSelectorSponsorPriceBinding;

import java.math.BigDecimal;

import teleblock.model.ui.SelectorSponsorPriceData;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：选择的捐献金额适配器
 */
public class SelectorSponsorPriceAdp extends BaseQuickAdapter<SelectorSponsorPriceData, BaseViewHolder> {
    //单价
    private BigDecimal price;

    private int oldPosition = -1;

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public SelectorSponsorPriceAdp() {
        super(R.layout.adp_selector_sponsor_price);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, SelectorSponsorPriceData selectorSponsorPriceData) {
        AdpSelectorSponsorPriceBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpSelectorSponsorPriceBinding::bind);

        binding.tvIcon.setText(selectorSponsorPriceData.getIcon());
        binding.tvPrice.setText(selectorSponsorPriceData.getPrice());
        if (selectorSponsorPriceData.isSelector()) {
            binding.ll.setBackgroundColor(Color.parseColor("#03BDFF"));
            binding.tvPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            binding.tvDoller.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            binding.ll.setBackgroundColor(Color.parseColor("#F7F8F9"));
            binding.tvPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            binding.tvDoller.setTextColor(Color.parseColor("#828283"));
        }

        if (price != null) {
            //换算成美元
            binding.tvDoller.setText(WalletUtil.toCoinPriceUSD(new BigDecimal(selectorSponsorPriceData.getPrice()), price, 6));
        }

    }

    /**
     * 选择操作
     * @param clickPosition
     */
    public void selectorOpera(int clickPosition) {
        if (oldPosition == clickPosition) {
            return;
        }

        if (oldPosition != -1) {
            getData().get(oldPosition).setSelector(false);
            notifyItemChanged(oldPosition);
        }

        getData().get(clickPosition).setSelector(true);
        notifyItemChanged(clickPosition);

        oldPosition = clickPosition;
    }
}