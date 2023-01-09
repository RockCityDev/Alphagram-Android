package teleblock.ui.activity;

import static teleblock.widget.TelegramUserAvatar.ADDRESS_TRANSFER;
import static teleblock.widget.TelegramUserAvatar.DEFAUTL;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.ruffian.library.widget.RImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ActTransferBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.CameraScanActivity;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.CameraScanEntity;
import teleblock.model.TransferHistoryEntity;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.TransferHistoryApi;
import teleblock.ui.adapter.MyTransferFriendAdapter;
import teleblock.ui.adapter.RecenttransactionsAdapter;
import teleblock.ui.dialog.ErrorWalletAddressDialog;
import teleblock.ui.dialog.TransferDialog;
import teleblock.util.ContactInfoUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：转账页面
 */
public class TransferActivity extends BaseFragment {

    private ActTransferBinding binding;
    //最近交易适配器
    private RecenttransactionsAdapter mRecenttransactionsAdapter;
    //我的好友适配器
    private MyTransferFriendAdapter mMyTransferFriendAdapter;
    private View emptyView, emptyViewFriend;
    private List<WalletInfo> hasWalletFriends = new ArrayList<>();
    private List<TransferHistoryEntity> transferList = new ArrayList<>();
    private String toAddress;
    private long toTgId;

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        EventBus.getDefault().unregister(this);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);

        binding = ActTransferBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();

        inintView();
        loadFriends();
        loadHistory();
        return fragmentView;
    }

    private void inintView() {
        //无数据的view
        emptyView = getEmptyView();
        emptyView.setVisibility(View.GONE);
        emptyViewFriend = getEmptyView();
        emptyViewFriend.setVisibility(View.GONE);

        binding.rlTop.setPadding(0, AndroidUtilities.statusBarHeight + 16, 0, 16);
        binding.ivClosePage.setOnClickListener(view -> finishFragment());
        binding.tvTitle.setText(LocaleController.getString("transfer_activity_title", R.string.transfer_activity_title));
        binding.tvSendTitle.setText(LocaleController.getString("transfer_activity_sended", R.string.transfer_activity_sended));
        binding.etSearchAccount.setHint(LocaleController.getString("transfer_activity_search_hint", R.string.transfer_activity_search_hint));
        binding.tvRecenttransactionsTitle.setText(LocaleController.getString("transfer_activity_recenttransactions", R.string.transfer_activity_recenttransactions));
        binding.tvMyfriendTitle.setText(LocaleController.getString("transfer_activity_myfriend", R.string.transfer_activity_myfriend));
        binding.tvNextstepBtn.setText(LocaleController.getString("transfer_activity_transfer_nextstep", R.string.transfer_activity_transfer_nextstep));
        binding.tvNoFriendTitle.setText(LocaleController.getString("transfer_activity_nobind_friend", R.string.transfer_activity_nobind_friend));
        binding.tvTips.setText(LocaleController.getString("transfer_activity_tips", R.string.transfer_activity_tips));
        binding.tvWalletTips.setText(LocaleController.getString("ac_wallet_tips", R.string.ac_wallet_tips));

        //显示自己头像
        binding.flAvatarFrom.setUserInfo(getUserConfig().getCurrentUser()).loadView();
        binding.tvFromAccount.setText(MMKVUtil.connectedWalletAddress());
        binding.llWalletTips.setOnClickListener(view -> {
            new ErrorWalletAddressDialog(getContext()).show();
        });

        //扫一扫
        binding.ivScreen.setOnClickListener(view -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.SHOW_CAMERA_FUNCTION, CameraScanActivity.SCAN_WALLET_ADDRESS));
        });
        //下一步按钮的点击事件
        binding.tvNextstepBtn.setOnClickListener(view -> {//跳转到转账页面
            //先获取钱包配置数据，然后在弹出转账对话框
            WalletUtil.requestWalletNetworkConfigData(() -> {
                //弹出转账对话框
                new TransferDialog(
                        getParentActivity(),
                        getUserConfig().getCurrentUser(),
                        toTgId == 0L ? null : AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(toTgId),
                        toAddress,
                        -1,
                        false,
                        parseStr -> {
                            //转账成功后，重新请求交易历史数据
                            loadHistory();
                        }
                ).show();
            });
        });

        binding.tvNextstepBtn.setClickable(false);

        //交易历史适配器
        binding.rvRecenttransactions.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecenttransactionsAdapter = new RecenttransactionsAdapter();
        binding.rvRecenttransactions.setAdapter(mRecenttransactionsAdapter);
        mRecenttransactionsAdapter.setOnItemClickListener((adapter, view, position) -> {
            binding.etSearchAccount.setVisibility(View.GONE);
            binding.llSelectorToAccount.setVisibility(View.VISIBLE);

            TransferHistoryEntity entity = mRecenttransactionsAdapter.getItem(position);
            long myId = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getClientUserId();
            if (("" + myId).equals(entity.receipt_tg_user_id)) {//转入
                toTgId = TextUtils.isEmpty(entity.payment_tg_user_id) ? 0L : Long.parseLong(entity.payment_tg_user_id);
                toAddress = entity.payment_account;
            } else {//转出
                toTgId = TextUtils.isEmpty(entity.receipt_tg_user_id) ? 0L : Long.parseLong(entity.receipt_tg_user_id);
                toAddress = entity.receipt_account;
            }

            binding.tvShowAddress.setText(toAddress);
            binding.etSearchAccount.setText(toAddress);
            checkNextBtn();
            //tg图片
            if (toTgId != 0L) {
                TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(toTgId);
                if (user != null) {
                    binding.flAvatarTo.setUserInfo(user).setModel(DEFAUTL).loadView();
                } else {
                    binding.flAvatarTo.setModel(ADDRESS_TRANSFER).loadView();
                }
            } else {
                binding.flAvatarTo.setModel(ADDRESS_TRANSFER).loadView();
            }
        });
        mRecenttransactionsAdapter.setEmptyView(emptyView);

        //我的好友适配器
        binding.rvMyfriend.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mMyTransferFriendAdapter = new MyTransferFriendAdapter();
        binding.rvMyfriend.setAdapter(mMyTransferFriendAdapter);
        mMyTransferFriendAdapter.setOnItemClickListener((adapter, view, position) -> {
            binding.etSearchAccount.setVisibility(View.GONE);
            binding.llSelectorToAccount.setVisibility(View.VISIBLE);
            WalletInfo walletInfo = mMyTransferFriendAdapter.getItem(position);
            toTgId = walletInfo.getTg_user_id();
            TLRPC.User toUser = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(toTgId);
            if (toUser != null) {
                binding.flAvatarTo.setUserInfo(toUser).setModel(DEFAUTL).loadView();
            }
            toAddress = walletInfo.getWallet_info().get(0).getWallet_address();
            binding.tvShowAddress.setText(toAddress);
            binding.etSearchAccount.setText(toAddress);
            checkNextBtn();
        });
        mMyTransferFriendAdapter.setEmptyView(emptyViewFriend);

        //关闭选择的账户
        binding.ivClose.setOnClickListener(view -> {
            toAddress = "";
            checkNextBtn();
            binding.etSearchAccount.setText("");
            binding.etSearchAccount.setVisibility(View.VISIBLE);
            binding.llSelectorToAccount.setVisibility(View.GONE);
        });

        //搜索账户
        binding.etSearchAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                toAddress = editable.toString();
                toTgId = 0L;
                if (!TextUtils.isEmpty(toAddress) && !toAddress.toLowerCase().startsWith("0x")) {
                    binding.llWalletTips.setVisibility(View.VISIBLE);
                } else {
                    binding.llWalletTips.setVisibility(View.GONE);
                }
                checkNextBtn();
            }
        });
    }

    private void loadFriends() {
        ContactInfoUtil.getInstance().load(data -> {
            if (data == null || data.size() == 0) return;
            hasWalletFriends = data;
            mMyTransferFriendAdapter.setList(hasWalletFriends);
            checkTipsView();
        });
    }

    private void loadHistory() {
        transferList.clear();
        EasyHttp.post(new ApplicationLifecycle()).api(new TransferHistoryApi()).request(new OnHttpListener<BaseBean<BaseLoadmoreModel<TransferHistoryEntity>>>() {
            @Override
            public void onSucceed(BaseBean<BaseLoadmoreModel<TransferHistoryEntity>> result) {
                if (!CollectionUtils.isEmpty(result.getData().getData())) {
                    transferList.addAll(result.getData().getData());
                }
                mRecenttransactionsAdapter.setList(transferList);
                checkTipsView();
            }

            @Override
            public void onFail(Exception e) {
            }
        });
    }

    private void checkNextBtn() {
        if (!TextUtils.isEmpty(toAddress) && toAddress.toLowerCase().startsWith("0x")) {
            binding.tvNextstepBtn.setClickable(true);
            binding.tvNextstepBtn.setAlpha(1.0f);
        } else {
            binding.tvNextstepBtn.setClickable(false);
            binding.tvNextstepBtn.setAlpha(0.5f);
        }
    }

    private void checkTipsView() {
        if (hasWalletFriends.size() == 0 && transferList.size() == 0) {
            binding.llBottom.setVisibility(View.VISIBLE);
        }
        if (hasWalletFriends.size() == 0) {
            emptyViewFriend.setVisibility(View.VISIBLE);
        }
        if (transferList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取空布局view
     *
     * @return
     */
    private View getEmptyView() {
        return LayoutInflater.from(getParentActivity()).inflate(R.layout.transfer_emptyview, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.CAMERA_SCAN_RESULT:
                CameraScanEntity scanEntity = (CameraScanEntity) event.getData();
                if (scanEntity.type == CameraScanActivity.SCAN_WALLET_ADDRESS) {
                    String address = scanEntity.data;
                    //判断是不是小狐狸钱包的二维码，如果是则按照这个进行截取
                    if (!address.isEmpty() && address.length() >= 52 && address.contains("0x") && address.toLowerCase().startsWith("ethereum:")) {
                        toAddress = address.substring(address.indexOf(":") + 1, address.indexOf(":") + 43);
                    } else {
                        toAddress = address;
                    }

                    binding.etSearchAccount.setText(toAddress);
                }
                break;
        }
    }
}
