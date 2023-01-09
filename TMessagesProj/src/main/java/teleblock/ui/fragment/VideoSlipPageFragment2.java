package teleblock.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.exoplayer2.analytics.AnalyticsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.VideoPlayer;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.chat.TGChatManager;
import teleblock.config.AppConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.CollectChangeEvent;
import teleblock.event.data.DeleteVideoItemEvent;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageCollectManager;
import teleblock.file.KKFileMessageLoadListener;
import teleblock.file.KKFileMessageManager;
import teleblock.file.KKFileTypes;
import teleblock.file.KKLocalFileManager;
import teleblock.model.BaseModel;
import teleblock.model.MiddleData;
import teleblock.model.VideoSlipEntity;
import teleblock.model.VideoStaggeredEntity;
import teleblock.ui.activity.FullScreenPlayActivity;
import teleblock.ui.activity.MyMixActivity;
import teleblock.ui.activity.VideoStaggeredActivity;
import teleblock.ui.adapter.VideoSlipPageAdapter;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TGLog;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDownloadListener;


/**
 * Created by LSD on 2021/3/20.
 * Desc 通用视频抖音滑动Fragment，新的。给activity载体用
 */
public class VideoSlipPageFragment2 extends BaseFragment implements KKFileMessageLoadListener, KKVideoDownloadListener {
    KKFileTypes currentType = KKFileTypes.TAG_VIDEO;
    ViewPager2 viewPager2;
    LinearLayout mainLoading;
    LinearLayout nullLayout;
    LinearLayout ll_play_guide;
    ImageView play_guide_close;

    List<KKFileMessage> downloadingList = new ArrayList<>();
    VideoSlipPageAdapter slipPageAdapter;

    VideoPlayer videoPlayer;
    VideoSlipEntity entity;
    int page = 0;
    long requestDialogId = 0;
    int currentTab = AppConfig.HomeTab.TAB_VIDEO;
    int currentPlayPosition;
    boolean hasMore = true;
    boolean refreshData = false;

    boolean doSkip = false;
    boolean showGuide = false;
    boolean onPause = false;
    int adStyle;

