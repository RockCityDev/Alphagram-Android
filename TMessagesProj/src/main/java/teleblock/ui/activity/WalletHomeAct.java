package teleblock.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.android.material.appbar.AppBarLayout;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.request.HttpRequest;
import com.luck.picture.lib.config.Crop;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActWalletHomeBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.NFTAssets;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.NftInfoApi;
import teleblock.network.api.WalletInfoApi;
import teleblock.network.api.blockchain.ethereum.EthSingleNftApi;
import teleblock.network.api.blockchain.polygon.PolygonSingleNftApi;
import teleblock.ui.dialog.ChainTypeSelectorDialog;
import teleblock.ui.dialog.WalletOperaDialog;
import teleblock.ui.view.WalletNftsView;
import teleblock.ui.view.WalletTokensView;
import teleblock.ui.view.WalletTransactionView;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.NftHexagonView;
import teleblock.widget.ViewPageAdapter;

/**
 * 钱包主页
 */
public class WalletHomeAct extends BaseFragment implements View.OnClickListener, AppBarLayout.OnOffsetChangedListener {

    public ActWalletHomeBinding binding;
    private long currentTimeMillis;
    public boolean userSelf;
    public String address;
    private TLRPC.User user;
    public NFTInfo nftInfo;
    private int scrollMaxHeight;
    private long otherUserId = -1;

    //钱包操作对话框
    private WalletOperaDialog mWalletOperaDialog;
    private WalletNetworkConfigEntity.WalletNetworkConfigChainType currentChainType;
    private ChainTypeSelectorDialog chainTypeSelectorDialog;

