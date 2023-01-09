package teleblock.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.imageview.ShapeableImageView;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.StatsController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.VideoPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.blogc.android.views.ExpandableTextView;
import teleblock.config.AppConfig;
import teleblock.config.Constants;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageCollectManager;
import teleblock.file.KKFileMessageManager;
import teleblock.model.BaseModel;
import teleblock.model.VideoSlipEntity;
import teleblock.ui.activity.BaseActivity;
import teleblock.util.EventUtil;
import teleblock.util.StringUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TGLog;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDataManager;


public class VideoSlipPageAdapter extends BaseDelegateMultiAdapter<KKFileMessage, BaseViewHolder> {
    private final int ITEM_TYPE_AD = 1;
    private final int ITEM_TYPE_NORMAL = 0;

    Context context;
    VideoPlayer videoPlayer;
    String from;
    boolean inTouch = false;

    public static final int what = 100;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == what) {
                ImageView iv_play = (ImageView) msg.obj;
                if (iv_play != null) iv_play.setVisibility(View.GONE);
            }
        }
    };

    public VideoSlipPageAdapter(Context context, VideoPlayer videoPlayer, String from) {
        this.context = context;
        this.videoPlayer = videoPlayer;
        this.from = from;

        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<KKFileMessage>() {
            @Override
            public int getItemType(@NotNull List<? extends KKFileMessage> data, int position) {
                KKFileMessage message = data.get(position);
                if (message.getModelType() == BaseModel.AD) {
                    return ITEM_TYPE_AD;
                } else {
                    return ITEM_TYPE_NORMAL;
                }
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(ITEM_TYPE_NORMAL, R.layout.layout_video_slip_item)
                .addItemType(ITEM_TYPE_AD, R.layout.layout_video_slip_ad_item);
    }

    public KKFileMessage getMessageByFileName(String fileName) {
        for (KKFileMessage kkVideoMessage : getData()) {
            if (!kkVideoMessage.isDateMessage() && fileName.equals(kkVideoMessage.getDownloadFileName())) {
                return kkVideoMessage;
            }
        }
        return null;
    }

    public void notifyItemStatusChanged(ViewPager2 viewPager2, String fileName, KKFileDownloadStatus fileDownloadStatus) {
        int position = -1;
        List<KKFileMessage> list = getData();
        for (int i = 0; i < list.size(); i++) {
            KKFileMessage kkVideoMessage = list.get(i);
            if (!kkVideoMessage.isDateMessage() && fileName.equals(kkVideoMessage.getDownloadFileName())) {
                position = i;
                break;
            }
        }
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
            TextView tv_download_size = baseViewHolder.findView(R.id.tv_download_size);
            if (fileDownloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                tv_download_size.setText(SystemUtil.getSizeFormat(fileDownloadStatus.getTotalSize()) + "/" + SystemUtil.getSizeFormat(fileDownloadStatus.getTotalSize()));
            } else {
                tv_download_size.setText(SystemUtil.getSizeFormat(fileDownloadStatus.getDownloadedSize()) + "/" + SystemUtil.getSizeFormat(fileDownloadStatus.getTotalSize()));
            }
        }
    }

    //获取位置
    public int getMessageIdPosition(int messageId) {
        int position = -1;
        for (int i = 0; i < getItemCount(); i++) {
            KKFileMessage message = getItem(i);
            if (message.getId() == messageId) {
                position = i;
                break;
            }
        }
        return position;
    }

    //获取位置
    public int getMessageIdPositionByName(String fileName) {
        int position = -1;
        for (int i = 0; i < getItemCount(); i++) {
            KKFileMessage message = getItem(i);
            if (fileName.equals(message.getDownloadFileName())) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void updateCollectState(BaseViewHolder baseViewHolder, boolean collect) {
        TextView textView = baseViewHolder.findView(R.id.tv_video_collect);
        setDrawableTop(textView, collect);
    }

    public void updatePlayState(BaseViewHolder baseViewHolder, boolean doPlay) {
        ImageView iv_play = baseViewHolder.findView(R.id.iv_play);
        ProgressBar loading_progress = baseViewHolder.findView(R.id.loading_progress);
        if (doPlay) {
            videoPlayer.play();
            iv_play.setImageResource(R.drawable.ic_item_pause);
            Message message = handler.obtainMessage();
            message.obj = iv_play;
            message.what = what;
            handler.sendMessageDelayed(message, 3000);
        } else {
            videoPlayer.pause();
            iv_play.setImageResource(R.drawable.ic_item_play);
            iv_play.setVisibility(View.VISIBLE);
            loading_progress.setVisibility(View.GONE);
            handler.removeMessages(what);
        }
    }

    public void updateViewState(BaseViewHolder baseViewHolder, int playbackState) {
        FrameLayout video_cover = baseViewHolder.findView(R.id.video_cover);
        ProgressBar loading_progress = baseViewHolder.findView(R.id.loading_progress);
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_BUFFERING:
                loading_progress.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_READY:
                loading_progress.setVisibility(View.GONE);
                video_cover.setVisibility(View.GONE);

                ImageView iv_play = baseViewHolder.findView(R.id.iv_play);
                if (iv_play.getVisibility() == View.VISIBLE) {
                    Message message = handler.obtainMessage();
                    message.obj = iv_play;
                    message.what = what;
                    handler.sendMessageDelayed(message, 3000);
                }
                break;
            case ExoPlayer.STATE_ENDED:
                break;
        }
    }

    public void updatePlayProgress(BaseViewHolder baseViewHolder) {
        SeekBar seekProgress = baseViewHolder.findView(R.id.seek_progress);
        TextView tv_pro = baseViewHolder.findView(R.id.tv_pro);

        long playTime = videoPlayer.getCurrentPosition();
        long duration = videoPlayer.getDuration();
        float pro = 0;
        if (duration != 0) pro = playTime * 100.0f / duration;
        if (inTouch) return;
        seekProgress.setProgress((int) pro);
        seekProgress.setSecondaryProgress(videoPlayer.getBufferedPercentage());
        tv_pro.setText(SystemUtil.timeTransfer((int) (playTime / 1000.0f)) + " / " + SystemUtil.timeTransfer((int) (duration / 1000.0f)));
    }

    public void updateCollectStatus(BaseViewHolder baseViewHolder, KKFileMessage message, VideoSlipEntity entity) {
        boolean collect = KKFileMessageCollectManager.getInstance().getCollectStatus(message);
        if (collect) {
            KKFileMessageCollectManager.getInstance().removeCollect(message);
        } else {
            KKFileMessageCollectManager.getInstance().collectMessage(message);

            String from = "";
            if ("feedView".equals(entity.from)) {//首页
                from = "首页的视频";
            } else if ("slipPage".equals(entity.from)) {//某个channel
                from = "频道的视频";
            } else if ("chatPage".equals(entity.from)) {//聊天界面
                from = "频道的视频";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("from", from);
        }
        TextView tv_video_collect = baseViewHolder.findView(R.id.tv_video_collect);
        setDrawableTop(tv_video_collect, !collect);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, KKFileMessage entity) {
        switch (baseViewHolder.getItemViewType()) {
            case ITEM_TYPE_NORMAL:
                showItem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_AD:
                showAdItem(baseViewHolder, entity);
                break;
        }
    }


    private void showItem(BaseViewHolder baseViewHolder, KKFileMessage entity) {
        //TGLog.debug("加载：：" + baseViewHolder.getAdapterPosition());
        if (entity.getDownloadStatus() != null && entity.getDownloadStatus().getStatus() != KKFileDownloadStatus.Status.DOWNLOADED) {
            if (entity.getSize() <= 50 * 1024 * 1024 && ApplicationLoader.getCurrentNetworkType() == StatsController.TYPE_WIFI) {//小于50M
                TGLog.debug("开始下载位置：" + baseViewHolder.getAdapterPosition() + ",fileName = " + entity.getDownloadFileName());
                KKFileMessageManager.getInstance().startDownloadVideo(entity);//开始下载
            }
        }
        View view_container = baseViewHolder.findView(R.id.view_container);
        FrameLayout video_container = baseViewHolder.findView(R.id.video_container);
        LinearLayout full_btn_container = baseViewHolder.findView(R.id.full_btn_container);
        TextureView video_view = baseViewHolder.findView(R.id.video_view);
        FrameLayout video_cover = baseViewHolder.findView(R.id.video_cover);
        ProgressBar loading_progress = baseViewHolder.findView(R.id.loading_progress);
        TextView tv_group_name = baseViewHolder.findView(R.id.tv_group_name);
        ExpandableTextView tv_video_text = baseViewHolder.findView(R.id.tv_video_text);
        TextView tv_expand = baseViewHolder.findView(R.id.tv_expand);
        TextView tv_views = baseViewHolder.findView(R.id.tv_views);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        SeekBar seekProgress = baseViewHolder.findView(R.id.seek_progress);
        TextView tv_pro = baseViewHolder.findView(R.id.tv_pro);
        TextView tv_video_collect = baseViewHolder.findView(R.id.tv_video_collect);

        TextView tv_video_download = baseViewHolder.findView(R.id.tv_video_download);
        TextView tv_video_guide = baseViewHolder.findView(R.id.tv_video_guide);
        TextView tv_guide_skip = baseViewHolder.findView(R.id.tv_guide_skip);
        TextView tv_save_album = baseViewHolder.findView(R.id.tv_save_album);
        tv_video_download.setText(LocaleController.getString("vw_video_download", R.string.vw_video_download));
        tv_video_guide.setText(LocaleController.getString("vw_video_guide_text", R.string.vw_video_guide_text));
        tv_guide_skip.setText(LocaleController.getString("vw_video_guide_btn_text", R.string.vw_video_guide_btn_text));
        tv_guide_skip.setText(LocaleController.getString("vw_video_guide_play_normal", R.string.vw_video_guide_play_normal));
        tv_save_album.setText(LocaleController.getString("vw_video_save_album", R.string.vw_video_save_album));

        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        ImageView iv_play = baseViewHolder.findView(R.id.iv_play);
        TextView tv_download_size = baseViewHolder.findView(R.id.tv_download_size);
        LinearLayout guide_container = baseViewHolder.findView(R.id.guide_container);
        View nativeAdLayout = baseViewHolder.findView(R.id.native_ad_layout);
        ImageView adIcon = baseViewHolder.findView(R.id.ad_icon);

        if (Constants.showOnlineAd() && !UserConfig.getInstance(UserConfig.selectedAccount).vip) {
            showGGNativeAdLayout(baseViewHolder, nativeAdLayout);
        } else {
            nativeAdLayout.setVisibility(View.GONE);
        }

        //收藏状态
        boolean collect = KKFileMessageCollectManager.getInstance().getCollectStatus(entity);
        setDrawableTop(tv_video_collect, collect);

        view_container.setOnClickListener(view -> {
            handler.removeMessages(what);
            if (iv_play.getVisibility() == View.VISIBLE) {
                iv_play.setVisibility(View.GONE);
            } else {
                iv_play.setVisibility(View.VISIBLE);
                Message message = handler.obtainMessage();
                message.obj = iv_play;
                message.what = what;
                handler.sendMessageDelayed(message, 3000);
            }
        });

        seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                inTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                inTouch = false;
                if (videoPlayer != null) {
                    int progress = seekBar.getProgress();
                    long duration = videoPlayer.getDuration();
                    long seekPosition = duration * progress / 100;
                    videoPlayer.seekTo(seekPosition);
                    tv_pro.setText(SystemUtil.timeTransfer((int) seekPosition / 1000) + " / " + SystemUtil.timeTransfer(entity.getMediaDuration()));
                }
            }
        });


        //init State
        iv_play.setVisibility(View.GONE);
        loading_progress.setVisibility(View.GONE);
        video_cover.setVisibility(View.GONE);

        TLRPC.Document document = entity.getDocument();
        if (document != null) {
            //视频区域大小
            ArrayList<TLRPC.DocumentAttribute> attributes = document.attributes;
            int viewH = 320;
            int vw = 0;
            int vh = 0;
            if (attributes != null && attributes.size() > 0) {
                vw = attributes.get(0).w;
                vh = attributes.get(0).h;
                int sw = ScreenUtils.getScreenWidth();
                if (vw != 0 && vh != 0) {
                    viewH = sw * vh / vw;
                }
            }
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video_container.getLayoutParams();
            params.height = viewH;
            video_container.setLayoutParams(params);

            float ratio = vw * 1.0f / vh;
            float setRatio = 16 * 1.0f / 9;
            if (vw > 760 && ratio >= setRatio) {//横屏视频
                full_btn_container.setVisibility(View.VISIBLE);
            } else {
                full_btn_container.setVisibility(View.GONE);
            }

            //视频封面
            BackupImageView ivThumb = new BackupImageView(context);
            if (document.thumbs != null) {
                TLRPC.PhotoSize bigthumb = FileLoader.getClosestPhotoSizeWithSize(entity.getDocument().thumbs, 320);
                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(entity.getDocument().thumbs, 40);
                if (thumb == bigthumb) {
                    bigthumb = null;
                }
                //ivThumb.setRoundRadius(AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
                ivThumb.getImageReceiver().setNeedsQualityThumb(bigthumb == null);
                ivThumb.getImageReceiver().setShouldGenerateQualityThumb(bigthumb == null);
                ivThumb.setImage(ImageLocation.getForDocument(bigthumb, entity.getDocument()), "480_320", ImageLocation.getForDocument(thumb, entity.getDocument()), "480_320_b", null, 0, 1, entity.getMessageObject());
                video_cover.addView(ivThumb);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_group_name.setVisibility(View.VISIBLE);
            tv_group_name.setText("@" + entity.getFromName());
        } else {
            tv_group_name.setVisibility(View.GONE);
        }

        //描述
        boolean showExpand;
        String videoText = entity.getMessage();
        if (TextUtils.isEmpty(videoText)) {
            showExpand = false;
        } else {
            showExpand = true;
        }
        int length = StringUtil.String_length(videoText);
        if (length < 220) {
            showExpand = false;
        }
        if (TextUtils.isEmpty(videoText)) {
            tv_video_text.setVisibility(View.GONE);
        } else {
            tv_video_text.setVisibility(View.VISIBLE);
            tv_video_text.setText(videoText);
        }
        tv_expand.setText(LocaleController.getString("fg_textview_expand", R.string.fg_textview_expand));
        tv_expand.setVisibility(showExpand ? View.VISIBLE : View.GONE);
        tv_expand.setOnClickListener(view -> {
            if (tv_video_text.isExpanded()) {
                tv_video_text.collapse();
            } else {
                tv_video_text.expand();
            }
        });
        tv_video_text.addOnExpandListener(new ExpandableTextView.OnExpandListener() {
            @Override
            public void onExpand(@NonNull ExpandableTextView view) {
                tv_expand.setText(LocaleController.getString("fg_textview_collapse", R.string.fg_textview_collapse));
                setDrawableRight(tv_expand, true);
            }

            @Override
            public void onCollapse(@NonNull ExpandableTextView view) {
                tv_expand.setText(LocaleController.getString("fg_textview_expand", R.string.fg_textview_expand));
                setDrawableRight(tv_expand, false);
            }
        });

        //查看数
        tv_views.setText(entity.getMessageObject().messageOwner.views + "");

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //进度
        seekProgress.setProgress(0);
        tv_pro.setText("00:00 / 00:00");

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.addView(avatarImageView);
        }

        //下载数据
        tv_download_size.setText(SystemUtil.getSizeFormat(entity.getDownloadStatus().getTotalSize()));
        //显示引导层
        guide_container.setOnClickListener(view -> {
        });
    }

    private void showAdItem(BaseViewHolder baseViewHolder, KKFileMessage entity) {
        if (entity.adClosed || entity.adLoaded) return;
        NativeAdView ggad_view = baseViewHolder.findView(R.id.ggad_view);
        FrameLayout max_ad_frame = baseViewHolder.findView(R.id.max_ad_frame);
        ggad_view.setVisibility(View.GONE);
        max_ad_frame.setVisibility(View.GONE);

        MediaView ggad_media = baseViewHolder.findView(R.id.ggad_media);
        ShapeableImageView ggad_icon = baseViewHolder.findView(R.id.ggad_icon);
        TextView tv_ggtitle = baseViewHolder.findView(R.id.tv_ggtitle);
        TextView tv_ggbody = baseViewHolder.findView(R.id.tv_ggbody);
        Button bt_ggbtn = baseViewHolder.findView(R.id.bt_ggbtn);

        LinearLayout ll_ad_remove = baseViewHolder.findView(R.id.ll_ad_remove);
        TextView tv_ad_tips = baseViewHolder.findView(R.id.tv_ad_tips);
        tv_ad_tips.setText(LocaleController.getString("ac_video_feed_ad_tips", R.string.ac_video_feed_ad_tips));

        //gg广告
        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();//video Muted
        NativeAdOptions adOptions = new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_LEFT).setVideoOptions(videoOptions).build();
        AdLoader adLoader = new AdLoader.Builder(context, AppConfig.AD_UNIT.SLIP_FEED_NATIVE_ID).forNativeAd(nativeAd -> {
            if (((BaseActivity) context).isDestroyed() || ((BaseActivity) context).isFinishing()) {
                nativeAd.destroy();
                return;
            }
            ggad_view.setVisibility(View.VISIBLE);
            tv_ggtitle.setText(nativeAd.getHeadline());
            tv_ggbody.setText(nativeAd.getBody());

            ggad_media.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            ggad_view.setMediaView(ggad_media);
            ggad_view.getMediaView().setMediaContent(nativeAd.getMediaContent());
            ggad_view.setIconView(ggad_icon);
            ggad_view.setBodyView(tv_ggbody);
            ggad_view.setHeadlineView(tv_ggtitle);
            NativeAd.Image image = nativeAd.getIcon();
            if (image != null && image.getDrawable() != null) ggad_icon.setImageDrawable(image.getDrawable());

            //action
            ggad_view.setCallToActionView(bt_ggbtn);
            String action = nativeAd.getCallToAction();
            bt_ggbtn.setText(TextUtils.isEmpty(action) ? "" : action);
            if (TextUtils.isEmpty(action)) {
                bt_ggbtn.setVisibility(View.INVISIBLE);
            } else {
                bt_ggbtn.setVisibility(View.VISIBLE);
            }
            ggad_view.setNativeAd(nativeAd);
        }).withAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                String error = String.format("domain: %s, code: %d, message: %s", loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                TGLog.erro("onAdFailedToLoad-->" + error);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Map<String, Object> map = new ArrayMap<>();
                map.put("type", entity.adStyle + "");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                entity.adLoaded = true;
            }
        }).withNativeAdOptions(adOptions).build();
        adLoader.loadAd(new AdRequest.Builder().build());

    }

    private void showGGNativeAdLayout(BaseViewHolder baseViewHolder, View adLayout) {
        KKFileMessage entity = getItem(baseViewHolder.getAdapterPosition());
        if (entity.adStyle == 2 || entity.adStyle == 3) {
            if (entity.adClosed || entity.adLoaded) return;
            entity.needShowAd = true;
            //gg广告
            baseViewHolder.findView(R.id.ggad_close).setOnClickListener(view -> {
                entity.adClosed = true;
                adLayout.setVisibility(View.GONE);
            });
            NativeAdView ggad_view = baseViewHolder.findView(R.id.ggad_view);

            MediaView ggad_media = baseViewHolder.findView(R.id.ggad_media);
            RelativeLayout ggad_content_layout = baseViewHolder.findView(R.id.ggad_content_layout);
            ggad_media.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            ShapeableImageView ggad_icon = baseViewHolder.findView(R.id.ggad_icon);
            TextView tv_ggtitle = baseViewHolder.findView(R.id.tv_ggtitle);
            TextView tv_ggbody = baseViewHolder.findView(R.id.tv_ggbody);
            Button bt_ggbtn = baseViewHolder.findView(R.id.bt_ggbtn);
            TextView tv_ad_tips = baseViewHolder.findView(R.id.tv_ad_tips);
            tv_ad_tips.setText(LocaleController.getString("ac_video_feed_ad_tips", R.string.ac_video_feed_ad_tips));

            adLayout.setVisibility(View.GONE);
            ggad_view.setVisibility(View.GONE);
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();//video Muted
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).setVideoOptions(videoOptions).build();
            AdLoader adLoader = new AdLoader.Builder(context, AppConfig.AD_UNIT.SLIP_FEED_NATIVE_ID).forNativeAd(nativeAd -> {
                if (((BaseActivity) context).isDestroyed() || ((BaseActivity) context).isFinishing()) {
                    nativeAd.destroy();
                    return;
                }
                adLayout.setVisibility(View.VISIBLE);
                ggad_view.setVisibility(View.VISIBLE);
                tv_ggtitle.setText(nativeAd.getHeadline());
                tv_ggbody.setText(nativeAd.getBody());

                ggad_view.setMediaView(ggad_media);
                ggad_view.getMediaView().setMediaContent(nativeAd.getMediaContent());
                ggad_view.setIconView(ggad_icon);
                ggad_view.setBodyView(tv_ggbody);
                ggad_view.setHeadlineView(tv_ggtitle);
                NativeAd.Image image = nativeAd.getIcon();
                if (image != null && image.getDrawable() != null) ggad_icon.setImageDrawable(image.getDrawable());

                //action
                ggad_view.setCallToActionView(bt_ggbtn);
                String action = nativeAd.getCallToAction();
                bt_ggbtn.setText(TextUtils.isEmpty(action) ? "" : action);
                if (TextUtils.isEmpty(action)) {
                    bt_ggbtn.setVisibility(View.INVISIBLE);
                } else {
                    bt_ggbtn.setVisibility(View.VISIBLE);
                }

                if (entity.adStyle == 2) {
                    ggad_media.setVisibility(View.GONE);
                    ggad_content_layout.setVisibility(View.VISIBLE);
                } else if (entity.adStyle == 3) {
                    ggad_media.setVisibility(View.VISIBLE);
                    ggad_content_layout.setVisibility(View.GONE);
                }

                ggad_view.setNativeAd(nativeAd);
            }).withAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    String error = String.format("domain: %s, code: %d, message: %s", loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                    TGLog.erro("onAdFailedToLoad-->" + error);
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Map<String, Object> map = new ArrayMap<>();
                    map.put("type", entity.adStyle + "");
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    entity.adLoaded = true;
                }
            }).withNativeAdOptions(adOptions).build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    private void setDrawableRight(TextView textView, boolean isOpen) {
        Drawable drawable;
        if (isOpen) {
            drawable = context.getResources().getDrawable(R.drawable.ic_text_close);
        } else {
            drawable = context.getResources().getDrawable(R.drawable.ic_text_open);
        }
        //这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, null, drawable, null);
    }

    private void setDrawableTop(TextView textView, boolean collect) {
        Drawable drawable;
        if (collect) {
            drawable = context.getResources().getDrawable(R.drawable.ic_video_collected);
            textView.setText(LocaleController.getString("vw_video_collected", R.string.vw_video_collected));
            textView.setTextColor(Color.parseColor("#ffffff"));
        } else {
            textView.setText(LocaleController.getString("vw_video_collect", R.string.vw_video_collect));
            drawable = context.getResources().getDrawable(R.drawable.ic_video_collect);
            textView.setTextColor(Color.parseColor("#ffffff"));
        }
        //这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
    }
}
