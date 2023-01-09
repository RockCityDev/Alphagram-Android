package teleblock.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ViewUserPersonalBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivity;

import java.util.HashMap;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.wallet.WalletInfo;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.CommonChatsAdapter;
import teleblock.ui.dialog.TransferDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.EventUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/6/9
 * 描述：用户简介-个人页
 */
public class UserPersonalView extends FrameLayout implements View.OnClickListener, OnItemClickListener {

    private ViewUserPersonalBinding binding;
    private ChatActivity fragment;
    private TLRPC.User user;
    private TLRPC.UserFull userInfo;
    private AboutLinkCell aboutLinkCell;
    private boolean loading;
    private CommonChatsAdapter commonChatsAdapter;

    //用户昵称
    private String userName;

    //钱包地址
    private String walletAddress = "";
    private int userChainId = -1;

    public UserPersonalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        binding = ViewUserPersonalBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Theme.createProfileResources(getContext());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commonChatsAdapter = new CommonChatsAdapter();
        commonChatsAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(commonChatsAdapter);

        binding.tvCloseDialog.setOnClickListener(this);
        binding.rtvUserName.setOnClickListener(this);
        binding.llProfileMessage.setOnClickListener(this);
        binding.llProfileVoice.setOnClickListener(this);
        binding.llProfileMainPage.setOnClickListener(this);
        binding.llProfileSecret.setOnClickListener(this);
        binding.llProfileAddFriend.setOnClickListener(this);
        binding.tvUserSearch.setOnClickListener(this);

        binding.tvMsg.setText(LocaleController.getString("user_personal_message", R.string.user_personal_message));
        binding.tvVoice.setText(LocaleController.getString("user_personal_voice", R.string.user_personal_voice));
        binding.tvMainPage.setText(LocaleController.getString("user_personal_main_page", R.string.user_personal_main_page));
        binding.tvCommune.setText(LocaleController.getString("user_personal_secret_chat", R.string.user_personal_secret_chat));
        binding.tvAddFriend.setText(LocaleController.getString("user_personal_add_friend", R.string.user_personal_add_friend));
        binding.tvIntroduction.setText(LocaleController.getString("user_personal_introduction", R.string.user_personal_introduction));
        binding.tvProfileExpand.setText(LocaleController.getString("fg_textview_expand", R.string.fg_textview_expand));
        binding.tvLastMsgDate.setText(LocaleController.getString("user_personal_last_msg_date", R.string.user_personal_last_msg_date));
        binding.tvOnlineTimeTitle.setText(LocaleController.getString("user_personal_online_time", R.string.user_personal_online_time));
        binding.tvCommonChats.setText(LocaleController.getString("user_personal_common_group", R.string.user_personal_common_group));
        binding.tvWallletTitle.setText(LocaleController.getString("dialog_user_personal_wallet", R.string.dialog_user_personal_wallet));
        binding.tvSeeWalletHome.setText(LocaleController.getString("dialog_user_personal_look_wallet", R.string.dialog_user_personal_look_wallet));
        binding.tvTransferTo.setText(LocaleController.getString("dialog_user_personal_transfer_to_he", R.string.dialog_user_personal_transfer_to_he));

        //根据后台配置，如果有网址，则显示？图标，如果没有则不显示
        if (!MMKVUtil.getSystemMsg().wallet_way.isEmpty()) {
            Drawable filledDrawable = ResourceUtils.getDrawable(R.drawable.filled_faq_icon);
            binding.tvWallletTitle.getHelper().setIconNormalRight(filledDrawable);
        }

        //查看大图
        binding.flAvatarContainer.setOnClickListener(v -> {
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(fragment);
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        });

        //点击跳转 h5 web3页面
        binding.tvWallletTitle.setOnClickListener(v -> {
            if (MMKVUtil.getSystemMsg().wallet_way.isEmpty()) {
                return;
            }
            Browser.openUrl(fragment.getContext(), MMKVUtil.getSystemMsg().wallet_way);
        });

