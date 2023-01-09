package teleblock.ui.adapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import teleblock.manager.DialogManager;
import teleblock.model.ChatInfoEntity;

public class ChatListAdapter extends BaseQuickAdapter<ChatInfoEntity, ChatListAdapter.MyViewHolder> {

    public ChatListAdapter() {
        super(R.layout.item_chat_list);
    }


    @Override
    protected void convert(@NonNull MyViewHolder helper, ChatInfoEntity chatInfoEntity) {
        chatInfoEntity = DialogManager.getInstance(UserConfig.selectedAccount).loadChatInfos.get(chatInfoEntity.chatId);
        TLRPC.Chat chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(chatInfoEntity.chatId);
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(chat);
            int value = helper.flAvatar.getLayoutParams().height;
            helper.avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), value + "_" + value, avatarDrawable, chat);
            boolean isChannel = ChatObject.isChannel(chat) && !chat.megagroup;
            helper.tvTitle.setText(chat.title + "(" + (isChannel ? "频道" : "群组") + ")");
            helper.tvLoadAdmin.setText("我管理的群组和频道：" + chatInfoEntity.loadAdmin);
            if (isChannel) {
                helper.tvLoadMember.setVisibility(View.GONE);
            } else {
                helper.tvLoadMember.setVisibility(View.VISIBLE);
                helper.tvLoadMember.setText("我参与的群组：" + chatInfoEntity.loadMember);
            }
        }
    }

    public class MyViewHolder extends BaseViewHolder {
        private BackupImageView avatarImageView;
        private FrameLayout flAvatar;
        private TextView tvTitle;
        private TextView tvLoadAdmin;
        private TextView tvLoadMember;

        public MyViewHolder(View view) {
            super(view);
            flAvatar = view.findViewById(R.id.fl_avatar);
            tvTitle = view.findViewById(R.id.tv_title);
            tvLoadAdmin = view.findViewById(R.id.tv_loadAdmin);
            tvLoadMember = view.findViewById(R.id.tv_loadMember);

            avatarImageView = new BackupImageView(getContext());
            int value = flAvatar.getLayoutParams().height;
            avatarImageView.setRoundRadius(AndroidUtilities.dp(value / 2f));
            flAvatar.removeAllViews();
            flAvatar.addView(avatarImageView);
        }
    }
}
