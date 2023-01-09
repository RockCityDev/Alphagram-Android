package teleblock.ui.popup;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.PopupChannelLikeBinding;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.ReactionsContainerLayout;

import razerdp.basepopup.BasePopupWindow;
import teleblock.telegram.channels.ChannelMessage;

/**
 * Time:2022/7/18
 * Author:Perry
 * Description：频道点赞弹窗
 */
public class ChannelLikePopup extends BasePopupWindow {

    private BaseFragment fragment;
    private PopupChannelLikeBinding binding;

    private ReactionsContainerLayout reactionsLayout;
    private LinearLayout.LayoutParams params;
    int pad = 22;
    int sPad = 24;

    private ChannelLikePopupClickListener mChannelLikePopupClickListener;

    public ChannelLikePopup(BaseFragment fragment, ChannelLikePopupClickListener mChannelLikePopupClickListener) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.mChannelLikePopupClickListener = mChannelLikePopupClickListener;
        reactionsLayout = new ReactionsContainerLayout(fragment, fragment.getParentActivity(), UserConfig.selectedAccount, null);
        reactionsLayout.setCloseLongClick(true);
        reactionsLayout.setLayoutDirection();
        reactionsLayout.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4) + sPad, AndroidUtilities.dp(pad));
        params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52 + pad, Gravity.RIGHT, sPad, sPad, 0, -20);
        binding = PopupChannelLikeBinding.inflate(LayoutInflater.from(fragment.getParentActivity()));
        setContentView(binding.getRoot());
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onViewCreated(@NonNull View contentView) {
        super.onViewCreated(contentView);
        reactionsLayout.setDelegate(new ReactionsContainerLayout.ReactionsContainerDelegate() {
            @Override
            public void onReactionClicked(View v, ReactionsLayoutInBubble.VisibleReaction visibleReaction, boolean longpress, boolean addToRecent) {
                mChannelLikePopupClickListener.selectReaction(visibleReaction, longpress);
                dismiss();
            }

            @Override
            public void hideMenu() {
                dismiss();
            }
        });
    }

    public void showLikePopupwindow(View view, ChannelMessage channelMessage) {
        if (binding.rfl.getChildCount() == 0) {
            binding.rfl.addView(reactionsLayout, params);
            binding.rfl.setClipChildren(false);
        }
        TLRPC.ChatFull chatFull = fragment.getMessagesController().getChatFull(channelMessage.getChat().id);
        if (chatFull != null) {
            showLikePoppup(view, channelMessage.getMessageObject(), chatFull);
        } else {
            TLObject request;
            if (ChatObject.isChannel(channelMessage.getChat())) {
                TLRPC.TL_channels_getFullChannel req = new TLRPC.TL_channels_getFullChannel();
                req.channel = MessagesController.getInputChannel(channelMessage.getChat());
                request = req;
            } else {
                TLRPC.TL_messages_getFullChat req = new TLRPC.TL_messages_getFullChat();
                req.chat_id = channelMessage.getChat().id;
                request = req;
            }

            fragment.getConnectionsManager().sendRequest(request, (response, error) -> {
                if (error == null) {
                    AndroidUtilities.runOnUIThread(() -> {
                        TLRPC.TL_messages_chatFull res = (TLRPC.TL_messages_chatFull) response;
                        showLikePoppup(view, channelMessage.getMessageObject(), res.full_chat);
                    });
                }
            });
        }
    }

    private void showLikePoppup(View view, MessageObject messageObject, TLRPC.ChatFull full_chat) {
        boolean isReactionsAvailable;
        if (messageObject.isForwardedChannelPost()) {
            isReactionsAvailable = messageObject.isReactionsAvailable() && (full_chat != null && !(full_chat.available_reactions instanceof TLRPC.TL_chatReactionsNone));
        } else {
            isReactionsAvailable = !messageObject.isSecretMedia() && messageObject.isReactionsAvailable() && (full_chat != null && !(full_chat.available_reactions instanceof TLRPC.TL_chatReactionsNone));
        }

        if (full_chat == null || full_chat.available_reactions == null || !isReactionsAvailable) {
            ToastUtils.showShort(LocaleController.getString("fragment_channel_nolike_tips", R.string.fragment_channel_nolike_tips));
            return;
        }

        reactionsLayout.setMessage(messageObject, full_chat);
        //显示弹窗
        showPopupWindow(view);
    }

    public interface ChannelLikePopupClickListener {
        void selectReaction(ReactionsLayoutInBubble.VisibleReaction visibleReaction, boolean bigEmoji);
    }
}
