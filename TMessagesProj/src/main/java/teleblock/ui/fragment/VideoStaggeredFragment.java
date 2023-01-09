package teleblock.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.SizeUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.PhotoViewer;

import java.util.HashMap;
import java.util.List;

import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageLoadListener;
import teleblock.file.KKFileMessageManager;
import teleblock.file.KKFileTypes;
import teleblock.model.MiddleData;
import teleblock.model.VideoSlipEntity;
import teleblock.model.VideoStaggeredEntity;
import teleblock.ui.activity.VideoGroupActivity;
import teleblock.ui.activity.VideoSlipPageActivity;
import teleblock.ui.activity.VideoStaggeredActivity;
import teleblock.ui.adapter.VideoRvAdapter2;
import teleblock.util.EventUtil;
import teleblock.util.TGLog;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDownloadListener;

/**
 * Created by LSD on 2021/3/20.
 * Desc 首页视频瀑布流
 */
public class VideoStaggeredFragment extends BaseFragment implements KKFileMessageLoadListener, KKVideoDownloadListener {
    KKFileTypes currentType = KKFileTypes.TAG_VIDEO;
    private RecyclerView videoRv;
    private SmartRefreshLayout refreshLayout;

    VideoRvAdapter2 videoRvAdapter;
    long requestDialogId = 0;
    int page = 0;
    boolean onPause = false;
    VideoStaggeredEntity entity;

    public static VideoStaggeredFragment instance(VideoStaggeredEntity entity) {
        VideoStaggeredFragment fragment = new VideoStaggeredFragment();
        Bundle args = new Bundle();
        args.putSerializable("entity", entity);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_video_list2, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        TGLog.debug("VideoListFragment2 -> onResume");
        if (onPause) {
            onPause = false;
            KKFileMessageManager.getInstance().addListener(this);
            if (videoRvAdapter.getItemCount() == 0) {//首页会默认跳到第二个tab阻止了这里的加载
                refreshLayout.autoRefresh();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        TGLog.debug("VideoListFragment2 -> onPause");
        onPause = true;
        refreshLayout.finishRefresh();
        KKFileMessageManager.getInstance().removeListener(this);
    }

    @Override
    protected void onViewCreated() {
        KKFileMessageManager.getInstance().addListener(this);
        getExtra();
        initView();
        refreshLayout.autoRefresh();

        if ("messageFeedPage".equals(entity.from)) {//首页打开的视频统计
        }
    }

    public void onRefreshVideoEvent() {
        refreshLayout.autoRefresh();
    }

    private void getExtra() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("entity")) {
            entity = (VideoStaggeredEntity) bundle.getSerializable("entity");
            requestDialogId = entity.dialogId;
        }
    }

    private void initView() {
        //下拉刷新
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadData();
        });
        refreshLayout.setEnableLoadMore(false);

        //view
        videoRv = rootView.findViewById(R.id.video_rv);
        videoRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = SizeUtils.dp2px(4f);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = spacing;
                outRect.bottom = spacing;
            }
        });
        ((SimpleItemAnimator) videoRv.getItemAnimator()).setSupportsChangeAnimations(false);
        //videoRv.setItemAnimator(null);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        videoRv.setLayoutManager(layoutManager);
        videoRv.setAdapter(videoRvAdapter = new VideoRvAdapter2(mActivity));
        videoRvAdapter.getLoadMoreModule().setPreLoadNumber(4);
