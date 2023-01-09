package teleblock.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.RFrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpSelectorChatBinding;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import teleblock.model.PermissChatEntity;

/**
 * Time:2022/8/8
 * Author:Perry
 * Description：选中的会话列表适配器
 */
public class SelectorChatAdp extends BaseQuickAdapter<PermissChatEntity, BaseViewHolder> {

    public SelectorChatAdp() {
        super(R.layout.adp_selector_chat);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, PermissChatEntity data) {
        AdpSelectorChatBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpSelectorChatBinding::bind);

        //头像
        setHeaderImg(binding.flAvatar, data);
        //昵称
        binding.tvName.setText(data.getChatName());
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
}
