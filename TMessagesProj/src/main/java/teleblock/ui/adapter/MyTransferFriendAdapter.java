package teleblock.ui.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.AdpMytransferFriendBinding;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import teleblock.model.wallet.WalletInfo;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：我的转账好友适配器
 */
public class MyTransferFriendAdapter extends BaseQuickAdapter<WalletInfo, BaseViewHolder> {
    public MyTransferFriendAdapter() {
        super(R.layout.adp_mytransfer_friend, new ArrayList<>());
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, WalletInfo walletInfo) {
        AdpMytransferFriendBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpMytransferFriendBinding::bind);
        TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(walletInfo.getTg_user_id());
        if (user != null) {
            binding.flAvatar.setUserInfo(user).loadView();
            String name = "";
            if (!TextUtils.isEmpty(user.first_name)) {
                name += user.first_name;
            }
            if (!TextUtils.isEmpty(user.last_name)) {
                name += user.last_name;
            }
            binding.tvName.setText(name);
            if (walletInfo != null && walletInfo.getWallet_info().size() > 0) {
                binding.tvAccount.setText(walletInfo.getWallet_info().get(0).getWallet_address());
            }
        }
    }
}