        //点击复制钱包地址
        binding.tvCopyWalletAddress.setOnClickListener(v -> {
            ClipboardUtils.copyText(walletAddress);
            BulletinFactory.of(binding.getRoot(), fragment.getResourceProvider()).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), fragment.getResourceProvider()).show();
        });

        //点击跳转到对方钱包页面
        binding.tvSeeWalletHome.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.他人主页_查看钱包, new HashMap<>());
            Bundle args = new Bundle();
            args.putString("address", walletAddress);
            args.putBoolean("userSelf", false);
            args.putLong("otherUserId", user.id);
            fragment.presentFragment(new WalletHomeAct(args));
            EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
        });

        //点击弹出转账窗口
        binding.tvTransferTo.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.他人主页_转账给他, new HashMap<>());
            new TransferDialog(
                    getContext(),
                    fragment.getUserConfig().getCurrentUser(),
                    user,
                    walletAddress,
                    userChainId,
                    false,
                    parseStr -> {
                    }
            ).show();

            EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
        });
    }

    public void setData(ChatActivity fragment, TLRPC.User user, TLRPC.UserFull userInfo) {
        this.fragment = fragment;
        this.user = user;
        this.userInfo = userInfo;

        //默认头像
        binding.flAvatarContainer.setUserInfo(user).setBorder(SizeUtils.dp2px(4f), Color.WHITE).loadView();

        //用户昵称
        userName = String.valueOf(Emoji.replaceEmoji(UserObject.getUserName(user), null, AndroidUtilities.dp(24), false));
        binding.tvFullName.requestFocus();
        binding.tvFullName.setText(userName);

        if (!TextUtils.isEmpty(user.username)) {
            binding.rtvUserName.setVisibility(VISIBLE);
            binding.rtvUserName.setText("@" + user.username);
        }
        if (user.contact) {
            binding.ivAddFriend.setSelected(true);
            binding.tvAddFriend.setText(LocaleController.getString("user_personal_edit_friend", R.string.user_personal_edit_friend));
        }
        binding.tvOnlineTimeContent.setText(LocaleController.formatUserStatus(fragment.getCurrentAccount(), user));

        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.about)) {
                if (aboutLinkCell == null) {
                    aboutLinkCell = new AboutLinkCell(getContext(), fragment, true) {
                        @Override
                        protected void didPressUrl(String url) {
                            openUrl(url);
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                        }

                        @Override
                        protected void didResizeEnd() {
                        }

                        @Override
                        protected void didResizeStart() {
                        }
                    };
                    binding.flProfileInfo.addView(aboutLinkCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.NO_GRAVITY, -2, 0, 0, 0));
                }
                aboutLinkCell.setNofitTheme(false);
                aboutLinkCell.setText(userInfo.about, true);
            } else {
                binding.llProfileTitle.setVisibility(GONE);
            }
            fragment.getMediaDataController().searchMessagesInChat("", fragment.getDialogId(), fragment.getMergeDialogId(), fragment.getClassGuid(), 0, fragment.getThreadId(), false, user, null, false);
            getChats(0, 100);
        } else {
            queryServerSearch(userName);
        }
    }

    private void queryServerSearch(String name) {
        long channelId = ChatObject.isChannel(fragment.getCurrentChat()) ? fragment.getCurrentChat().id : 0;
        if (channelId == 0) return;
        TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.filter = new TLRPC.TL_channelParticipantsSearch();
        req.filter.q = name;
        req.limit = 10;
        req.offset = 0;
        req.channel = fragment.getMessagesController().getInputChannel(channelId);
        fragment.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                fragment.getMessagesController().putUsers(res.users, false);
                fragment.getMessagesController().putChats(res.chats, false);
//                for (TLRPC.ChannelParticipant participant:res.participants){
//                    if (participant.peer.user_id==user.id){
//                        Timber.i("加群时间-->"+LocaleController.formatJoined(participant.date));
//                    }
//                }
                // 重新请求之前报错的接口
                for (TLRPC.User user1 : res.users) {
                    if (user1.id == user.id) {
                        user = fragment.getMessagesController().getUser(user.id);
                        fragment.getMessagesController().loadFullUser(user, fragment.getClassGuid(), true);
                        break;
                    }
                }
            }
        }));
    }

    private void openUrl(String url) {
        if (url.startsWith("@")) {
            fragment.getMessagesController().openByUserName(url.substring(1), fragment, 0);
        } else if (url.startsWith("#")) {
            DialogsActivity fragment = new DialogsActivity(null);
            fragment.setSearchString(url);
            fragment.presentFragment(fragment);
        } else if (url.startsWith("/")) {
            fragment.chatActivityEnterView.setCommand(null, url, false, false);
        }
    }

    private void getChats(long max_id, final int count) {
        if (loading) {
            return;
        }
        TLRPC.TL_messages_getCommonChats req = new TLRPC.TL_messages_getCommonChats();
        req.user_id = fragment.getMessagesController().getInputUser(user);
        if (req.user_id instanceof TLRPC.TL_inputUserEmpty) {
            return;
        }
        req.limit = count;
        req.max_id = max_id;
        loading = true;
        fragment.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.messages_Chats res = (TLRPC.messages_Chats) response;
                fragment.getMessagesController().putChats(res.chats, false);
                commonChatsAdapter.setList(res.chats);
                if (res.chats.isEmpty()) binding.tvCommonChats.setVisibility(GONE);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.SHOW_DIALOG));
            }
            loading = false;
        }));
    }

    public void setSearchData(Integer count, long date) {
        String format = LocaleController.getString("user_personal_msg_num", R.string.user_personal_msg_num);
        String formatStr = String.format(format, count);
        binding.tvUserSearch.setText(formatStr);

        String text = LocaleController.getString("user_personal_last_msg_date", R.string.user_personal_last_msg_date);
        String language = LocaleController.getInstance().getCurrentLocale().getLanguage();
        String pattern = "zh".equals(language) ? "yyyy/MM/dd HH:mm" : "MM/dd/yyyy HH:mm";
        binding.tvLastMsgDate.setText(text + " " + TimeUtils.millis2String(date * 1000, TimeUtils.getSafeDateFormat(pattern)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close_dialog:
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                break;
            case R.id.rtv_user_name:
            case R.id.ll_profile_message:
                Bundle args = new Bundle();
                args.putLong("user_id", user.id);
                if (!fragment.getMessagesController().checkCanOpenChat(args, fragment)) {
                    return;
                }
                ChatActivity chatActivity = new ChatActivity(args);
                chatActivity.setPreloadedSticker(fragment.getMediaDataController().getGreetingsSticker(), false);
                fragment.presentFragment(chatActivity);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                break;
            case R.id.ll_profile_voice:
                // 代码来自：ProfileActivity#1815
                if (user != null) {
                    VoIPHelper.startCall(user, false, userInfo != null && userInfo.video_calls_available, fragment.getParentActivity(), userInfo, fragment.getAccountInstance());
                }
                break;
            case R.id.ll_profile_secret:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), null);
                builder.setTitle(LocaleController.getString("AreYouSureSecretChatTitle", R.string.AreYouSureSecretChatTitle));
                builder.setMessage(LocaleController.getString("AreYouSureSecretChat", R.string.AreYouSureSecretChat));
                builder.setPositiveButton(LocaleController.getString("Start", R.string.Start), (dialogInterface, i) -> {
                    fragment.getSecretChatHelper().startSecretChat(fragment.getParentActivity(), user);
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                fragment.showDialog(builder.create());
                break;
            case R.id.ll_profile_add_friend:
                args = new Bundle();
                args.putLong("user_id", user.id);
                args.putBoolean("addContact", !user.contact);
                fragment.presentFragment(new ContactAddActivity(args));
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                break;
            case R.id.ll_profile_main_page:
                // 代码来自：ChatActivity#25355
                args = new Bundle();
                args.putLong("user_id", user.id);
                ProfileActivity profileActivity = new ProfileActivity(args);
                profileActivity.setPlayProfileAnimation(fragment.getCurrentUser() != null && fragment.getCurrentUser().id == user.id ? 1 : 0);
                AndroidUtilities.setAdjustResizeToNothing(fragment.getParentActivity(), fragment.getClassGuid());
                fragment.presentFragment(profileActivity);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                break;
            case R.id.tv_user_search:
                fragment.openSearchWithText(null);
                fragment.searchUserButton.performClick();
                fragment.searchUserMessages(user, null);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
                break;
        }
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        TLRPC.Chat chat = (TLRPC.Chat) adapter.getItem(position);
        Bundle args = new Bundle();
        args.putLong("chat_id", chat.id);
        if (!fragment.getMessagesController().checkCanOpenChat(args, fragment)) {
            return;
        }
        fragment.presentFragment(new ChatActivity(args));
        EventBus.getDefault().post(new MessageEvent(EventBusTags.DISMISS_DIALOG));
    }

    /**
     * 查看大图的provider
     */
    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {
        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
            int value = getLayoutParams().height;//加载头像的父布局高度
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(userInfo);
            BackupImageView avatarImageView = new BackupImageView(getContext());
            avatarImageView.setRoundRadius(AndroidUtilities.dp(value / 2f));
            avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), value + "_" + value, avatarDrawable, userInfo);

            if (fileLocation == null) {
                return null;
            }

            TLRPC.FileLocation photoBig = null;
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                photoBig = user.photo.photo_big;
            }

            if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int[] coords = new int[2];
                avatarImageView.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1];
                object.parentView = avatarImageView;
                object.imageReceiver = avatarImageView.getImageReceiver();
                if (user != null) {
                    object.dialogId = user.id;
                }
                object.thumb = object.imageReceiver.getBitmapSafe();
                object.size = -1;
                object.radius = avatarImageView.getImageReceiver().getRoundRadius();
                object.scale = avatarImageView.getScaleX();
                return object;
            }
            return null;
        }
    };

    public void setNftData() {
        //NftHexagonView.FORCE_DISPLAY
        binding.flAvatarContainer.setUserInfo(user).loadView();
    }

    /**
     * 设置钱包数据
     *
     * @param walletInfo
     */
    public void setWalletData(WalletInfo walletInfo) {
        binding.clWalletInfo.setVisibility(VISIBLE);
        binding.tvFullName.setVisibility(GONE);
        binding.vLine.setVisibility(VISIBLE);

        //用户昵称
        binding.tvWalletName.requestFocus();
        binding.tvWalletName.setText(userName);
        //链ID
        userChainId = walletInfo.chain_id;
        //链名称
        binding.tvChainType.setText(walletInfo.chain_name);
        //链图标
        GlideHelper.getDrawableGlide(getContext(), walletInfo.chain_icon, drawable -> {
            binding.tvChainType.getHelper().setIconNormalLeft(drawable);
        });

        //钱包地址
        if (!CollectionUtils.isEmpty(walletInfo.getWallet_info())) {
            walletAddress = walletInfo.getWallet_info().get(0).getWallet_address();
        }

        if (!walletAddress.isEmpty()) {
            //设置钱包地址
            binding.tvCopyWalletAddress.setText(WalletUtil.formatAddress(walletAddress));
        }
    }
}