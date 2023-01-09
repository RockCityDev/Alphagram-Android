package teleblock.ui.activity;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.VideoPlayer;

import java.net.URLEncoder;
import java.util.ArrayList;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.CollectChangeEvent;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageCollectManager;
import teleblock.file.KKFileMessageManager;
import teleblock.ui.dialog.SpeedSetDialog;
import teleblock.util.SystemUtil;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDataManager;

/**
 * Created by LSD on 2021/5/17.
 * Desc
 */
public class FullScreenPlayActivity extends BaseActivity {
    View main_container;
    ImageView iv_close;
    TextView tv_video_text;
    FrameLayout avatar_frame;
    TextView tv_group_name;
    FrameLayout video_container;
    TextureView video_view;
    FrameLayout video_cover;
    ProgressBar loading_progress;
    SeekBar seek_progress;
    ImageView iv_play;
    TextView tv_pro;
    ImageView iv_video_collect;
    ImageView iv_video_download;
    TextView tv_speed;
    LinearLayout top_container;
    LinearLayout bottom_container;

    public static MessageObject currentMessageObject;
    public static KKFileDownloadStatus downloadStatus;
    public static long seekTo;

    VideoPlayer videoPlayer;
    boolean seekBarInTouch = false;
    boolean collect = false;
    Handler handler = new Handler();
    Runnable runnable = () -> {
        if (seekBarInTouch) return;
        showControl(false);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen_play);