    public static VideoSlipPageFragment2 instance(VideoSlipEntity entity) {
        VideoSlipPageFragment2 fragment = new VideoSlipPageFragment2();
        Bundle args = new Bundle();
        args.putSerializable("entity", entity);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_videoslip, null);
    }


    @Override
    public void onPause() {
        super.onPause();
        TGLog.debug("VideoSlipPageFragment " + "【" + (entity != null ? entity.title : "") + "】-> onPause");
        onPause = true;
        refreshData = false;
        KKFileMessageManager.getInstance().removeListener(this);

        updatePlayState(false);
        pauseAllDownload();
    }

    @Override
    public void onResume() {
        super.onResume();
        TGLog.debug("VideoSlipPageFragment " + "【" + (entity != null ? entity.title : "") + "】-> onResume");
        if (onPause) {
            onPause = false;
            KKFileMessageManager.getInstance().addListener(this);//文件列表监听
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        KKFileMessageManager.getInstance().removeListener(this);
        if (videoPlayer != null) {
            videoPlayer.releasePlayer(true);
        }
    }


    @Override
    protected void onViewCreated() {
        EventBus.getDefault().register(this);
        KKFileMessageManager.getInstance().addListener(this);//文件列表监听
        KKFileMessageCollectManager.getInstance().addDownloadFilesListener(this);//收藏下载状态
        entity = (VideoSlipEntity) getArguments().getSerializable("entity");

        initPlayer();
        initView();
        checkAndLoadData();
    }

    private void initView() {
        mainLoading = rootView.findViewById(R.id.main_loading_view);
        TextView tvVideoLoading = rootView.findViewById(R.id.tv_video_loading);
        tvVideoLoading.setText(LocaleController.getString("vw_video_main_loading", R.string.vw_video_main_loading));
        nullLayout = rootView.findViewById(R.id.null_layout);
        TextView tvEmpty = rootView.findViewById(R.id.tv_empty);
        tvEmpty.setText(LocaleController.getString("ac_downed_null_tips", R.string.ac_downed_null_tips));
        ll_play_guide = rootView.findViewById(R.id.ll_play_guide);
        ll_play_guide.setOnClickListener(view -> {
            doSkip = true;
            checkMessageAndSkip();
            hidePlayGuide();
        });
        play_guide_close = rootView.findViewById(R.id.play_guide_close);
        play_guide_close.setOnClickListener(view -> hidePlayGuide());
        TextView tvPlayGuide = rootView.findViewById(R.id.tv_play_guide);
        tvPlayGuide.setText(LocaleController.getString("view_play_guide_text", R.string.view_play_guide_text));
        viewPager2 = rootView.findViewById(R.id.viewPager2);
        ((RecyclerView) viewPager2.getChildAt(0)).setItemAnimator(null);//取消动画
        viewPager2.setVisibility(View.INVISIBLE);
        viewPager2.setKeepScreenOn(true);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setAdapter(slipPageAdapter = new VideoSlipPageAdapter(mActivity, videoPlayer, entity.from));
        slipPageAdapter.addChildClickViewIds(R.id.full_btn_container, R.id.iv_play, R.id.tv_video_download, R.id.tv_video_collect,
                R.id.avatar_frame, R.id.tv_guide_skip, R.id.tv_play_normal, R.id.ll_ad_remove, R.id.tv_save_album);
        slipPageAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            KKFileMessage message = slipPageAdapter.getItem(position);
            RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
            switch (view.getId()) {
//                case R.id.ll_ad_remove://订阅去广告
//                    TelegramUtil.payVip(mActivity);
//                    break;
                case R.id.full_btn_container:
                    FullScreenPlayActivity.currentMessageObject = message.getMessageObject();
                    FullScreenPlayActivity.downloadStatus = message.getDownloadStatus();
                    FullScreenPlayActivity.seekTo = videoPlayer.getCurrentPosition();
                    startActivity(new Intent(mActivity, FullScreenPlayActivity.class));
                    break;
                case R.id.iv_play:
                    boolean playing = videoPlayer.isPlaying();
                    updatePlayState(!playing);
                    break;
                case R.id.tv_video_download:
                    String from = "";
                    if ("collectPage".equals(entity.from)) {//收藏
                        from = "收藏的视频";
                    } else if ("feedView".equals(entity.from)) {//首页
                        from = "首页的视频";
                    } else if ("slipPage".equals(entity.from)) {//某个channel
                        from = "频道的视频";
                    } else if ("chatPage".equals(entity.from)) {//聊天界面
                        from = "频道的视频";
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("from", from);
                    KKFileMessageManager.getInstance().manualStartDownloadVideo(message.getMessageObject());//手动开始下载
                    startActivity(new Intent(mActivity, MyMixActivity.class).putExtra("currentItem", 1).putExtra("target", "downloading"));
                    break;
                case R.id.tv_save_album:
                    if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                        KKLocalFileManager.getInstance().updateMessageFlag(message.getMessageObject(), "save_album");
                        MediaController.saveFile(message.getDownloadStatus().getVideoFile().toString(), ApplicationLoader.applicationContext, 1, null, null, new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SAVE_GALLERY);
                            }
                        });
                    } else {
                        KKFileMessageManager.getInstance().startDownloadVideo(message.getMessageObject(), "save_album", 0);
                    }
                    MyMixActivity.start(mActivity, 1, "save_album");
                    break;
                case R.id.tv_video_collect:
                    if (viewHolder != null) {
                        BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                        slipPageAdapter.updateCollectStatus(baseViewHolder, message, entity);
                    }
                    break;
                case R.id.avatar_frame:
                    if ("homeOpenStagger".equals(entity.from)) {
                        VideoStaggeredEntity videoStaggeredEntity = new VideoStaggeredEntity();
                        videoStaggeredEntity.dialogId = message.getDialogId();
                        mActivity.startActivity(new Intent(mActivity, VideoStaggeredActivity.class).putExtra("entity", videoStaggeredEntity));
                    }
                    break;
                case R.id.tv_guide_skip:
                    doSkip = true;
                    checkMessageAndSkip();
                    break;
                case R.id.tv_play_normal:
                    if (position + 1 < slipPageAdapter.getItemCount()) {
                        viewPager2.setCurrentItem(position + 1, false);
                        viewPager2.postDelayed(() -> {
                            playItem(position + 1);
                        }, 600);
                    }
                    break;
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (positionOffsetPixels < 700) return;
                //隐藏跳转按钮
                hidePlayGuide();

                //已加载的广告不在显示
                KKFileMessage message = slipPageAdapter.getItem(position);
                if (message.needShowAd) message.adClosed = true;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //自动播放
                playItem(position);

                if (hasMore && position >= slipPageAdapter.getItemCount() - 4) {
                    loadMore();
                }
            }
        });
    }

    private void initPlayer() {
        videoPlayer = new VideoPlayer();
        videoPlayer.setLooping(true);
        videoPlayer.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(currentPlayPosition);
                if (viewHolder != null) {
                    if (slipPageAdapter.getItem(currentPlayPosition).getModelType() == BaseModel.AD) return;
                    BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                    slipPageAdapter.updateViewState(baseViewHolder, playbackState);
                }
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
                RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(currentPlayPosition);
                if (viewHolder != null) {
                    if (slipPageAdapter.getItem(currentPlayPosition).getModelType() == BaseModel.AD) return;
                    BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                    slipPageAdapter.updatePlayProgress(baseViewHolder);
                }
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

    private void playItem(int position) {
        TGLog.debug("VideoSlipPageFragment " + "PLAY " + position);
        if (currentPlayPosition != 0 && position == currentPlayPosition) return;
        if (currentPlayPosition >= slipPageAdapter.getItemCount()) return;
        String from = "";
        if ("collectPage".equals(entity.from)) {//收藏
            from = "收藏的视频";
        } else if ("feedView".equals(entity.from)) {//首页
            from = "首页的视频";
        } else if ("slipPage".equals(entity.from)) {//某个channel
            from = "频道的视频";
        } else if ("chatPage".equals(entity.from)) {//聊天界面
            from = "频道的视频";
        }
        Map<String, Object> map = new HashMap<>();
        KKFileMessage temp = slipPageAdapter.getItem(currentPlayPosition);
        map.put("from", from);
        if (temp != null) {
            map.put("dialogId", temp.getDialogId() + "");
            map.put("title", temp.getFromName());
            map.put("messageId", temp.getMessageObject().messageOwner.id + "");
        }
        map.put("from", "视频流");

        //处理上一个
        if (currentPlayPosition < slipPageAdapter.getItemCount()) {
            KKFileMessage message = slipPageAdapter.getItem(currentPlayPosition);
            if (message.getModelType() == BaseModel.NORMAL) {
                RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(currentPlayPosition);
                if (viewHolder != null) {
                    BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                    TextureView video_view = baseViewHolder.findView(R.id.video_view);
                    FrameLayout video_cover = baseViewHolder.findView(R.id.video_cover);
                    video_view.setVisibility(View.GONE);//隐藏避免画面没有刷新，显示上一个的画面
                    video_cover.setVisibility(View.GONE);
                }

                if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
                    TGLog.debug("VideoSlipPageFragment " + "暂停下载位置：" + currentPlayPosition);
                    List<Integer> manualDownloadIds = KKLocalFileManager.getInstance().getFlagMessageIds("user");
                    if (!manualDownloadIds.contains(message.getId())) {//自己手动下载的不暂停
                        KKFileMessageManager.getInstance().pauseDownloadVideo(message);
                    }
                }
            }
        }

        //处理当前
        if (videoPlayer.isPlaying()) videoPlayer.pause();
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            currentPlayPosition = position;
            KKFileMessage message = slipPageAdapter.getItem(position);
            if (message.getModelType() == BaseModel.NORMAL) {
                doKeepLastPlay(message);//保存最后一个播放

                //设置播放TextureView
                BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                TextureView video_view = baseViewHolder.findView(R.id.video_view);
                video_view.setVisibility(View.VISIBLE);
                videoPlayer.setTextureView(video_view);

                //显示封面
                FrameLayout video_cover = baseViewHolder.findView(R.id.video_cover);
                video_cover.setVisibility(View.VISIBLE);

                Uri uri = null;
                if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {//已完成
                    uri = Uri.fromFile(message.getDownloadStatus().getVideoFile());
                } else {
                    if (message.getDownloadStatus().getStatus() != KKFileDownloadStatus.Status.DOWNLOADING) {
                        TGLog.debug("VideoSlipPageFragment " + "播放时下载位置：" + position);
                        KKFileMessageManager.getInstance().startDownloadVideo(message, 1);//开始下载
                    }
                    try {
                        MessageObject currentMessageObject = message.getMessageObject();
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
                    if (videoPlayer.isPlaying()) videoPlayer.pause();

                    //准备播放
                    videoPlayer.preparePlayer(uri, "");
                    if (onPause) {
                        videoPlayer.setPlayWhenReady(false);
                    } else {
                        videoPlayer.setPlayWhenReady(true);
                    }
                }
            }
        }
    }

    private void updatePlayState(boolean doPlay) {
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(currentPlayPosition);
        if (viewHolder != null) {
            if (slipPageAdapter.getItem(currentPlayPosition).getModelType() == BaseModel.AD) return;
            BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
            slipPageAdapter.updatePlayState(baseViewHolder, doPlay);
        }
    }

    private void checkAndLoadData() {
        if (entity.hasMessage) {
            entity = MiddleData.getInstance().videoSlipEntity;
        }
        if (entity != null && entity.messageList != null && entity.messageList.size() > 0) {
            requestDialogId = entity.dialogId;
            page = entity.page;
            AndroidUtilities.runOnUIThread(() -> {
                mainLoading.setVisibility(View.GONE);
                viewPager2.setVisibility(View.VISIBLE);
//                List<KKFileMessage> messageList = insertAdMessage(entity.messageList);
                List<KKFileMessage> messageList = entity.messageList;
                slipPageAdapter.setNewInstance(messageList);
                if (entity.position != 0) {
                    int position = entity.position;
                    KKFileMessage kData = entity.messageList.get(position);
                    for (int i = 0; i < messageList.size(); i++) {
                        KKFileMessage iData = messageList.get(i);
                        if (iData.getId() == kData.getId()) {
                            position = i;
                            break;
                        }
                    }
                    int fPosition = position;
                    TGLog.debug("fPosition = " + fPosition);
                    viewPager2.setCurrentItem(fPosition, false);
                    viewPager2.postDelayed(() -> {
                        playItem(fPosition);
                    }, 600);
                }
            });
        } else {
            doLoadData();
        }
    }

    private void doLoadData() {
        if (slipPageAdapter.getItemCount() == 0) {
            mainLoading.setVisibility(View.VISIBLE);
        }
        loadData();
    }

    private void loadData() {
        page = 0;
        if (videoPlayer.isPlaying()) {
            videoPlayer.pause();
        }
        if (entity == null) {
            mainLoading.setVisibility(View.GONE);
            nullLayout.setVisibility(View.VISIBLE);
            viewPager2.setVisibility(View.INVISIBLE);
            return;
        }
        //收藏界面
        if (entity != null && "collectPage".equals(entity.from)) {
            loadCollectData();
            return;
        }

        TGLog.debug("VideoSlipPageFragment " + "【" + (entity != null ? entity.title : "") + "】loadData");
        if ("feedView".equals(entity.from)) {//首页视频流
            requestDialogId = 0;
        } else if ("slipPage".equals(entity.from)) {//某个channel
            requestDialogId = entity.dialogId;
        } else if ("chatPage".equals(entity.from)) {//聊天界面
            requestDialogId = entity.dialogId;
        }
        KKFileMessageManager.getInstance().loadFileMessages(requestDialogId, currentType, page, this);
    }

    private void loadMore() {
        page++;
        KKFileMessageManager.getInstance().loadFileMessages(requestDialogId, currentType, page, this);
    }

    //收藏列表
    private void loadCollectData() {
        KKFileMessageCollectManager.getInstance().loadCollectMessageList(videoMessages -> {
            hasMore = false;
            AndroidUtilities.runOnUIThread(() -> {
                mainLoading.setVisibility(View.GONE);
                viewPager2.setVisibility(View.VISIBLE);
                slipPageAdapter.setNewInstance(videoMessages);
                if (entity.position != 0) {
                    viewPager2.setCurrentItem(entity.position, false);
                    viewPager2.postDelayed(() -> {
                        playItem(entity.position);
                    }, 600);
                }
            });
        });
    }

    //检查是否有重复消息
    private List<KKFileMessage> checkVideoMessages(List<KKFileMessage> videoMessages) {
        List<KKFileMessage> newList = new ArrayList<>();
        List<KKFileMessage> dataList = slipPageAdapter.getData();
        if (dataList == null || dataList.size() == 0 || videoMessages == null) return videoMessages;

        for (KKFileMessage kkVideoMessage : videoMessages) {
            boolean has = false;
            for (KKFileMessage temp : dataList) {
                if (kkVideoMessage.getId() == temp.getId()) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                newList.add(kkVideoMessage);
            }
        }
        return newList;
    }


