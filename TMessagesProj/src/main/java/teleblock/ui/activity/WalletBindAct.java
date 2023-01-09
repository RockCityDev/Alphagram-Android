package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActBindWalletBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;

/**
 * Time:2022/8/4
 * Author:Perry
 * Description：绑定钱包页面
 */
public class WalletBindAct extends BaseFragment {

    private ActBindWalletBinding binding;

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        setNavigationBarColor(Color.WHITE, true);
        binding = ActBindWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("act_bindwallet_linkwallet", R.string.act_bindwallet_linkwallet));
        binding.tvTitleTips.setText(LocaleController.getString("act_bindwallet_select_linkwallet", R.string.act_bindwallet_select_linkwallet));
        binding.tvCancel.setText(LocaleController.getString("act_bindwallet_cancel", R.string.act_bindwallet_cancel));
        binding.ivBack.setOnClickListener(view -> finishFragment());

        binding.llWalletMetaMask.setOnClickListener(view -> {
            WalletUtil.walletConnect(BlockchainConfig.PKG_META_MASK);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_metamask点击, new HashMap<>());
        });
        binding.llWalletImtoken.setOnClickListener(view -> {
            WalletUtil.walletConnect(BlockchainConfig.PKG_IMTOKEN);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_imToken点击, new HashMap<>());
        });
//        binding.llWalletTrust.setOnClickListener(view -> {
//            TelegramUtil.walletConnect(BlockchainConfig.PKG_TRUST_WALLET);
//        });
        binding.llWalletTokenPocket.setOnClickListener(view -> {
            WalletUtil.walletConnect(BlockchainConfig.PKG_TOKEN_POCKET);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_tokenpocket点击, new HashMap<>());
        });
        binding.llWalletSpot.setOnClickListener(view -> {
            WalletUtil.walletConnect(BlockchainConfig.PKG_SPOT_WALLET);
        });
        binding.tvCancel.setOnClickListener(view -> {
            finishFragment();
        });
    }

    private void initData() {
        String pkg = MMKVUtil.connectedWalletPkg();
        if (BlockchainConfig.PKG_META_MASK.equals(pkg)) {
            binding.llWalletMetaMask.getChildAt(2).setVisibility(View.VISIBLE);
        } else if (BlockchainConfig.PKG_IMTOKEN.equals(pkg)) {
            binding.llWalletImtoken.getChildAt(2).setVisibility(View.VISIBLE);
        } else if (BlockchainConfig.PKG_TRUST_WALLET.equals(pkg)) {
            binding.llWalletTrust.getChildAt(2).setVisibility(View.VISIBLE);
        } else if (BlockchainConfig.PKG_TOKEN_POCKET.equals(pkg)) {
            binding.llWalletTokenPocket.getChildAt(2).setVisibility(View.VISIBLE);
        } else if (BlockchainConfig.PKG_SPOT_WALLET.equals(pkg)) {
            binding.llWalletSpot.getChildAt(2).setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.WALLET_CONNECT_APPROVED:
                if (BlockchainConfig.PKG_META_MASK.equals(MMKVUtil.connectedWalletPkg())) {
                    EventUtil.track(getContext(), EventUtil.Even.metamask链接成功, new HashMap<>());
                } else if (BlockchainConfig.PKG_IMTOKEN.equals(MMKVUtil.connectedWalletPkg())) {
                    EventUtil.track(getContext(), EventUtil.Even.imToken链接成功, new HashMap<>());
                } else if (BlockchainConfig.PKG_TOKEN_POCKET.equals(MMKVUtil.connectedWalletPkg())) {
                    EventUtil.track(getContext(), EventUtil.Even.tokenpocket链接成功, new HashMap<>());
                }

                finishFragment();
                break;
            case EventBusTags.WALLET_CONNECT_CLOSED:
                break;
        }
    }
}