        initView();
        initPlayer();
        playItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.releasePlayer(true);
        }
    }

    private void initPlayer() {
        videoPlayer = new VideoPlayer();
        videoPlayer.setTextureView(video_view);
        videoPlayer.setLooping(true);
        videoPlayer.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                updateViewState(playbackState);
            }

            @Override
            public void onError(VideoPlayer player, Exception e) {
            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            }

            @Override
            public void onRenderedFirstFrame() {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                updatePlayProgress();
            }

            @Override
            public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {

            }

            @Override
            public void onSeekStarted(AnalyticsListener.EventTime eventTime) {
            }

            @Override
            public void onSeekFinished(AnalyticsListener.EventTime eventTime) {
            }
        });
    }

    private void initView() {
        (iv_close = findViewById(R.id.iv_close)).setOnClickListener(view -> finish());
        findViewById(R.id.main_container).setOnClickListener(view -> {
            handler.removeCallbacks(runnable);
            if (top_container.getVisibility() == View.VISIBLE) {
                showControl(false);
            } else {
                showControl(true);
            }
        });
        tv_video_text = findViewById(R.id.tv_video_text);
        avatar_frame = findViewById(R.id.avatar_frame);
        tv_group_name = findViewById(R.id.tv_group_name);
        video_container = findViewById(R.id.video_container);
        video_view = findViewById(R.id.video_view);
        video_cover = findViewById(R.id.video_cover);
        loading_progress = findViewById(R.id.loading_progress);
        seek_progress = findViewById(R.id.seek_progress);
        iv_play = findViewById(R.id.iv_play);
        tv_pro = findViewById(R.id.tv_pro);
        iv_video_collect = findViewById(R.id.iv_video_collect);
        iv_video_download = findViewById(R.id.iv_video_download);
        tv_speed = findViewById(R.id.tv_speed);
        top_container = findViewById(R.id.top_container);
        bottom_container = findViewById(R.id.bottom_container);
        video_container.setKeepScreenOn(true);

        //描述
        String text = "";
        if (currentMessageObject != null && currentMessageObject.caption != null) {
            text = currentMessageObject.caption.toString();
        }
        tv_video_text.setText(text);

        //群名称
        String from = KKFileMessage.getFromName(currentMessageObject);
        if (!TextUtils.isEmpty(from)) {
            tv_group_name.setVisibility(View.VISIBLE);
            tv_group_name.setText("@" + from);
        } else {
            tv_group_name.setVisibility(View.GONE);
        }

        //群头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(currentMessageObject.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(mActivity);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.addView(avatarImageView);
        }

        //视频比例
        TLRPC.Document document = currentMessageObject.getDocument();
        if (document != null) {
            //视频区域大小
            ArrayList<TLRPC.DocumentAttribute> attributes = document.attributes;
            int viewW = ScreenUtils.getScreenWidth();
            int viewH = ScreenUtils.getScreenHeight();
            int sw;
            int sh;
            if (attributes != null && attributes.size() > 0) {
                sw = attributes.get(0).w;
                sh = attributes.get(0).h;
                if (sw != 0 && sh != 0) {
                    float ratio = sw * 1.0f / sh;
                    float sRatio = viewW * 1.0f / viewH;
                    if (sRatio >= ratio) {
                        viewW = viewH * sw / sh;
                    } else {
                        viewH = viewW * sh / sw;
                    }
                }
            }
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video_container.getLayoutParams();
            params.width = viewW;
            params.height = viewH;
            video_container.setLayoutParams(params);
        }

        //视频封面
        BackupImageView ivThumb = new BackupImageView(mActivity);
        if (document != null && document.thumbs != null) {
            TLRPC.PhotoSize bigthumb = FileLoader.getClosestPhotoSizeWithSize(currentMessageObject.getDocument().thumbs, 320);
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(currentMessageObject.getDocument().thumbs, 40);
            if (thumb == bigthumb) {
                bigthumb = null;
            }
            //ivThumb.setRoundRadius(AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
            ivThumb.getImageReceiver().setNeedsQualityThumb(bigthumb == null);
            ivThumb.getImageReceiver().setShouldGenerateQualityThumb(bigthumb == null);
            ivThumb.setImage(ImageLocation.getForDocument(bigthumb, currentMessageObject.getDocument()), "480_320", ImageLocation.getForDocument(thumb, currentMessageObject.getDocument()), "480_320_b", null, 0, 1, currentMessageObject);
            video_cover.addView(ivThumb);
        }


        //播放
        iv_play.setOnClickListener(view -> {
            boolean playing = videoPlayer.isPlaying();
            updatePlayState(!playing);
        });

        //进度
        seek_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarInTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarInTouch = false;
                showControl(true);
                if (videoPlayer != null) {
                    int progress = seekBar.getProgress();
                    long duration = videoPlayer.getDuration();
                    long seekPosition = duration * progress / 100;
                    videoPlayer.seekTo(seekPosition);
                    tv_pro.setText(SystemUtil.timeTransfer((int) seekPosition / 1000) + " / " + SystemUtil.timeTransfer((int) (duration / 1000.0f)));
                }
            }
        });

        //收藏
        boolean tempCollect = KKFileMessageCollectManager.getInstance().getCollectStatus(currentMessageObject);
        setCollect(tempCollect);
        iv_video_collect.setOnClickListener(view -> {
            if (collect) {
                KKFileMessageCollectManager.getInstance().removeCollect(currentMessageObject);
            } else {
                KKFileMessageCollectManager.getInstance().collectMessage(currentMessageObject);
            }
            setCollect(!collect);
        });

        //下载
        iv_video_download.setOnClickListener(view -> {
            KKFileMessageManager.getInstance().manualStartDownloadVideo(currentMessageObject);//手动开始下载
            startActivity(new Intent(mActivity, MyMixActivity.class).putExtra("currentItem", 1).putExtra("target", "downloading"));
        });

        tv_speed.setOnClickListener(view -> {
            new SpeedSetDialog(mActivity, speed -> {
                if (speed == 2.0f) {
                    tv_speed.setText(LocaleController.getString("vw_video_speed_text2_0", R.string.vw_video_speed_text2_0));
                } else if (speed == 1.5f) {
                    tv_speed.setText(LocaleController.getString("vw_video_speed_text1_5", R.string.vw_video_speed_text1_5));
                } else if (speed == 1.25f) {
                    tv_speed.setText(LocaleController.getString("vw_video_speed_text1_25", R.string.vw_video_speed_text1_25));
                } else if (speed == 1.0f) {
                    tv_speed.setText(LocaleController.getString("vw_video_speed_text1", R.string.vw_video_speed_text1));
                } else if (speed == 0.75f) {
                    tv_speed.setText(LocaleController.getString("vw_video_speed_text0_75", R.string.vw_video_speed_text0_75));
                }
                videoPlayer.setPlaybackSpeed(speed);
            }).show();
        });
    }

    private void playItem() {
        if (currentMessageObject == null) return;
        Uri uri = null;
        if (downloadStatus != null && downloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {//已完成
            uri = Uri.fromFile(downloadStatus.getVideoFile());
        } else {
            KKFileMessageManager.getInstance().autoCacheDownloadVideo(currentMessageObject);//开始下载
            try {
                int reference = FileLoader.getInstance(currentMessageObject.currentAccount).getFileReference(currentMessageObject);
                //FileLoader.getInstance(UserConfig.selectedAccount).loadFile(currentMessageObject.getDocument(), currentMessageObject, 1, 0);
                TLRPC.Document document = currentMessageObject.getDocument();
                String params = "?account=" + currentMessageObject.currentAccount +
                        "&id=" + document.id +
                        "&hash=" + document.access_hash +
                        "&dc=" + document.dc_id +
                        "&size=" + document.size +
                        "&mime=" + URLEncoder.encode(document.mime_type, "UTF-8") +
                        "&rid=" + reference +
                        "&name=" + URLEncoder.encode(FileLoader.getDocumentFileName(document), "UTF-8") +
                        "&reference=" + Utilities.bytesToHex(document.file_reference != null ? document.file_reference : new byte[0]);
                uri = Uri.parse("tg://" + currentMessageObject.getFileName() + params);
            } catch (Exception ignore) {
            }
        }
        if (uri != null) {
            //准备播放
            videoPlayer.preparePlayer(uri, "");
            videoPlayer.setPlayWhenReady(true);
            if (seekTo > 5 * 1000) {
                videoPlayer.seekTo(seekTo);
            }
        }
    }

    public void updateViewState(int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_BUFFERING:
                loading_progress.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_READY:
                loading_progress.setVisibility(View.GONE);
                video_cover.setVisibility(View.GONE);
                handler.postDelayed(runnable, 2000);
                break;
            case ExoPlayer.STATE_ENDED:
                break;
        }
    }

    public void updatePlayProgress() {
        long playTime = videoPlayer.getCurrentPosition();
        long duration = videoPlayer.getDuration();
        float pro = 0;
        if (duration != 0) pro = playTime * 100.0f / duration;
        if (seekBarInTouch) return;
        seek_progress.setProgress((int) pro);
        seek_progress.setSecondaryProgress(videoPlayer.getBufferedPercentage());
        tv_pro.setText(SystemUtil.timeTransfer((int) (playTime / 1000.0f)) + " / " + SystemUtil.timeTransfer((int) (duration / 1000.0f)));
    }

    public void updatePlayState(boolean doPlay) {
        if (doPlay) {
            videoPlayer.play();
            iv_play.setImageResource(R.drawable.ic_full_play_parse);
            handler.postDelayed(runnable, 4000);
        } else {
            videoPlayer.pause();
            iv_play.setImageResource(R.drawable.ic_full_play);
            handler.removeCallbacks(runnable);
        }
    }

    private void setCollect(boolean collect) {
        this.collect = collect;
        EventBus.getDefault().post(new MessageEvent(EventBusTags.COLLECT_CHANGE, new CollectChangeEvent(currentMessageObject.getId(),collect)));
        if (collect) {
            iv_video_collect.setImageResource(R.drawable.ic_video_collected);
        } else {
            iv_video_collect.setImageResource(R.drawable.ic_video_collect);
        }
    }

    private void showControl(boolean show) {
        if (show) {
            top_container.setVisibility(View.VISIBLE);
            bottom_container.setVisibility(View.VISIBLE);
            handler.postDelayed(runnable, 4000);
        } else {
            top_container.setVisibility(View.GONE);
            bottom_container.setVisibility(View.GONE);
        }
    }

}
