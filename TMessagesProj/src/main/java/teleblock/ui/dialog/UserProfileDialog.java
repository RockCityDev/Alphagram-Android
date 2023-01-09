package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogUserProfileBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;

import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.wallet.WalletInfo;
import teleblock.util.TelegramUtil;

/**
 * 创建日期：2022/7/15
 * 描述：用户简介
 */
public class UserProfileDialog extends BaseBottomSheetDialog implements NotificationCenter.NotificationCenterDelegate {

    private DialogUserProfileBinding binding;
    private ChatActivity fragment;
    private TLRPC.UserFull userInfo;
    private TLRPC.User user;
    private long userId;
    private long dialogId;

    public UserProfileDialog(@NonNull ChatActivity fragment, long userId) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.userId = userId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        binding = DialogUserProfileBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.chatSearchResultsAvailable);

        initView();
        initData();
    }

    private void initView() {
        binding.tabLayout.setTabData(new String[]{"TG 個人頁", "錢包頁"});
        binding.tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                binding.userPersonalView.setVisibility(View.INVISIBLE);
                binding.userWalletView.setVisibility(View.INVISIBLE);
                if (position == 0) binding.userPersonalView.setVisibility(View.VISIBLE);
                else if (position == 1) binding.userWalletView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
    }

    private void initData() {
        dialogId = fragment.getDialogId();
        user = fragment.getMessagesController().getUser(userId);
        userInfo = fragment.getMessagesController().getUserFull(user.id);
        binding.userPersonalView.setData(fragment, user, userInfo);
        fragment.getMessagesController().loadFullUser(user, fragment.getClassGuid(), true);

        //获取用户的nft信息数据
        TelegramUtil.getUserNftData(userId, new TelegramUtil.UserNftDataListener() {
            @Override
            public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                if (!CollectionUtils.isEmpty(walletInfoList)) {
                    WalletInfo walletInfo = walletInfoList.get(0);
                    binding.userPersonalView.setNftData();
                    if (walletInfo != null && !CollectionUtils.isEmpty(walletInfo.getWallet_info())) {
                        binding.userPersonalView.setWalletData(walletInfo);
                    }
                }
            }

            @Override
            public void nftDataRequestError(String errorMsg) {

            }
        });
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(50));
        getWindow().setGravity(Gravity.BOTTOM);
        FrameLayout bottomSheet = findViewById(R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.bg_dialog_user_profile);
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_dialog_user_profile));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.chatSearchResultsAvailable);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userInfoDidLoad) {
            userInfo = (TLRPC.UserFull) args[1];
            user = fragment.getMessagesController().getUser(user.id);
            binding.userPersonalView.setData(fragment, user, userInfo);
        } else if (id == NotificationCenter.encryptedChatCreated) {
            AndroidUtilities.runOnUIThread(() -> {
                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                Bundle args2 = new Bundle();
                args2.putInt("enc_id", encryptedChat.id);
                fragment.presentFragment(new ChatActivity(args2));
                dismiss();
            });
        } else if (id == NotificationCenter.chatSearchResultsAvailable) {
            int foundMessagePosition = (Integer) args[4];
            if (fragment.getMediaDataController().getFoundMessageObjects().size() > 0 && fragment.getMediaDataController().getFoundMessageObjects().size() > foundMessagePosition) {
                MessageObject messageObject = fragment.getMediaDataController().getFoundMessageObjects().get(foundMessagePosition);
                binding.userPersonalView.setSearchData((Integer) args[5], messageObject.messageOwner.date);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DISMISS_DIALOG:
                dismiss();
                break;
            case EventBusTags.SHOW_DIALOG:
                show();
                break;
        }
    }
}