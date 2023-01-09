package teleblock.ui.adapter;

import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ItemTransactionInfoBinding;

import java.math.BigDecimal;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.TransactionInfo;
import teleblock.util.MMKVUtil;
import teleblock.util.TimeUtil;
import teleblock.util.WalletUtil;

public class TransactionInfoAdapter extends BaseQuickAdapter<TransactionInfo, BaseViewHolder> implements LoadMoreModule {
    private String address;
    private String currentPrice;

    public TransactionInfoAdapter(String address) {
        super(R.layout.item_transaction_info);
        this.address = address;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TransactionInfo item) {
        ItemTransactionInfoBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ItemTransactionInfoBinding::bind);
        StringBuilder value = new StringBuilder();
        if (TextUtils.isEmpty(item.functionName)) {
            if (item.from.equalsIgnoreCase(address)) {
                value.append("-");
                binding.tvTitle.setText("Sent");
                binding.ivIcon.setImageResource(R.drawable.transfer_type_sent_ic);
            } else {
                value.append("+");
                binding.tvTitle.setText("Received");
                binding.ivIcon.setImageResource(R.drawable.transfer_type_received_ic);
            }
        } else {
            if (item.functionName.startsWith("transfer")) {
                binding.tvTitle.setText("Smart contract interactive");
                binding.ivIcon.setImageResource(R.drawable.transfer_type_contract_ic);
            }
        }
        String balance;
        if (!TextUtils.isEmpty(item.contractAddress)) { // 代币
            if (TextUtils.isEmpty(item.value)) item.value = "1";
            value.append(balance = WalletUtil.parseToken(item.value, item.tokenDecimal));
            value.append(" " + item.tokenSymbol);
        } else { // 主币
            WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
            if (chainType != null) {
                String symbol = chainType.getMain_currency_name();
                value.append(balance = WalletUtil.parseToken(item.value, 18));
                value.append(" ").append(symbol);
                if (!TextUtils.isEmpty(currentPrice)) {
                    binding.tvDollar.setText(WalletUtil.toCoinPriceUSD(new BigDecimal(balance), new BigDecimal(currentPrice), 6));
                }
            }
        }
        binding.tvValue.setText(value);
        long timestamp = Long.decode(item.timestamp);
        binding.tvTime.setText(TimeUtil.getDate2String(timestamp * 1000));
        if (item.isError) {
            binding.tvState.setTextColor(Color.RED);
            binding.tvState.setText("Fail");
        } else {
            binding.tvState.setTextColor(Color.parseColor("#44D320"));
            binding.tvState.setText("Confirmed");
        }
    }

    public void updateData(String currentPrice) {
        this.currentPrice = currentPrice;
        notifyDataSetChanged();
    }
}
