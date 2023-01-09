package teleblock.ui.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import java.io.IOException;

/**
 * Created by LSD on 2021/9/16.
 * Desc
 */
public class VideoBackgroundView extends FrameLayout {
    Context context;
    SurfaceView surfaceView;
    MediaPlayer player;

    boolean isPause;
    String resPath;
    int seekTo = 0;

    public VideoBackgroundView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_video_surface_play, this, true);

        player = new MediaPlayer();
        surfaceView = findViewById(R.id.surfaceView);
    }

    public String getResPath() {
        return resPath;
    }

    public boolean isStarted() {
        return !resPath.isEmpty();
    }

    public void start(String path) {
        if (TextUtils.isEmpty(path)) return;
        resPath = path;
        try {
            player.reset();
            player.setDataSource(context, Uri.parse(path));
            SurfaceHolder holder = surfaceView.getHolder();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    player.setDisplay(holder);
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });
            player.setLooping(true);
            //player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            player.prepare();
            player.setOnPreparedListener(mp -> {
                mp.setVolume(0f, 0f);//静音
                setSurfaceViewLayoutParams();
                if (seekTo > 0) {
                    //player.seekTo(seekTo);
                }
                player.start();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSurfaceViewLayoutParams() {
        int viewW = ScreenUtils.getScreenWidth();
        int viewH = ScreenUtils.getScreenHeight();
        int sw = player.getVideoWidth();
        int sh = player.getVideoHeight();
        float sRatio = viewW * 1.0f / viewH;
        float ratio = sw * 1.0f / sh;
        if (sRatio >= ratio) {
            viewH = viewW * sh / sw;
        } else {
            viewW = viewH * sw / sh;
        }
        LayoutParams params = (LayoutParams) surfaceView.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = viewW;
        params.height = viewH;
        surfaceView.setLayoutParams(params);
    }

    public void pause() {
        if (!isPause) {
            isPause = true;
            if (player != null){
                seekTo = player.getCurrentPosition();
                player.pause();
            }
        }
    }

    public void resume() {
        if (isPause) {
            isPause = false;
            AndroidUtilities.runOnUIThread(() -> {
                if (player != null) player.start();
            }, 40);
        }
    }

    public boolean isPause() {
        return isPause;
    }

    public void reset() {
        destroy();
        isPause = false;
        seekTo = 0;
        resPath = "";
        player = new MediaPlayer();
    }

    public void destroy() {
        try {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
        } catch (Exception e) {
        }
    }
}
