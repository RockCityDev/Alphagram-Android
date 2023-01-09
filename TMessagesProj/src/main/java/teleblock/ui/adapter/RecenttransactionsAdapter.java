package teleblock.ui.adapter;

import static teleblock.widget.TelegramUserAvatar.ADDRESS_TRANSFER;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.RImageView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.AdpRecenttransactionsBinding;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import teleblock.model.TransferHistoryEntity;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：最近交易适配器列表
 */
public class RecenttransactionsAdapter extends BaseQuickAdapter<TransferHistoryEntity, BaseViewHolder> {
    public RecenttransactionsAdapter() {
        super(R.layout.adp_recenttransactions, new ArrayList<>());
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, TransferHistoryEntity entity) {
        AdpRecenttransactionsBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpRecenttransactionsBinding::bind);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tvAccount.getLayoutParams();
        params.topMargin = ConvertUtils.dp2px(0);
        binding.tvAccount.setLayoutParams(params);

        String showTgUserId;
        String showAccount;
        String unit;
        long myId = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getClientUserId();
        if (String.valueOf(myId).equals(entity.receipt_tg_user_id)) {//转入
            showTgUserId = entity.payment_tg_user_id;
            showAccount = entity.payment_account;
            unit = "+";
        } else {//转出
            showTgUserId = entity.receipt_tg_user_id;
            showAccount = entity.receipt_account;
            unit = "-";
        }
        binding.tvAccount.setText(showAccount);
        binding.tvOrder.setText(unit + entity.amount + entity.currency_name);

        if (!TextUtils.isEmpty(showTgUserId) && !showTgUserId.equals("0")) {
            TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(Long.parseLong(showTgUserId));
            if (user != null) {
                binding.flAvatar.setUserInfo(user).loadView();
                binding.tvName.setVisibility(View.VISIBLE);
                String name = "";
                if (!TextUtils.isEmpty(user.first_name)) {
                    name += user.first_name;
                }
                if (!TextUtils.isEmpty(user.last_name)) {
                    name += user.last_name;
                }
                binding.tvName.setText(name);
                return;
            }
        }
        //这里是钱包地址转账
        binding.tvName.setText(WalletUtil.formatAddress(showAccount));
        binding.flAvatar.setModel(ADDRESS_TRANSFER).loadView();
    }
}