//        videoRvAdapter.getLoadMoreModule().setLoadMoreView(new NSLoadMoreView());
        videoRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            KKFileMessage message = videoRvAdapter.getItem(position);
            if (!TextUtils.isEmpty(message.getDateObject())) return;
            startActivity(new Intent(mActivity, VideoGroupActivity.class).putExtra("groupId", message.getDialogId()));
        });
        videoRvAdapter.addChildClickViewIds(R.id.ll_status, R.id.ll_text_content);
        videoRvAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            KKFileMessage message = videoRvAdapter.getItem(position);
            KKFileDownloadStatus status = message.getDownloadStatus();
            if (status == null) return;
            switch (view.getId()) {
                case R.id.ll_status:
                    if (status.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                        PhotoViewer.getInstance().setParentActivity((Activity) mActivity);
                        PhotoViewer.getInstance().openPhoto(message.getMessageObject(), null, message.getDialogId(), 0,0, photoViewerProvider);
                        return;
                    }
                    if (status.getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
                        KKFileMessageManager.getInstance().pauseDownloadVideo(message);
                    } else {
                        KKFileMessageManager.getInstance().startDownloadVideo(message);
                    }
                    break;
                case R.id.ll_text_content:
                    if (entity != null) {
                        if ("messageFeedPage".equals(entity.from)) {//首页瀑布流进来的，可以子界面瀑布流
                            VideoStaggeredEntity videoStaggeredEntity = new VideoStaggeredEntity();
                            videoStaggeredEntity.dialogId = message.getDialogId();
                            mActivity.startActivity(new Intent(mActivity, VideoStaggeredActivity.class).putExtra("entity", videoStaggeredEntity));
                        }
                    }
                    break;
            }
        });
        videoRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            VideoSlipEntity videoSlipEntity = new VideoSlipEntity();
            if ("messageFeedPage".equals(entity.from)) {//首页进来的，跳转到抖音样式，点小头像还可以进入子瀑布流
                videoSlipEntity.from = "homeOpenStagger";
            }
            videoSlipEntity.position = position;
            videoSlipEntity.page = page;
            videoSlipEntity.messageList = videoRvAdapter.getData();
            videoSlipEntity.hasMessage = true;
            videoSlipEntity.dialogId = requestDialogId;
            MiddleData.getInstance().videoSlipEntity = videoSlipEntity;

            VideoSlipEntity extra = new VideoSlipEntity();
            extra.hasMessage = true;
            startActivity(new Intent(mActivity, VideoSlipPageActivity.class).putExtra("entity", extra));
        });
        videoRvAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> loadMore());
    }

    private PhotoViewer.PhotoViewerProvider photoViewerProvider = new PhotoViewer.EmptyPhotoViewerProvider() {
        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
            return null;
        }
    };

    private void loadData() {
        TGLog.debug("VideoListFragment2 " + " loadData()");
        page = 0;
        KKFileMessageManager.getInstance().loadFileMessages(requestDialogId, currentType, page, this);
    }

    private void loadMore() {
        page++;
        KKFileMessageManager.getInstance().loadFileMessages(requestDialogId, currentType, page, this);
    }

    private List<KKFileMessage> handleMessage(List<KKFileMessage> messageList) {
        if (messageList == null || messageList.size() == 0) return messageList;
        //Collections.sort(messageList, new KKFileMessageSort());
        return messageList;
    }

    @Override
    public void onMessagesLoad(int loadRequestId, long dialogId, List<KKFileMessage> videoMessages) {
        TGLog.debug("VideoListFragment2 " + "VideoMessagesLoad==> page:" + page + ",size:" + videoMessages.size() + "");
        if ((videoMessages == null || videoMessages.size() == 0) && page == 0) {
        }
        boolean isEnd = videoMessages.size() < 20;
        final List<KKFileMessage> fList = handleMessage(videoMessages);
        AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.finishRefresh();
            videoRvAdapter.getLoadMoreModule().loadMoreComplete();
            if (isEnd) videoRvAdapter.getLoadMoreModule().loadMoreEnd(true);
            if (fList == null && fList.size() == 0) return;
            if (page == 0) {
                videoRvAdapter.setList(fList);
            } else {
                videoRvAdapter.addData(fList);
            }
        });
    }

    @Override
    public void onError(int loadRequestId, int errorCode, String msg) {
        AndroidUtilities.runOnUIThread(() -> {
            if (page != 0) {
                videoRvAdapter.getLoadMoreModule().loadMoreFail();
            } else {
                refreshLayout.finishRefresh(false);
            }
        });
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        AndroidUtilities.runOnUIThread(() -> videoRvAdapter.notifyItemStatusChanged(fileName));
    }
}
