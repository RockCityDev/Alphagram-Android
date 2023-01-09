package teleblock.ui.adapter;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.RFrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.AdpPermissionsChatBinding;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import teleblock.model.PermissChatEntity;
import teleblock.util.TGLog;

/**
 * Time:2022/8/8
 * Author:Perry
 * Description：黑名单/白名单会话列表
 */
public class PermissionsChatAdp extends BaseQuickAdapter<PermissChatEntity, BaseViewHolder> {

    public PermissionsChatAdp() {
        super(R.layout.adp_permissions_chat);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, PermissChatEntity data) {
        AdpPermissionsChatBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, AdpPermissionsChatBinding::bind);
        binding.tvRemove.setText(LocaleController.getString("act_permissions_chat_remove", R.string.act_permissions_chat_remove));
        binding.tvName.setText(data.getChatName());
        setHeaderImg(binding.flAvatar, data);

        if (data.isUserChat()) {//是用户会话的话，显示上线时间
            binding.tvStatus.setText(LocaleController.formatUserStatus(UserConfig.selectedAccount, data.getUser()));
        } else {
            getChatFull(data.getChat(), binding.tvStatus);
        }
    }

    private void setHeaderImg(RFrameLayout flAvatar, PermissChatEntity data) {
        int value = flAvatar.getLayoutParams().height;

        AvatarDrawable avatarDrawable = new AvatarDrawable();
        BackupImageView avatarImageView = new BackupImageView(getContext());
        avatarImageView.setRoundRadius(AndroidUtilities.dp(value));
        if (data.isUserChat()) {
            avatarDrawable.setInfo(data.getUser());
            avatarImageView.setImage(ImageLocation.getForUser(data.getUser(), ImageLocation.TYPE_SMALL), value + "_" + value, avatarDrawable, data.getUser());
        } else {
            avatarDrawable.setInfo(data.getChat());
            avatarImageView.setImage(ImageLocation.getForChat(data.getChat(), ImageLocation.TYPE_SMALL), value + "_" + value, avatarDrawable, data.getChat());
        }
        flAvatar.removeAllViews();
        flAvatar.addView(avatarImageView);
    }

    /**
     * 群和频道人数
     * @param chat
     * @param tvStatus
     */
    private void getChatFull(TLRPC.Chat chat, TextView tvStatus) {
        String chatPopeoNumFormat = ChatObject.isChannel(chat) && !chat.megagroup
                ? LocaleController.getString("act_permissions_chat_channel", R.string.act_permissions_chat_channel)
                : LocaleController.getString("act_permissions_chat_group", R.string.act_permissions_chat_group);

        TLRPC.ChatFull chatFull = MessagesController.getInstance(UserConfig.selectedAccount).getChatFull(chat.id);
        if (chatFull != null) {
            tvStatus.setText(String.format(chatPopeoNumFormat, chatFull.participants_count));
            return;
        }

        TLObject request;
        if (ChatObject.isChannel(chat)) {
            TLRPC.TL_channels_getFullChannel req = new TLRPC.TL_channels_getFullChannel();
            req.channel = MessagesController.getInputChannel(chat);
            request = req;
        } else {
            TLRPC.TL_messages_getFullChat req = new TLRPC.TL_messages_getFullChat();
            req.chat_id = chat.id;
            request = req;
        }

        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(request, (response, error) -> {
            if (error == null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.TL_messages_chatFull res = (TLRPC.TL_messages_chatFull) response;

                    MessagesStorage.getInstance(UserConfig.selectedAccount).updateChatInfo(res.full_chat, false);
                    MessagesController.getInstance(UserConfig.selectedAccount).putChatFull(res.full_chat);

                    if (res.full_chat != null) {
                        tvStatus.setText(String.format(chatPopeoNumFormat, res.full_chat.participants_count));
                    }
                });
            }
        });
    }

}
