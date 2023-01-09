package teleblock.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.AdpTgSearchResultBinding;
import org.telegram.tgnet.TLRPC;

import teleblock.manager.ChatManager;

/**
 * Time:2022/8/16
 * Author:Perry
 * Description：tg搜索结果适配器
 */
public class TgSearchResultAdp extends BaseQuickAdapter<TLRPC.Chat, BaseViewHolder> {

    public TgSearchResultAdp() {
        super(R.layout.adp_tg_search_result);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, TLRPC.Chat chat) {
        AdpTgSearchResultBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpTgSearchResultBinding::bind);
        //头像
        ChatManager.getInstance(UserConfig.selectedAccount).addTgAvatarView(getContext(), chat, binding.flAvatar, true);
        //名称
        binding.tvName.setText(chat.title);

        String stringFormat;
        if (ChatObject.isChannel(chat) && !chat.megagroup) {
            stringFormat = LocaleController.getString("channel_subscription_num", R.string.channel_subscription_num);
        } else {
            stringFormat = LocaleController.getString("group_subscription_num", R.string.group_subscription_num);
        }
        binding.tvSubscriptionNum.setText(String.format(stringFormat, chat.participants_count));
    }
}
