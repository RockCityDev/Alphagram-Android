package teleblock.ui.popup;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.PopupHomeOptionBinding;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.ChannelCreateActivity;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.NewContactActivity;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AlphaConfig;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.ScaleConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.ui.activity.RelatedMeSettingAct;

/**
 * 首页右上角菜单
 */
public class HomeOptionPopup extends BasePopupWindow implements View.OnClickListener {

    private DialogsActivity fragment;
    private PopupHomeOptionBinding binding;

    public HomeOptionPopup(DialogsActivity fragment) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        binding = PopupHomeOptionBinding.inflate(LayoutInflater.from(fragment.getParentActivity()));
        setContentView(binding.getRoot());
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onViewCreated(@NonNull View contentView) {
        super.onViewCreated(contentView);
        initView();
        initData();
    }

    private void initView() {
        binding.parent.setOnClickListener(this);
        binding.tvOptionQrcode.setOnClickListener(this);
        binding.tvOptionAddContact.setOnClickListener(this);
        binding.tvOptionNewGroup.setOnClickListener(this);
        binding.tvOptionNewChannel.setOnClickListener(this);
        binding.tvOptionNewSecretChat.setOnClickListener(this);
        binding.tvOptionNewMessage.setOnClickListener(this);
        binding.tvOptionRelatedme.setOnClickListener(this);
        binding.tvOptionQrcode.setText(LocaleController.getString("homeoption_pop_qrcode", R.string.homeoption_pop_qrcode));
        binding.tvOptionAddContact.setText(LocaleController.getString("homeoption_pop_addfriend", R.string.homeoption_pop_addfriend));
        binding.tvOptionNewGroup.setText(LocaleController.getString("homeoption_pop_creategroup", R.string.homeoption_pop_creategroup));
        binding.tvOptionNewChannel.setText(LocaleController.getString("homeoption_pop_createchannel", R.string.homeoption_pop_createchannel));
        binding.tvOptionNewSecretChat.setText(LocaleController.getString("homeoption_pop_encryptedchat", R.string.homeoption_pop_encryptedchat));
        binding.tvOptionNewMessage.setText(LocaleController.getString("homeoption_pop_createmsg", R.string.homeoption_pop_createmsg));
        binding.tvOptionRelatedme.setText(LocaleController.getString("homeoption_pop_relatedme", R.string.homeoption_pop_relatedme));
    }

    private void initData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.parent:
                dismiss();
                break;
            case R.id.tv_option_new_message:
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                fragment.presentFragment(new ContactsActivity(args));
                dismiss();
                break;
            case R.id.tv_option_qrcode:
                EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_CAMERA_SCAN));
                dismiss();
                break;
            case R.id.tv_option_add_contact:
                fragment.presentFragment(new NewContactActivity());
                dismiss();
                break;
            case R.id.tv_option_new_group:
                fragment.presentFragment(new GroupCreateActivity(new Bundle()), false);
                dismiss();
                break;
            case R.id.tv_option_new_channel:
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                if (!BuildVars.DEBUG_VERSION && preferences.getBoolean("channel_intro", false)) {
                    args = new Bundle();
                    args.putInt("step", 0);
                    fragment.presentFragment(new ChannelCreateActivity(args));
                } else {
                    fragment.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANNEL_CREATE));
                    preferences.edit().putBoolean("channel_intro", true).commit();
                }
                dismiss();
                break;
            case R.id.tv_option_new_secret_chat:
                args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("createSecretChat", true);
                args.putBoolean("allowBots", false);
                args.putBoolean("allowSelf", false);
                fragment.presentFragment(new ContactsActivity(args), false);
                dismiss();
                break;

            case R.id.tv_option_relatedme:
                fragment.presentFragment(new RelatedMeSettingAct());
                dismiss();
                break;
        }
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return AnimationHelper.asAnimation()
                .withScale(ScaleConfig.TOP_TO_BOTTOM.duration(200))
                .withAlpha(AlphaConfig.IN.duration(200))
                .toShow();
    }
}