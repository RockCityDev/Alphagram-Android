package teleblock.player;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import org.telegram.messenger.R;

/**
 * Created by LSD on 2021/2/24.
 * Desc
 */
public class IVideoPlayer extends StandardGSYVideoPlayer {
    ImageView mCoverImage;
    boolean blankClick = false;

    public IVideoPlayer(Context context) {
        super(context);
    }

    public IVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.ivideo_layout;
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = (ImageView) findViewById(R.id.thumbImage);
        if (mThumbImageViewLayout != null && (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }
    }

    public void loadCoverImage(String path) {
        mThumbImageViewLayout.setVisibility(VISIBLE);
        int viewW = ScreenUtils.getScreenWidth();
        int viewH;

        int sw = getCurrentVideoWidth();
        int sh = getCurrentVideoHeight();
        if (sw != 0) {
            viewH = viewW * sh / sw;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCoverImage.getLayoutParams();
            params.width = viewW;
            params.height = viewH;
            mCoverImage.setLayoutParams(params);
        }
        Glide.with(mContext).load(path).into(mCoverImage);
    }

    @Override
    protected void updateStartImage() {
        if (mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PAUSE) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_error_selector);
            } else {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
            }
        }
    }

    @Override
    public int getShrinkImageRes() {
        return R.drawable.ic_paly_small;
    }

    @Override
    public int getEnlargeImageRes() {
        return R.drawable.ic_play_full;
    }

    @Override
    public void onSurfaceAvailable(Surface surface) {
        super.onSurfaceAvailable(surface);
        if (GSYVideoType.getRenderType() != GSYVideoType.TEXTURE) {
            if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
                mThumbImageViewLayout.setVisibility(INVISIBLE);
            }
        }
    }

    public void hideAllView() {
        hideAllWidget();
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
        hideAllView();
        setViewShowState(mLoadingProgressBar, GONE);
    }

    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        setViewShowState(mBottomProgressBar, INVISIBLE);
        BarUtils.setStatusBarLightMode((Activity) mContext, true);
    }

    @Override
    protected void changeUiToClear() {
        super.changeUiToClear();
        setViewShowState(mBottomProgressBar, INVISIBLE);
        BarUtils.setStatusBarLightMode((Activity) mContext, true);
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        if (!blankClick) {
            hideAllWidget();
        } else {
            setViewShowState(mProgressBar, VISIBLE);
            BarUtils.setStatusBarLightMode((Activity) mContext, false);
        }
        blankClick = false;
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        BarUtils.setStatusBarLightMode((Activity) mContext, false);
    }

    public void onClickBlankShowUI() {
        blankClick = true;
    }

    public void onAutoCompleteShowUI() {
        ImageView imageView = (ImageView) mStartButton;
        imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
        setViewShowState(mProgressBar, VISIBLE);
    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        setViewShowState(mStartButton, INVISIBLE);
        hideAllView();
    }

    @Override
    protected void changeUiToPreparingShow() {
        super.changeUiToPreparingShow();
        setViewShowState(mProgressBar, INVISIBLE);
        setViewShowState(mTopContainer, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        hideAllView();
    }
}
