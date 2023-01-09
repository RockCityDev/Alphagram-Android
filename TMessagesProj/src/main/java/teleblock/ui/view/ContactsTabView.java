package teleblock.ui.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewTabContactsBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;



/**
 * 创建日期：2022/4/19
 * 描述：联系人界面
 */
public class ContactsTabView extends FrameLayout {

    private DialogsActivity mBaseFragment;
    private ViewTabContactsBinding binding;

    private BackupImageView avatarImageView;
    private ContactsActivity contactsActivity;

    public ContactsTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        this.mBaseFragment = dialogsActivity;
        initView();
        setVisibility(GONE);
    }

    private void initView() {
        setOnClickListener(v -> {});
        binding = ViewTabContactsBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.getRoot().setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }

    public void initData() {
        if (contactsActivity == null || contactsActivity.isFinished) {
            Bundle args = new Bundle();
            args.putBoolean("needPhonebook", true);
            args.putString("entry", "home");
            contactsActivity = new ContactsActivity(args);
            contactsActivity.onFragmentCreate();
            contactsActivity.setParentFragment(mBaseFragment);
            ActionBar actionBar = contactsActivity.getActionBar();
            actionBar.setTitle(LocaleController.getString("home_contact", R.string.home_contact));
            binding.flActionbar.removeAllViews();
            binding.flActionbar.addView(actionBar);
            binding.flContent.removeAllViews();
            binding.flContent.addView(contactsActivity.getFragmentView());
        }
    }

    public void updateStyle() {
        contactsActivity = null;
        binding.getRoot().setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        initData();
    }
}