    public WalletHomeAct(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            currentTimeMillis = arguments.getLong("currentTimeMillis");
            address = getArguments().getString("address");
            userSelf = getArguments().getBoolean("userSelf");
            otherUserId = getArguments().getLong("otherUserId", -1);
        }
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        setNavigationBarColor(Color.WHITE, true);
        binding = ActWalletHomeBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        initView();
        initData();
        return fragmentView;
    }

    private void initView() {
        binding.appBar.addOnOffsetChangedListener(this);
        binding.tvChainType.setOnClickListener(this);
        binding.ivWalletClose.setOnClickListener(this);
        binding.tvWalletClose.setOnClickListener(this);
        binding.llWalletAddress1.setOnClickListener(this);
        binding.tvSearchWallet.setOnClickListener(this);

        binding.tvContractAddressTitle.setText(LocaleController.getString("wallet_home_act_contract_address_title", R.string.wallet_home_act_contract_address_title));
        binding.tvTokenIdTitle.setText(LocaleController.getString("wallet_home_act_token_id_title", R.string.wallet_home_act_token_id_title));
        binding.tvBlockchainTitle.setText(LocaleController.getString("wallet_home_act_blockchain_title", R.string.wallet_home_act_blockchain_title));
        binding.tvTokenStandardTitle.setText(LocaleController.getString("wallet_home_act_token_standard_title", R.string.wallet_home_act_token_standard_title));
        binding.etWalletAddress.setHint(LocaleController.getString("wallet_home_input", R.string.wallet_home_input));
        binding.tvSearchWallet.setText(LocaleController.getString("wallet_home_btn", R.string.wallet_home_btn));
    }

    private void initData() {
        binding.viewStatusBar.getLayoutParams().height = AndroidUtilities.statusBarHeight;
        binding.layout.setMinimumHeight(AndroidUtilities.statusBarHeight + SizeUtils.dp2px(55 + 48));
        scrollMaxHeight = ScreenUtils.getScreenWidth() - AndroidUtilities.statusBarHeight - SizeUtils.dp2px(55);

        if (userSelf) { // 查看自己
            user = getUserConfig().getCurrentUser();
            binding.tvTgName.setText("@" + user.username);
            binding.llMyselfContent.setVisibility(View.VISIBLE);
            binding.llOthersContent.setVisibility(View.GONE);
            String pkg = MMKVUtil.connectedWalletPkg();
            binding.ivWalletLogo1.setImageResource(BlockchainConfig.getWalletIconByPkg(pkg));
            binding.ivWalletAddress.setImageResource(R.drawable.wallet_address_setting);
        } else { // 查看别人
            binding.llMyselfContent.setVisibility(View.GONE);
            binding.llOthersContent.setVisibility(View.VISIBLE);
            binding.ivWalletLogo1.setVisibility(View.GONE);
            binding.ivWalletAddress.setImageResource(R.drawable.wallet_address_copy);
        }

        updateChainType();
        binding.tvWalletAddress1.setText(WalletUtil.formatAddress(address));
        getWalletInfo();
        initTabLayout();
    }

    private void updateChainType() {
        currentChainType = MMKVUtil.currentChainConfig();
        if (currentChainType == null) return;
        binding.tvChainType.setText(currentChainType.getName());
        GlideHelper.getDrawableGlide(getContext(), currentChainType.getIcon(), drawable -> binding.tvChainType.getHelper().setIconNormalLeft(drawable));
    }

    private void initTabLayout() {
        String[] titles = {
                LocaleController.getString("wallet_home_act_bar_tokenid", R.string.wallet_home_act_bar_tokenid),
                LocaleController.getString("wallet_home_act_bar_nft", R.string.wallet_home_act_bar_nft),
                LocaleController.getString("wallet_home_act_bar_transactionhistory", R.string.wallet_home_act_bar_transactionhistory)
        };
        List<View> views = new ArrayList<>();
        views.add(new WalletTokensView(this));
        views.add(new WalletNftsView(this));
        views.add(new WalletTransactionView(this));
        binding.tabLayout.setTitle(titles);
        binding.viewPager.setAdapter(new ViewPageAdapter(views));
        binding.viewPager.setOffscreenPageLimit(views.size());
        binding.tabLayout.setViewPager(binding.viewPager);
        binding.tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                Map map = new HashMap();
                map.put("clickTab", titles[position]);
                EventUtil.track(getContext(), EventUtil.Even.资产页Tab点击, map);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
    }

    /**
     * 錢包主頁搜尋錢包時會3種：
     * 1.是TG用戶有設置過NFT頭像：有NFT頭像 錢包頁
     * 2.是TG用戶沒設置過NFT頭像：TG預設頭像 錢包頁
     * 3.不是TG用戶：NFT預設頭像 錢包頁
     */
    private void getWalletInfo() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new WalletInfoApi()
                        .setWallet_address(address))
                .request(new OnHttpListener<BaseBean<List<WalletInfo>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<WalletInfo>> result) {
                        if (isFinished) return;
                        if (!CollectionUtils.isEmpty(result.getData())) {
                            WalletInfo walletInfo = null;
                            if (userSelf) {
                                walletInfo = CollectionUtils.find(result.getData(), item -> item.getTg_user_id() == user.id);
                            } else {
                                for (WalletInfo walletInfoItem : result.getData()) {
                                    if (otherUserId == walletInfoItem.getTg_user_id()) {
                                        walletInfo = walletInfoItem;
                                        user = getMessagesController().getUser(otherUserId);
                                    }
                                }
                            }
                            if (walletInfo == null) {
                                return;
                            }
                            if (walletInfo.getTg_user_id() != 0) { // TG用户
                                if (!TextUtils.isEmpty(walletInfo.nft_contract_image)) { // NFT头像
                                    setNftImage(walletInfo.nft_contract_image, walletInfo.chain_id);
                                    updateNftInfo(walletInfo);
                                } else { // TG头像
                                    if (user != null) {
                                        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
                                        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));

                                        BackupImageView avatarImageView1 = new BackupImageView(getParentActivity());
                                        avatarImageView1.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_BIG), "300_300", avatarDrawable, user);
                                        binding.flAvatarBig.removeAllViews();
                                        binding.flAvatarBig.addView(avatarImageView1);

                                        BackupImageView avatarImageView2 = new BackupImageView(getParentActivity());
                                        avatarImageView2.getImageReceiver().setRoundRadius(AndroidUtilities.dp(20));
                                        avatarImageView2.setForUserOrChat(user, avatarDrawable);
                                        binding.flAvatarSmall.removeAllViews();
                                        binding.flAvatarSmall.addView(avatarImageView2);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    private void setNftImage(String nft_contract_image, int chainId) {
        Glide.with(getParentActivity()).load(nft_contract_image).into(binding.ivAvatarBig);
        GlideHelper.getDrawableGlide(getParentActivity(), nft_contract_image, drawable -> {
            Bitmap bitmap = ConvertUtils.drawable2Bitmap(drawable);
            binding.ivAvatarBig.setImageBitmap(bitmap);
            binding.ivAvatarSmall.setVisibility(View.GONE);
            binding.flAvatarSmall.removeAllViews();
            NftHexagonView nftHexagonView = new NftHexagonView(getParentActivity());
            nftHexagonView.setModel(NftHexagonView.DEFULT);
            nftHexagonView.setBitmap(bitmap);
            binding.flAvatarSmall.addView(nftHexagonView);
        });
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), getContext().getResources().getIdentifier("user_chain_logo_" + chainId, "drawable", getContext().getPackageName()));
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.transparent_space_icon);
        }

        binding.ivCoinIcon.setImageBitmap(bitmap);
    }

    private void updateNftInfo(WalletInfo walletInfo) {
        WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(walletInfo.nft_chain_id);
        binding.tvNftName.setText(walletInfo.nft_name);
        if (!TextUtils.isEmpty(walletInfo.nft_price) && chainType != null) {
            binding.tvNftPrice.setText(walletInfo.nft_price + " " + chainType.getMain_currency_name());
        }
        if (!userSelf) {
            binding.tvContractAddressContent.setText(WalletUtil.formatAddress(walletInfo.nft_contract));
            binding.tvTokenIdContent.setText(walletInfo.nft_token_id);
            binding.tvBlockchainContent.setText(chainType != null ? chainType.getName() : "");
            binding.tvTokenStandardContent.setText(walletInfo.nft_token_standard);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chain_type:
                if (chainTypeSelectorDialog == null) {
                    chainTypeSelectorDialog = new ChainTypeSelectorDialog(getContext(), new ChainTypeSelectorDialog.TransferSelectorChaintypeDialogListener() {
                        @Override
                        public void selectorChainData(WalletNetworkConfigEntity.WalletNetworkConfigChainType data) {
                            MMKVUtil.currentChainConfig(data);
                            updateChainType();
                            if (data.getId() == 1) {
                                EventUtil.track(getContext(), EventUtil.Even.资产页Eth点击, new HashMap());
                            } else if (data.getId() == 108) {
                                EventUtil.track(getContext(), EventUtil.Even.资产页TT点击, new HashMap());
                            } else if (data.getId() == 137) {
                                EventUtil.track(getContext(), EventUtil.Even.资产页Polygon点击, new HashMap());
                            } else if (data.getId() == 42262) {
                                EventUtil.track(getContext(), EventUtil.Even.资产页Oasis点击, new HashMap());
                            }
                        }
                    });
                }
                chainTypeSelectorDialog.setCurrentChainType(MMKVUtil.currentChainConfig());
                chainTypeSelectorDialog.show();
                break;
            case R.id.tv_wallet_close:
            case R.id.iv_wallet_close:
                finishFragment();
                break;
            case R.id.ll_wallet_address1:
                if (!userSelf) {
                    ClipboardUtils.copyText(address);
                    BulletinFactory.of(this).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), getResourceProvider()).show();
                    return;
                }
                if (mWalletOperaDialog == null) {
                    mWalletOperaDialog = new WalletOperaDialog(getParentActivity(), address, new WalletOperaDialog.WalletOperaDialogListener() {
                        @Override
                        public void disconnect() {
                            //清空存储的钱包地址
                            MMKVUtil.connectedWalletAddress("");
                            MMKVUtil.connectedWalletPkg("");
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_CLOSED));
                            finishFragment();
                        }

                        @Override
                        public void changeAddress() {
                            presentFragment(new WalletBindAct());
                        }

                        @Override
                        public void copyAddress() {
                            ClipboardUtils.copyText(address);
                            BulletinFactory.of(WalletHomeAct.this).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), getResourceProvider()).show();
                        }
                    });
                }
                mWalletOperaDialog.show();
                break;
            case R.id.tv_search_wallet:
                if (TextUtils.isEmpty(binding.etWalletAddress.getText().toString())) {
                    ToastUtils.showLong(getContext().getString(R.string.wallet_home_tip_input_address));
                    return;
                }
                Bundle args = new Bundle();
                args.putString("address", binding.etWalletAddress.getText().toString());
                args.putBoolean("userSelf", false);
                presentFragment(new WalletHomeAct(args));
                break;
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                Uri output = Crop.getOutput(data);
                if (output == null) return;
                NftInfoApi nftInfoApi = new NftInfoApi()
                        .setNft_path(output.getPath())
                        .setNft_contract(nftInfo.contract_address)
                        .setNft_contract_image(nftInfo.original_url)
                        .setNft_token_id(nftInfo.token_id)
                        .setNft_name(nftInfo.asset_name)
                        .setNft_chain_id(currentChainType.getId())
                        .setNft_price(nftInfo.getEthPrice())
                        .setNft_token_standard(nftInfo.token_standard);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPLOAD_USER_PROFILE, currentTimeMillis + "", nftInfoApi));
                WalletInfo walletInfo = new WalletInfo();
                walletInfo.nft_contract_image = nftInfo.original_url;
                walletInfo.nft_contract = nftInfo.contract_address;
                walletInfo.nft_token_id = nftInfo.token_id;
                walletInfo.nft_chain_id = currentChainType.getId();
                setNftImage(walletInfo.nft_contract_image, walletInfo.chain_id);
                updateNftInfo(walletInfo);
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int scrollDistance = Math.min(-verticalOffset, scrollMaxHeight);
        float statusAlpha = (float) scrollDistance / scrollMaxHeight;
        binding.topLayout.setAlpha(statusAlpha);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.WALLET_CONNECT_APPROVED:
                address = MMKVUtil.connectedWalletAddress();
            case EventBusTags.CHAIN_TYPE_CHANGED:
                initData();
                break;
        }
    }
}
