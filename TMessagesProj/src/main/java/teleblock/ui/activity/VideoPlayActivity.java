package teleblock.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.BarUtils;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Components.Bulletin;

import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.VideoSaveGalleryEvent;
import teleblock.model.MiddleData;
import teleblock.player.IVideoPlayer;
import teleblock.ui.adapter.VideoPlayAdapter;
import teleblock.util.ShareUtil;
import teleblock.util.SystemUtil;
import teleblock.video.KKVideoDataManager;


/**
 * Created by LSD on 2021/3/9.
 * Desc  这个页面只支持播放已经存在的视频
 */
public class VideoPlayActivity extends BaseActivity {
    ViewPager2 viewPager2;
    VideoPlayAdapter videoPlayAdapter;
    List<String> list;
    int position;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        BarUtils.setStatusBarColor(mActivity, Color.parseColor("#000000"));
        BarUtils.setStatusBarLightMode(mActivity,true);
        setContentView(R.layout.activity_video_play);

        initView();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (videoPlayAdapter != null) videoPlayAdapter.onDestroy();
        MiddleData.getInstance().playList = null;
    }

    private void initView() {
        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        //viewPager2.setOffscreenPageLimit(2);
        viewPager2.setAdapter(videoPlayAdapter = new VideoPlayAdapter(this, viewPager2, view -> {
            int position = viewPager2.getCurrentItem();
            String path = videoPlayAdapter.getItem(position);
            switch (view.getId()) {
                case R.id.back:
                    onBackPressed();
                    break;
                case R.id.iv_delete:
                    String fileName = SystemUtil.getFileName(path);
                    KKVideoDataManager.getInstance().removeLocalFile(fileName);
                    SystemUtil.deleteFile(path);
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
                    Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case R.id.iv_share:
                    ShareUtil.shareVideo2(mActivity, path);
                    break;
                case R.id.full_save_to_gallery:
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.VIDEO_SAVE_GALLERY, new VideoSaveGalleryEvent(path)));
                    MediaController.saveFile(path, ApplicationLoader.applicationContext, 1, null, null, () -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SAVE_GALLERY));
                    break;
            }
        }));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //自动播放
                int playPosition = GSYVideoManager.instance().getPlayPosition();
                if (playPosition >= 0 && playPosition != position) {
                    play(position);
                }
            }
        });
    }

    private void initData() {
        list = MiddleData.getInstance().playList;
        if (list == null) finish();
        position = getIntent().getIntExtra("position", 0);
        from = getIntent().getStringExtra("from");

        videoPlayAdapter.setData(list, from);
        viewPager2.setCurrentItem(position, false);
        viewPager2.post(() -> {
            play(position);
        });
    }

    private void play(int position) {
        //开始新播放
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
            IVideoPlayer iVideoPlayer = baseViewHolder.findView(R.id.i_player);
            iVideoPlayer.startPlayLogic();
        }
    }

    @Override
    public void onBackPressed() {
        if (videoPlayAdapter.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
