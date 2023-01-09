
package teleblock.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.blankj.utilcode.util.BarUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import java.util.HashMap;

import teleblock.model.VideoStaggeredEntity;
import teleblock.ui.fragment.VideoStaggeredFragment;
import teleblock.util.ColorUtil;
import teleblock.util.DrawableColorChange;
import teleblock.util.EventUtil;
import teleblock.video.KKVideoDataManager;


/**
 * Created by LSD on 2021/5/3.
 * Desc 视频瀑布流载体Activity
 */
public class VideoStaggeredActivity extends BaseActivity {
    VideoStaggeredEntity entity;
    TextView tvGroupName;
    FrameLayout groupAvstarLayout;

    AvatarDrawable avatarDrawable = new AvatarDrawable();
    BackupImageView avatarImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_staggered);
        //initTitleBarStyle();

        getExtras();
        initView();
        loadChat();
    }

    private void initTitleBarStyle() {
        //背景
        int color = Theme.getColor(Theme.key_actionBarDefault);
        findViewById(R.id.layout_title).setBackgroundColor(color);
        BarUtils.setStatusBarColor(mActivity, ColorUtil.getDarkerColor(color));

        //标题
        TextView tvTitle = findViewById(R.id.tv_group_name);
        tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));

        //返回
        ImageView ivBack = findViewById(R.id.iv_back);
        DrawableColorChange drawableColorChange = new DrawableColorChange(mActivity);
        ivBack.setImageDrawable(drawableColorChange.changeColorByColor(R.drawable.calls_back, Theme.getColor(Theme.key_actionBarDefaultIcon)));
    }

    private void getExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            entity = (VideoStaggeredEntity) bundle.getSerializable("entity");
        }
    }

    private void initView() {
        avatarImageView = new BackupImageView(this);
        findViewById(R.id.iv_back).setOnClickListener(view -> {
            finish();
        });
        findViewById(R.id.iv_to_chat).setOnClickListener(view -> {
            startActivity(new Intent(mActivity, TGViewPresentActivity.class).putExtra("type", "toChat").putExtra("chatId", Math.abs(entity.dialogId)));
        });
        tvGroupName = findViewById(R.id.tv_group_name);
        groupAvstarLayout = findViewById(R.id.group_avstar_layout);

        Fragment fragment = VideoStaggeredFragment.instance(entity);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.feed_frame, fragment);
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

    private void loadChat() {
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.dialogId);
        if (chat == null) return;
        tvGroupName.setText(chat.title);

        avatarDrawable.setInfo(chat);
        if (avatarImageView != null) {
            avatarImageView.setRoundRadius(AndroidUtilities.dp(50));
            avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, chat);
            groupAvstarLayout.addView(avatarImageView);
        }
    }
}
