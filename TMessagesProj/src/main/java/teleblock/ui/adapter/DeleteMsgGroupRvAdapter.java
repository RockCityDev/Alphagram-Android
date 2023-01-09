package teleblock.ui.adapter;

import android.content.Context;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import java.util.List;

import teleblock.model.DeleteMessageEntity;
import teleblock.video.KKVideoDataManager;

/**
 * 删除消息分组
 */
public class DeleteMsgGroupRvAdapter extends BaseDelegateMultiAdapter<DeleteMessageEntity, BaseViewHolder> implements LoadMoreModule {
    Context context;

    public DeleteMsgGroupRvAdapter(Context context) {
        this.context = context;
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<DeleteMessageEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends DeleteMessageEntity> data, int position) {
                return 0;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(0, R.layout.view_delete_msg_group_item);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);

        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        //头像
        avatar_frame.removeAllViews();
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            baseViewHolder.setText(R.id.tv_title, chat.title);
            avatar_frame.addView(avatarImageView);
            return;
        }

        //名称
        TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
        if (user != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(user);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
            }
            baseViewHolder.setText(R.id.tv_title, ContactsController.formatName(user.first_name, user.last_name));
            avatar_frame.addView(avatarImageView);
        }
    }
}
