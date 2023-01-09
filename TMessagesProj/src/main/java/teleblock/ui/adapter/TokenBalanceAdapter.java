package teleblock.ui.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ItemTokenBalanceBinding;

import java.math.BigDecimal;

import teleblock.model.wallet.TokenBalance;
import teleblock.util.StringUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

public class TokenBalanceAdapter extends BaseQuickAdapter<TokenBalance, BaseViewHolder> implements LoadMoreModule {

    public TokenBalanceAdapter() {
        super(R.layout.item_token_balance);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TokenBalance item) {
        ItemTokenBalanceBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ItemTokenBalanceBinding::bind);
        if (TextUtils.isEmpty(item.image)) {
            GlideHelper.displayRoundImage(getContext(), binding.ivTokenAvatar, item.imageRes, 18);
        } else {
            GlideHelper.displayRoundImage(getContext(), binding.ivTokenAvatar, item.image, 18);
        }
        binding.tvTokenName.setText(item.symbol);
        binding.tvTokenPrice.setText("$" + StringUtil.formatNumber(item.price, 4));
        binding.tvTokenBalance.setText(item.balance + "");
        binding.tvBalanceUsd.setText("$" + StringUtil.formatPrice(item.balanceUSD, true));
    }
}
