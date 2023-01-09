package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityChannelFeedBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import teleblock.model.ChannelFeedEntity;
import teleblock.ui.fragment.ChannelFeedFragment;
import teleblock.util.DrawableColorChange;
import teleblock.video.KKVideoDataManager;


/**
 * Created by LSD on 2021/5/3.
 * Desc频道聚合载体Activity
 */
public class ChannelFeedActivity extends BaseFragment {

    private ActivityChannelFeedBinding mActivityChannelFeedBinding;
    private ChannelFeedEntity entity;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private BackupImageView avatarImageView;

    private FragmentActivity fragmentActivity;

    public ChannelFeedActivity(Bundle args, FragmentActivity fragmentActivity) {
        super(args);
        this.fragmentActivity = fragmentActivity;
    }

    @Override
    public boolean onFragmentCreate() {
        if (getArguments() != null) {
            entity = (ChannelFeedEntity) arguments.getSerializable("entity");
        }
        return true;
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        setNavigationBarColor(Color.WHITE,true);
        mActivityChannelFeedBinding = ActivityChannelFeedBinding.inflate(LayoutInflater.from(context));
        fragmentView = mActivityChannelFeedBinding.getRoot();
        initView();
        initStyle();
        return fragmentView;
    }

    private void initView() {
        mActivityChannelFeedBinding.ll.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);
        avatarImageView = new BackupImageView(getParentActivity());

        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.chatId);
        if (chat == null) return;
        mActivityChannelFeedBinding.tvTitle.setText(chat.title);
        avatarDrawable.setInfo(chat);

        mActivityChannelFeedBinding.ivBack.setOnClickListener(view -> {
            finishFragment();
        });

        if (avatarImageView != null) {
            avatarImageView.setRoundRadius(AndroidUtilities.dp(50));
            avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, chat);
            mActivityChannelFeedBinding.groupAvstarLayout.addView(avatarImageView);
        }

        ChannelFeedFragment channelPage = new ChannelFeedFragment();
        channelPage.setmBaseFragment(this);
        channelPage.setEntity(entity);
        fragmentActivity.getSupportFragmentManager().beginTransaction().replace(R.id.feed_frame, channelPage).commit();
    }

    private void initStyle() {
        //背景
        int color = Theme.getColor(Theme.key_actionBarDefault);
        mActivityChannelFeedBinding.getRoot().setBackgroundColor(color);
        mActivityChannelFeedBinding.layoutTitle.setBackgroundColor(color);

        //标题
        mActivityChannelFeedBinding.tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));

        //返回
        DrawableColorChange drawableColorChange = new DrawableColorChange(getContext());
        mActivityChannelFeedBinding.ivBack.setImageDrawable(drawableColorChange.changeColorByColor(R.drawable.calls_back, Theme.getColor(Theme.key_actionBarDefaultIcon)));
    }

}
