package teleblock.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.DeleteVideoItemEvent;
import teleblock.event.data.DeleteVideoSelectEvent;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageManager;
import teleblock.file.KKFileTypes;
import teleblock.file.ManualDownloadFileManager;
import teleblock.model.MiddleData;
import teleblock.ui.activity.VideoPlayActivity;
import teleblock.ui.adapter.MyDownloadIngRvAdapter;
import teleblock.util.NetUtil;
import teleblock.util.ShareUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TGLog;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDataManager;
import teleblock.video.KKVideoDownloadListener;


/**
 * Created by LSD on 2021/3/20.
 * Desc 保存到相册
 */
public class MySaveAlbumFragment extends BaseFragment implements ManualDownloadFileManager.Listener<KKFileMessage>, KKVideoDownloadListener {
    private SmartRefreshLayout refreshLayout;
    private RecyclerView downdingRv;
    private LinearLayout bottomLayout;
    private TextView tvSaveToGallery;
    private TextView tvDoDelete;
    private TextView tvDoShare;
    private LinearLayout nullLayout;
    private TextView tvEmpty;
    private TextView tvWifiSpeed;

    private MyDownloadIngRvAdapter videoRvAdapter;
    List<KKFileMessage> videos;
    public static int downloadId = -1;

    public static MySaveAlbumFragment instance() {
        MySaveAlbumFragment fragment = new MySaveAlbumFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_my_save_album, null);
    }

    @Override
    protected void onViewCreated() {
        EventBus.getDefault().register(this);
        initView();

        //添加监听下载中的文件
        ManualDownloadFileManager.getInstance().addLocalVideoFilesListener(this, this);
        ManualDownloadFileManager.getInstance().checkUpdate(true, "save_album");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        //下拉刷新
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        downdingRv = rootView.findViewById(R.id.downding_rv);
        bottomLayout = rootView.findViewById(R.id.bottom_layout);
        tvSaveToGallery = rootView.findViewById(R.id.tv_save_to_gallery);
        tvDoDelete = rootView.findViewById(R.id.tv_do_delete);
        tvDoShare = rootView.findViewById(R.id.tv_do_share);
        nullLayout = rootView.findViewById(R.id.null_layout);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        tvWifiSpeed = rootView.findViewById(R.id.tv_wifi_speed);

        tvSaveToGallery.setText(LocaleController.getString("ac_download_text_save_gallery", R.string.ac_download_text_save_gallery));
        tvDoDelete.setText(LocaleController.getString("ac_download_text_delete", R.string.ac_download_text_delete));
        tvDoShare.setText(LocaleController.getString("ac_download_text_share", R.string.ac_download_text_share));
        tvEmpty.setText(LocaleController.getString("ac_save_album_null_tips", R.string.ac_save_album_null_tips));

        refreshLayout.setOnRefreshListener(refreshLayout -> {
            ManualDownloadFileManager.getInstance().checkUpdate(true, "save_album");
        });
        downdingRv.setItemAnimator(null);
        downdingRv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        downdingRv.setAdapter(videoRvAdapter = new MyDownloadIngRvAdapter(mActivity));
        videoRvAdapter.getLoadMoreModule().setEnableLoadMore(false);
        videoRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            KKFileMessage message = videoRvAdapter.getItem(position);
            if (videoRvAdapter.isEdit()) {//删除模式
                if (!videoRvAdapter.selectList.contains(message.getDownloadFileName())) {
                    videoRvAdapter.selectList.add(message.getDownloadFileName());
                } else {
                    videoRvAdapter.selectList.remove(message.getDownloadFileName());
                }
                videoRvAdapter.notifyItemChanged(position);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_SELECT, new DeleteVideoSelectEvent(videoRvAdapter.selectList.size(), videoRvAdapter.selectList.size() == videoRvAdapter.getItemCount())));
            } else {//正常模式点击
                if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                    if (message.getFileType() == KKFileTypes.VIDEO_FILTER || message.getFileType() == KKFileTypes.MKV || message.getFileType() == KKFileTypes.MOV || message.getFileType() == KKFileTypes.MP4) {
                        //媒体
                        int p_position = 0;
                        List<String> dataList = new ArrayList<>();
                        List<KKFileMessage> list = videoRvAdapter.getDownloadedMedia();
                        for (int i = 0; i < list.size(); i++) {
                            KKFileMessage temp = list.get(i);
                            dataList.add(temp.getDownloadStatus().getVideoFile().getAbsolutePath());
                            if (message.getId() == temp.getId()) {
                                p_position = i;
                            }
                        }
                        if (dataList.size() == 0) return;
                        MiddleData.getInstance().playList = dataList;
                        startActivity(new Intent(mActivity, VideoPlayActivity.class).putExtra("position", p_position).putExtra("from", "show_album"));
                    } else {//其他
                        try {
                            AndroidUtilities.openForView(message.getMessageObject(), mActivity, null);
                        } catch (Exception e) {
                            TGLog.erro(e.getMessage());
                        }
                    }
                } else if (message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
                    KKFileMessageManager.getInstance().pauseDownloadVideo(message);
                } else {
                    KKFileMessageManager.getInstance().startDownloadVideo(message);
                }
            }
        });
        videoRvAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            MyDownloadContainerFragment myDownloadContainerFragment = (MyDownloadContainerFragment) getParentFragment();
            myDownloadContainerFragment.ivEdit.performClick();
            return true;
        });

        //删除
        tvDoDelete.setOnClickListener(view -> {
            if (videoRvAdapter.selectList.size() > 0) {
                for (String fileName : videoRvAdapter.selectList) {
                    KKFileMessage message = videoRvAdapter.getMessageByFileName(fileName);
                    KKVideoDataManager.getInstance().removeLocalFile(fileName);
                    if (message != null && message.getDownloadStatus() != null) {
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_ITEM, new DeleteVideoItemEvent(message.getDownloadStatus().getVideoFile().getAbsolutePath())));
                    }
                }
                videoRvAdapter.selectList.clear();
                Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
            }
        });
        //分享
        tvDoShare.setOnClickListener(view -> {
            if (CollectionUtils.isEmpty(videoRvAdapter.selectList)) return;
            for (String fileName : videoRvAdapter.selectList) {
                KKFileMessage message = videoRvAdapter.getMessageByFileName(fileName);
                if (message != null && message.getDownloadStatus() != null) {
                    if (KKFileDownloadStatus.Status.DOWNLOADED == message.getDownloadStatus().getStatus()) {
                        ShareUtil.shareVideo2(mActivity, message.getDownloadStatus().getVideoFile().toString());
                        break;
                    }
                }
            }
        });
    }

    public void checkAllOrUnCheckAll(boolean checkAll) {
        List<KKFileMessage> list = videoRvAdapter.getData();
        videoRvAdapter.selectList.clear();
        for (KKFileMessage entity : list) {
            if (checkAll) {
                videoRvAdapter.selectList.add(entity.getDownloadFileName());
            }
        }
        videoRvAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_SELECT, new DeleteVideoSelectEvent(videoRvAdapter.selectList.size(), checkAll)));
    }

    private void setNetSpeed() {
        int net = NetUtil.getNetworkState(mActivity);
        Drawable drawableLeft;
        if (net == 1) {
            drawableLeft = mActivity.getResources().getDrawable(R.drawable.icon_wifi);
            tvWifiSpeed.setTextColor(Color.parseColor("#25BD45"));
            long netSpeed = SystemUtil.getNetSpeed();
            if (netSpeed > 1024) {
                tvWifiSpeed.setText(LocaleController.getString("wifi_net", R.string.wifi_net) + " " + String.format("%.1f", netSpeed / 1024.0) + "MB/s");
            } else {
                tvWifiSpeed.setText(LocaleController.getString("wifi_net", R.string.wifi_net) + " " + netSpeed + "KB/s");
            }
        } else {
            drawableLeft = mActivity.getResources().getDrawable(R.drawable.icon_netmobile);
            tvWifiSpeed.setTextColor(Color.parseColor("#FF4343"));
            long netSpeed = SystemUtil.getNetSpeed();
            if (netSpeed > 1024) {
                tvWifiSpeed.setText(LocaleController.getString("mobile_net", R.string.mobile_net) + " " + String.format("%.1f", netSpeed / 1024.0) + "MB/s");
            } else {
                tvWifiSpeed.setText(LocaleController.getString("mobile_net", R.string.mobile_net) + " " + netSpeed + "KB/s");
            }
        }
        tvWifiSpeed.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
        tvWifiSpeed.setCompoundDrawablePadding(10);
    }


    public void notifyEditChange(boolean isEdit) {
        videoRvAdapter.setEdit(isEdit);
        if (isEdit) {
            bottomLayout.setVisibility(View.VISIBLE);
        } else {
            bottomLayout.setVisibility(View.GONE);
        }
    }

    public List<KKFileMessage> sortDownloadMessage(List<KKFileMessage> list) {
        List<KKFileMessage> result = new ArrayList<>();
        if (list == null || list.size() == 0) return result;
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            KKFileMessage kkVideoMessage = list.get(i);
            if (kkVideoMessage.getId() != downloadId) {
                result.add(kkVideoMessage);
            } else {
                index = i;
            }
        }
        if (index != -1) {
            result.add(0, videos.get(index));
        }
        return result;
    }

    private int getDownloadingCount() {
        List<KKFileMessage> result = new ArrayList<>();
        for (KKFileMessage kkVideoMessage : videoRvAdapter.getData()) {
            if (!kkVideoMessage.isDateMessage() && KKFileDownloadStatus.Status.DOWNLOADING == kkVideoMessage.getDownloadStatus().getStatus()) {
                result.add(kkVideoMessage);
            }
        }
        return result.size();
    }

    @Override
    public void onLocalVideoFilesUpdate(List<KKFileMessage> videoMessages, String messageFlag) {
        if (!"save_album".equals(messageFlag)) return;
        this.videos = videoMessages;
        List<KKFileMessage> downloadingList = videos;
        //List<KKFileMessage> temp = sortDownloadMessage(downloading);
        AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.finishRefresh();
            if (downloadingList.size() == 0) {
                nullLayout.setVisibility(View.VISIBLE);
                downdingRv.setVisibility(View.GONE);
                videoRvAdapter.setList(null);
            } else {
                nullLayout.setVisibility(View.GONE);
                downdingRv.setVisibility(View.VISIBLE);
                videoRvAdapter.setList(downloadingList);
            }
        });
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        AndroidUtilities.runOnUIThread(() -> {
            videoRvAdapter.notifyItemStatusChanged(fileName);
            int count = getDownloadingCount();
            if (count > 0) {
                tvWifiSpeed.setVisibility(View.VISIBLE);
                setNetSpeed();
            } else {
                tvWifiSpeed.setVisibility(View.GONE);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBus(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DELETE_VIDEO_OK:
                ManualDownloadFileManager.getInstance().checkUpdate(true, "save_album");
                break;
        }
    }
}
