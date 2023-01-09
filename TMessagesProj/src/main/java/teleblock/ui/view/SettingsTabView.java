package teleblock.ui.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ViewTabSettingsBinding;
import org.telegram.messenger.databinding.ViewTabSettingsBinding;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.ProfileActivity;

/**
 * 创建日期：2022/4/19
 * 描述：设置界面
 */
public class SettingsTabView extends FrameLayout {

    private ViewTabSettingsBinding binding;
    private DialogsActivity parentFragment;
    public ProfileActivity profileActivity;

    public SettingsTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        this.parentFragment = dialogsActivity;
        initView();
        setVisibility(GONE);
    }

    private void initView() {
        setOnClickListener(v -> {
        });
        binding = ViewTabSettingsBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.getRoot().setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }

    public void initData() {
        if (profileActivity == null || profileActivity.isFinished) {
            Bundle args = new Bundle();
            args.putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
            //args.putBoolean("expandPhoto", true);
            args.putString("entry", "home");
            profileActivity = new ProfileActivity(args);
            profileActivity.onFragmentCreate();
            profileActivity.setParentFragment(parentFragment);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            binding.flContent.removeAllViews();
            binding.flContent.addView(profileActivity.getFragmentView(), layoutParams);
        }

    }

    public void updateStyle() {
        profileActivity = null;
        binding.getRoot().setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        initData();
    }
}