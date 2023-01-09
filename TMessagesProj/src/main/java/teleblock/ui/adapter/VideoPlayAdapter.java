package teleblock.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.R;

import java.util.List;

import teleblock.player.IVideoPlayer;


public class VideoPlayAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    Context context;
    OrientationUtils orientationUtils;
    View.OnClickListener clickListener;
    ViewPager2 viewPager2;
    String from;
    BaseViewHolder tempBaseViewHolder;

    public VideoPlayAdapter(Context context, ViewPager2 viewPager2, View.OnClickListener clickListener) {
        super(R.layout.layout_video_play_item);
        this.context = context;
        this.clickListener = clickListener;
        this.viewPager2 = viewPager2;
    }

    public void setData(List<String> list, String from) {
        this.from = from;
        setList(list);
    }

    public void onDestroy() {
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
        }
    }

    public boolean onBackPressed() {
        if (orientationUtils != null && orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            resolveScreen(tempBaseViewHolder);
            return true;
        }
        return false;
    }

    private void showSave(BaseViewHolder baseViewHolder, boolean forceGone) {
        if (baseViewHolder == null) return;
        IVideoPlayer iVideoPlayer = baseViewHolder.findView(R.id.i_player);
        LinearLayout full_save_to_gallery = iVideoPlayer.findViewById(R.id.full_save_to_gallery);
        boolean gone = forceGone || (!TextUtils.isEmpty(from) && from.equals("show_album"));
        if (gone) {
            full_save_to_gallery.setVisibility(View.GONE);
        } else {
            full_save_to_gallery.setVisibility(View.VISIBLE);
        }
    }

    private void resolveScreen(BaseViewHolder baseViewHolder) {
        orientationUtils.resolveByClick();
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            viewPager2.setUserInputEnabled(false);//横屏禁止滑动
            showSave(baseViewHolder, true);
        } else {
            viewPager2.setUserInputEnabled(true);
            showSave(baseViewHolder, false);
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String entity) {
        IVideoPlayer iVideoPlayer = baseViewHolder.findView(R.id.i_player);
        if (orientationUtils == null) {
            orientationUtils = new OrientationUtils((Activity) context, iVideoPlayer);
        }
        iVideoPlayer.getFullscreenButton().setOnClickListener(view -> {//横屏
            tempBaseViewHolder = baseViewHolder;
            resolveScreen(baseViewHolder);
        });
        LinearLayout full_save_to_gallery = iVideoPlayer.findViewById(R.id.full_save_to_gallery);
        full_save_to_gallery.setOnClickListener(clickListener);
        showSave(baseViewHolder, false);
        iVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        iVideoPlayer.getBackButton().setOnClickListener(clickListener);
        iVideoPlayer.findViewById(R.id.iv_delete).setOnClickListener(clickListener);
        iVideoPlayer.findViewById(R.id.iv_share).setOnClickListener(clickListener);
        GSYVideoOptionBuilder gsyVideoOptionBuilder = new GSYVideoOptionBuilder();
        gsyVideoOptionBuilder
                .setIsTouchWiget(false)
                .setUrl("file://" + entity)
                .setLooping(true)
                .setPlayPosition(baseViewHolder.getAdapterPosition())
                .setRotateViewAuto(false)
                .setLockLand(true)
                .setShowFullAnimation(true)
                .setNeedLockFull(true)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onClickBlank(String url, Object... objects) {
                        IVideoPlayer player = (IVideoPlayer) objects[1];
                        player.onClickBlankShowUI();
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        IVideoPlayer player = (IVideoPlayer) objects[1];
                        player.onAutoCompleteShowUI();
                    }
                }).build(iVideoPlayer);
        iVideoPlayer.hideAllView();
        iVideoPlayer.loadCoverImage(entity);
    }
}
