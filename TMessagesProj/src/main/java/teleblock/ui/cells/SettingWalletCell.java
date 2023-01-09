package teleblock.ui.cells;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewSettingWalletBinding;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivity;

import java.util.HashMap;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.ui.activity.WalletBindAct;
import teleblock.ui.dialog.WalletOperaDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;

/**
 * Time:2022/8/3
 * Author:Perry
 * Description：设置里面的钱包布局
 */
public class SettingWalletCell extends ConstraintLayout {

    private ViewSettingWalletBinding binding;
    private ProfileActivity fragment;
    private HeaderCell headerCell;
    private TextCheckCell bindSwitch;

    public SettingWalletCell(@NonNull Context context, ProfileActivity fragment) {
        super(context);
        EventBus.getDefault().register(this);
        this.fragment = fragment;
    }

    {
        binding = ViewSettingWalletBinding.inflate(LayoutInflater.from(getContext()), this, true);
        initView();
        initData();
    }

    private void initView() {
        //未绑定
        binding.tvWalletUnbindTitle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        binding.tvWalletUnbindTitle.setText(LocaleController.getString("setting_wallet_unbind_title", R.string.setting_wallet_unbind_title));
        binding.tvWalletUnbindTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvLinkWallet.setText(LocaleController.getString("setting_linkwallet_title", R.string.setting_linkwallet_title));
        binding.viewUnbindDir.setBackgroundColor(Theme.getColor(Theme.key_divider));
        TextCheckCell unBindSwitch = new TextCheckCell(getContext());
        unBindSwitch.setAlpha(0.5f);
        unBindSwitch.setTextAndCheck(LocaleController.getString("setting_wallet_open_nft", R.string.setting_wallet_open_nft), false, false);
        binding.unbindSwitchFrame.addView(unBindSwitch, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        binding.llLinkWallet.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.设置页_链接钱包, new HashMap<>());
            fragment.presentFragment(new WalletBindAct());
        });
        binding.unbindSwitchFrame.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.设置页_链接钱包, new HashMap<>());
            fragment.presentFragment(new WalletBindAct());
        });


        //已绑定
        headerCell = new HeaderCell(getContext(), 23, null);
        headerCell.setText(LocaleController.getString("fragment_setting_wallet_unbind_title", R.string.setting_wallet_unbind_title));
        binding.llBindWallet.addView(headerCell, 0);
        binding.tvWalletAddress.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvChangeWallet.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        binding.ivChangeWallet.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        binding.ivArrowRight.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        binding.tvChangeWallet.setText(LocaleController.getString("setting_add_wallet", R.string.setting_add_wallet));
        binding.viewDir.setBackgroundColor(Theme.getColor(Theme.key_divider));
        binding.bindViewDir2.setBackgroundColor(Theme.getColor(Theme.key_divider));
        bindSwitch = new TextCheckCell(getContext());
        bindSwitch.setTextAndCheck(LocaleController.getString("setting_wallet_open_nft", R.string.setting_wallet_open_nft), MMKVUtil.getNftphotoIfShow(), false);
        binding.bindSwitchFrame.addView(bindSwitch, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        binding.bindSwitchFrame.setOnClickListener(view -> {
            boolean ifShow = MMKVUtil.getNftphotoIfShow();
            MMKVUtil.setNftphotoIfShow(ifShow ? 0 : 1);
            bindSwitch.setChecked(!ifShow);

            //更改nft状态
            TelegramUtil.changeUserNftStatus(ifShow ? 0 : 1, () -> {});
        });
        //已连接钱包
        binding.rvBindingWallet.setOnClickListener(view -> {
            new WalletOperaDialog(getContext(), MMKVUtil.connectedWalletAddress(), new WalletOperaDialog.WalletOperaDialogListener() {
                @Override
                public void disconnect() {
                    //清空存储的钱包地址
                    MMKVUtil.connectedWalletAddress("");
                    MMKVUtil.connectedWalletPkg("");
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_CLOSED));
                }

                @Override
                public void changeAddress() {
                    fragment.presentFragment(new WalletBindAct());
                }

                @Override
                public void copyAddress() {
                    ClipboardUtils.copyText(MMKVUtil.connectedWalletAddress());
                    BulletinFactory.of(fragment).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), fragment.getResourceProvider()).show();
                }
            }).show();
        });
        //切换
        binding.llChangeWallet.setOnClickListener(view -> {
            fragment.presentFragment(new WalletBindAct());
        });
    }

    private void initData() {
        String pkg = MMKVUtil.connectedWalletPkg();
        if (TextUtils.isEmpty(pkg)) {
            binding.rlUnbindWallet.setVisibility(VISIBLE);
            binding.llBindWallet.setVisibility(GONE);
            headerCell.setVisibility(GONE);
        } else {
            binding.tvWalletAddress.setText(WalletUtil.formatAddress(MMKVUtil.connectedWalletAddress()));
            binding.rlUnbindWallet.setVisibility(GONE);
            binding.llBindWallet.setVisibility(VISIBLE);
            headerCell.setVisibility(VISIBLE);
            //钱包图标
            binding.ivBgIcon.setImageResource(BlockchainConfig.getWalletIconByPkg(pkg));
            bindSwitch.setChecked(MMKVUtil.getNftphotoIfShow());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.WALLET_CONNECT_APPROVED:
            case EventBusTags.WALLET_CONNECT_CLOSED:
                initData();
                break;
        }
    }
}