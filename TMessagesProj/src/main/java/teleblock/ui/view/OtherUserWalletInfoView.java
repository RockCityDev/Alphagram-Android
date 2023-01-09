package teleblock.ui.view;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.OtherUserWalletInfoViewBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;

import teleblock.model.wallet.WalletInfo;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.dialog.TransferDialog;
import teleblock.util.WalletUtil;

/**
 * Time:2022/11/11
 * Author:Perry
 * Description：其他用户钱包信息
 */
public class OtherUserWalletInfoView extends ConstraintLayout {
    private BaseFragment baseFragment;
    private OtherUserWalletInfoViewBinding binding;

    //对方user信息
    private TLRPC.User otherUserInfo;
    //钱包信息数据
    private WalletInfo walletInfo;
    //钱包地址
    private String walletAddress;

    public OtherUserWalletInfoView(@NonNull BaseFragment baseFragment, WalletInfo walletInfo, TLRPC.User otherUserInfo) {
        super(baseFragment.getParentActivity());
        this.baseFragment = baseFragment;
        this.walletInfo = walletInfo;
        this.otherUserInfo = otherUserInfo;

        binding = OtherUserWalletInfoViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setThemeColor();
        initView();
    }

    private void setThemeColor() {
        binding.tvCopyWalletAddress.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvChainName.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvWalletAddressTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText2));
        binding.tvNetworkTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText2));
        binding.tvWalletDetails.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        binding.tvTransferTo.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));

        Drawable copyAddressIcon = ResourceUtils.getDrawable(R.drawable.icon_copy_address_wallet_white);
        copyAddressIcon.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
        binding.tvCopyWalletAddress.getHelper().setIconNormalRight(copyAddressIcon);

        Drawable arrowIcon = ResourceUtils.getDrawable(R.drawable.arrow_newchat);
        arrowIcon.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        binding.tvWalletDetails.getHelper().setIconNormalRight(arrowIcon);
        binding.tvTransferTo.getHelper().setIconNormalRight(arrowIcon);

        Drawable transferIcon = ResourceUtils.getDrawable(R.drawable.tab_transfer_icon);
        transferIcon.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        binding.tvTransferTo.getHelper().setIconNormalLeft(transferIcon);
    }

    private void initView() {
        binding.tvWalletAddressTitle.setText(LocaleController.getString("other_user_wallet_info_wallet_address", R.string.other_user_wallet_info_wallet_address));
        binding.tvNetworkTitle.setText(LocaleController.getString("other_user_wallet_info_network", R.string.other_user_wallet_info_network));
        binding.tvWalletDetails.setText(LocaleController.getString("other_user_wallet_info_details", R.string.other_user_wallet_info_details));
        binding.tvTransferTo.setText(LocaleController.getString("dialog_user_personal_transfer_to_he", R.string.dialog_user_personal_transfer_to_he));

        //钱包地址
        if (!CollectionUtils.isEmpty(walletInfo.getWallet_info())) {
            walletAddress = walletInfo.getWallet_info().get(0).getWallet_address();
        }

        if (!walletAddress.isEmpty()) {
            //设置钱包地址
            binding.tvCopyWalletAddress.setText(WalletUtil.formatAddress(walletAddress));
        }

        //链名称
        binding.tvChainName.setText(walletInfo.chain_name);

        //点击复制钱包地址
        binding.tvCopyWalletAddress.setOnClickListener(v -> {
            ClipboardUtils.copyText(walletAddress);
            BulletinFactory.of(baseFragment).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), baseFragment.getResourceProvider()).show();
        });

        //跳转到钱包主页
        binding.tvWalletDetails.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("address", walletAddress);
            args.putBoolean("userSelf", false);
            args.putLong("otherUserId", otherUserInfo.id);
            baseFragment.presentFragment(new WalletHomeAct(args));
        });

        //拉起转账
        binding.tvTransferTo.setOnClickListener(v -> {
            new TransferDialog(
                    getContext(),
                    baseFragment.getUserConfig().getCurrentUser(),
                    otherUserInfo,
                    walletAddress,
                    walletInfo.chain_id,
                    false,
                    parseStr -> {}
            ).show();
        });
    }
}