//    private List<KKFileMessage> insertAdMessage(List<KKFileMessage> videoMessages) {
//        if (!Constants.showOnlineAd() || !PaperUtil.onlineAdConfig().video_view_slide.adSwitch) {
//            return videoMessages;
//        }
//        List<KKFileMessage> newList = new ArrayList<>();
//        for (int position = 0; position < videoMessages.size(); position++) {
//            KKFileMessage kkVideoMessage = videoMessages.get(position);
//            if (position != 0 && (position == ADUtil.firstPosterADShowPosition() || (position - ADUtil.firstPosterADShowPosition()) % ADUtil.getPosterADInterval() == 0)) {//需要插入广告
//                if (ManifestUtil.getChannel(mActivity).contains("official")) {//rushTG
//                    newList.add(kkVideoMessage);
//                    KKFileAdMessage adMessage = new KKFileAdMessage(kkVideoMessage.getMessageObject(), kkVideoMessage.getDownloadStatus(), kkVideoMessage.getFileType());
//                    adMessage.adStyle = 1;
//                    newList.add(adMessage);
//                } else {
//                    if (adStyle % 3 == 0) {//样式1插入一条Ad数据
//                        newList.add(kkVideoMessage);
//                        KKFileAdMessage adMessage = new KKFileAdMessage(kkVideoMessage.getMessageObject(), kkVideoMessage.getDownloadStatus(), kkVideoMessage.getFileType());
//                        adMessage.adStyle = 1;
//                        newList.add(adMessage);
//                        adStyle = adMessage.adStyle;
//                    } else {
//                        kkVideoMessage.adStyle = adStyle % 3 + 1;
//                        newList.add(kkVideoMessage);
//                        adStyle = kkVideoMessage.adStyle;
//                    }
//                }
//            } else {
//                newList.add(kkVideoMessage);
//            }
//        }
//        return newList;
//    }

    //检查是否需要显示引导
    private void checkPlayGuide() {
        new Thread(() -> {
            if (MMKVUtil.getLastPlayId(requestDialogId) == 0) {
                showGuide = false;
            } else {
                ArrayList<TLRPC.Chat> chats = TGChatManager.getInstance().getAllChats();
                boolean lastKeepExist = false;
                for (TLRPC.Chat chat : chats) {
                    if (Math.abs(chat.id) == Math.abs(MMKVUtil.getLastPlayItemDialogId(requestDialogId))) {
                        lastKeepExist = true;
                        break;
                    }
                }
                showGuide = lastKeepExist;
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (showGuide) {
                    showPlayGuide();
                }
            });
        }).start();
    }

    private void showPlayGuide() {
        if (ll_play_guide.getVisibility() == View.VISIBLE) return;
        ll_play_guide.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_play_guide, "translationX", SizeUtils.dp2px(-200), 0);
        animator.setDuration(400);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private void hidePlayGuide() {
        if (ll_play_guide.getVisibility() == View.GONE) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_play_guide, "translationX", 0, SizeUtils.dp2px(-200));
        animator.setDuration(400);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ll_play_guide.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    //检查变跳转到指定消息
    private void checkMessageAndSkip() {
        mainLoading.setVisibility(View.VISIBLE);
        if (videoPlayer.isPlaying()) {
            videoPlayer.pause();
        }
        List<KKFileMessage> videoMessages = slipPageAdapter.getData();
        int index = -1;
        if (videoMessages != null) {
            for (int i = 0; i < videoMessages.size(); i++) {
                KKFileMessage message = videoMessages.get(i);
                if (message.getId() == MMKVUtil.getLastPlayId(requestDialogId) && Math.abs(message.getDialogId()) == MMKVUtil.getLastPlayItemDialogId(requestDialogId)) {
                    index = i;
                    break;
                }
            }
        }
        final int fIndex = index;
        if (fIndex == -1) {
            loadMore();
        } else {
            mainLoading.setVisibility(View.GONE);
            doSkip = false;
            viewPager2.setCurrentItem(fIndex, false);
            viewPager2.postDelayed(() -> {
                playItem(fIndex);
            }, 600);
        }
    }

    //保存最后一个播放记录
    private void doKeepLastPlay(KKFileMessage message) {
        long time = MMKVUtil.getLastPlayItemTime(requestDialogId);
        if (message.getMessageObject() != null) {
            if (time == 0 || time > message.getMessageObject().messageOwner.date) {
                MMKVUtil.setLastPlayItemTime(requestDialogId, message.getMessageObject().messageOwner.date);
                MMKVUtil.setLastPlayId(requestDialogId, message.getId());
                MMKVUtil.setLastPlayItemDialogId(requestDialogId, Math.abs(message.getDialogId()));
            }
        }
    }

    //暂停所有下载
    private void pauseAllDownload() {
        if (downloadingList == null || downloadingList.size() == 0) return;
        List<Integer> manualDownloadIds = KKLocalFileManager.getInstance().getFlagMessageIds("user");
        for (KKFileMessage message : downloadingList) {
            if (!manualDownloadIds.contains(message.getId())) {
                if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
                    KKFileMessageManager.getInstance().pauseDownloadVideo(message);
                }
            }
        }
        downloadingList = new ArrayList<>();
    }

    @Override
    public void onMessagesLoad(int loadRequestId, long dialogId, List<KKFileMessage> videoMessages) {
        if (dialogId == requestDialogId) {
            TGLog.debug("VideoSlipPageFragment " + "【" + (entity != null ? entity.title : "") + "】VideoMessagesLoad==> page:" + page + ",size:" + videoMessages.size() + "");
            hasMore = !(videoMessages.size() < 20);
            AndroidUtilities.runOnUIThread(() -> {
                mainLoading.setVisibility(View.GONE);
                if (doSkip) {
                    checkMessageAndSkip();
                }
            });
            if (page == 0) {
                AndroidUtilities.runOnUIThread(() -> {
                    if (videoMessages == null || videoMessages.size() == 0) {
                        nullLayout.setVisibility(View.VISIBLE);
                        viewPager2.setVisibility(View.INVISIBLE);
                    } else {
//                        List<KKFileMessage> aList = insertAdMessage(videoMessages);
                        List<KKFileMessage> aList = videoMessages;
                        nullLayout.setVisibility(View.GONE);
                        viewPager2.setVisibility(View.VISIBLE);
                        //checkPlayGuide();
                        slipPageAdapter.setNewInstance(aList);
                        if (refreshData) {
                            refreshData = false;
                            viewPager2.setCurrentItem(0, false);
                            viewPager2.postDelayed(() -> {
                                playItem(0);
                            }, 600);
                        }
                    }
                });
            } else {
                List<KKFileMessage> fList = checkVideoMessages(videoMessages);
//                List<KKFileMessage> aList = insertAdMessage(fList);
                List<KKFileMessage> aList = fList;
                AndroidUtilities.runOnUIThread(() -> slipPageAdapter.addData(aList));
            }
        }
    }

    @Override
    public void onError(int loadRequestId, int errorCode, String msg) {
        AndroidUtilities.runOnUIThread(() -> {
            mainLoading.setVisibility(View.GONE);
        });
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        //AndroidUtilities.runOnUIThread(() -> slipPageAdapter.notifyItemStatusChanged(viewPager2, fileName, fileDownloadStatus));
        if (fileDownloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
            KKFileMessage message = slipPageAdapter.getMessageByFileName(fileName);
            if (message != null && !downloadingList.contains(message)) {
                downloadingList.add(message);
            }
        } else if (fileDownloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
            int position = slipPageAdapter.getMessageIdPositionByName(fileName);
            //TGLog.debug("VideoSlipPageFragment " + "下载完成位置：" + position + ",fileName = " + fileName);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBus(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DELETE_VIDEO_ITEM:
                DeleteVideoItemEvent data = (DeleteVideoItemEvent) event.getData();
                String fileName = SystemUtil.getFileName(data.filePath);
                KKFileMessage kkVideoMessage = slipPageAdapter.getMessageByFileName(fileName);
                if (kkVideoMessage != null) {
                    kkVideoMessage.getDownloadStatus().setStatus(KKFileDownloadStatus.Status.NOT_START);
                }
                break;

            case EventBusTags.COLLECT_CHANGE:
                CollectChangeEvent mCollectChangeEvent = (CollectChangeEvent) event.getData();
                int messageId = mCollectChangeEvent.messageId;
                boolean collect = mCollectChangeEvent.collect;

                int position = slipPageAdapter.getMessageIdPosition(messageId);
                RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager2.getChildAt(0)).findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
                    slipPageAdapter.updateCollectState(baseViewHolder, collect);
                }
                break;
        }
    }

//    private void removeAdMessage() {
//        if (!UserConfig.getInstance(UserConfig.selectedAccount).vip) return;
//        KKFileMessage message = slipPageAdapter.getItem(viewPager2.getCurrentItem());
//        if (message.adStyle == 1) {
//            message = slipPageAdapter.getItem(viewPager2.getCurrentItem() + 1);
//        } else {
//            updatePlayState(false);
//        }
//        // 过滤广告数据
//        List<KKFileMessage> messageList = slipPageAdapter.getData();
//        CollectionUtils.filter(messageList, item -> item.adStyle != 1);
//        slipPageAdapter.setList(messageList);
//        // 定位当前播放位置
//        int pos = 0;
//        messageList = slipPageAdapter.getData();
//        for (int i = 0; i < messageList.size(); i++) {
//            if (messageList.get(i).getId() == message.getId()) {
//                pos = i;
//                break;
//            }
//        }
//        int finalPos = pos;
//        viewPager2.setCurrentItem(finalPos, false);
//        viewPager2.postDelayed(() -> {
//            currentPlayPosition = 0;
//            playItem(finalPos);
//        }, 600);
//    }
}
