package teleblock.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.DeleteVideoItemEvent;
import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageManager;
import teleblock.file.KKFileTypes;
import teleblock.file.KKLocalFileManager;
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
 * Desc
 */
public class MyLocalCacheFragment extends BaseFragment implements KKLocalFileManager.Listener<KKFileMessage>, KKVideoDownloadListener {
    private SmartRefreshLayout refreshLayout;
    private RecyclerView videoRv;
    private LinearLayout nullLayout;
    private TextView tvWifiSpeed;
    private TextView tvEmpty;
    private LinearLayout bottomLayout;
    private RelativeLayout rlDeleteALL;
    private TextView tvSaveToGallery;
    private TextView tvDoDelete;
    private TextView tvDoShare;

    private MyDownloadIngRvAdapter videoRvAdapter;
    List<KKFileMessage> videos;

    public static MyLocalCacheFragment instance() {
        MyLocalCacheFragment fragment = new MyLocalCacheFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_mylocal_cache, null);
    }

    @Override
    protected void onViewCreated() {
        EventBus.getDefault().register(this);
        initView();

        //添加监听下载中的文件
        KKLocalFileManager.getInstance().addLocalVideoFilesListener(this, this);
        KKLocalFileManager.getInstance().refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }



    private void initView() {
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        nullLayout = rootView.findViewById(R.id.null_layout);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        tvWifiSpeed = rootView.findViewById(R.id.tv_wifi_speed);
        videoRv = rootView.findViewById(R.id.local_cache_rv);
        bottomLayout = rootView.findViewById(R.id.bottom_layout);
        rlDeleteALL = rootView.findViewById(R.id.rl_delete_all);
        tvSaveToGallery = rootView.findViewById(R.id.tv_save_to_gallery);
        tvDoDelete = rootView.findViewById(R.id.tv_do_delete);
        tvDoShare = rootView.findViewById(R.id.tv_do_share);

        tvEmpty.setText(LocaleController.getString("ac_downing_null_tips", R.string.ac_downing_null_tips));
        tvSaveToGallery.setText(LocaleController.getString("ac_download_text_save_gallery", R.string.ac_download_text_save_gallery));
        tvDoDelete.setText(LocaleController.getString("ac_download_text_delete", R.string.ac_download_text_delete));
        tvDoShare.setText(LocaleController.getString("ac_download_text_share", R.string.ac_download_text_share));
        ((TextView) rootView.findViewById(R.id.tv_delete_all)).setText(LocaleController.getString("tv_delete_all", R.string.tv_delete_all));

        refreshLayout.setOnRefreshListener(refreshLayout -> {
            KKLocalFileManager.getInstance().refresh();
        });
        refreshLayout.setEnableLoadMore(false);
        videoRv.setItemAnimator(null);
        videoRv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        videoRv.setAdapter(videoRvAdapter = new MyDownloadIngRvAdapter(mActivity));
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
                ((MyLocalContainerFragment) getParentFragment()).setSelectText(videoRvAdapter.selectList.size(), videoRvAdapter.selectList.size() == videoRvAdapter.getItemCount());
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
                        startActivity(new Intent(mActivity, VideoPlayActivity.class).putExtra("position", p_position));
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
            MyLocalContainerFragment myLocalContainerFragment = (MyLocalContainerFragment) getParentFragment();
            myLocalContainerFragment.ivEdit.performClick();
            return true;
        });

        //删除
        tvDoDelete.setOnClickListener(view -> {
            if (videoRvAdapter.selectList.size() > 0) {
                for (String fileName : videoRvAdapter.selectList) {
                    KKFileMessage message = videoRvAdapter.getMessageByFileName(fileName);
                    KKVideoDataManager.getInstance().removeLocalFile(fileName);
                    if (message != null && message.getDownloadStatus() != null) {
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_ITEM, new DeleteVideoItemEvent(message.getDownloadStatus().getVideoFile().toString())));
                    }
                }
                videoRvAdapter.selectList.clear();
                Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
            }
        });

        //删除全部
        rlDeleteALL.setOnClickListener(view -> {
            deleteAllData();
        });

        //保存到相册
        tvSaveToGallery.setOnClickListener(view -> {
            if (videoRvAdapter.selectList == null || videoRvAdapter.selectList.size() == 0) return;
            List<KKFileMessage> messageList = new ArrayList<>();
            for (String fileName : videoRvAdapter.selectList) {
                KKFileMessage message = videoRvAdapter.getMessageByFileName(fileName);
                if (message != null && message.getDownloadStatus() != null && message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                    messageList.add(message);
                }
            }
            if (messageList.size() == 0) {
                videoRvAdapter.selectList.clear();
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
                Toast.makeText(mActivity, LocaleController.getString("tv_download_file_not_finish", R.string.tv_download_file_not_finish), Toast.LENGTH_LONG).show();
                return;
            }
            final boolean[] showToast = {false};
            for (KKFileMessage message : messageList) {
                MediaController.saveFile(message.getDownloadStatus().getVideoFile().toString(), ApplicationLoader.applicationContext, 1, null, null, new Runnable() {
                    @Override
                    public void run() {
                        if (showToast[0]) return;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SAVE_GALLERY);
                        showToast[0] = true;
                    }
                });
            }
            videoRvAdapter.selectList.clear();
            EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
        });

        //分享
        tvDoShare.setOnClickListener(view -> {
            if (videoRvAdapter.selectList == null || videoRvAdapter.selectList.size() == 0) return;
            List<KKFileMessage> messageList = new ArrayList<>();
            for (String fileName : videoRvAdapter.selectList) {
                KKFileMessage message = videoRvAdapter.getMessageByFileName(fileName);
                if (message != null && message.getDownloadStatus() != null && message.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                    messageList.add(message);
                }
            }
            if (messageList.size() == 0) {
                videoRvAdapter.selectList.clear();
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
                Toast.makeText(mActivity, LocaleController.getString("tv_download_file_not_finish", R.string.tv_download_file_not_finish), Toast.LENGTH_LONG).show();
                return;
            }
            ShareUtil.shareVideo2(mActivity, messageList.get(0).getDownloadStatus().getVideoFile().toString());
        });
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
            bottomLayout.setVisibility(videoRvAdapter.getData().isEmpty() ? View.GONE : View.VISIBLE);
            rlDeleteALL.setVisibility(View.GONE);
            refreshLayout.setEnableRefresh(false);
        } else {
            bottomLayout.setVisibility(View.GONE);
            rlDeleteALL.setVisibility(videoRvAdapter.getData().isEmpty() ? View.GONE : View.VISIBLE);
            refreshLayout.setEnableRefresh(true);
            checkAllOrUnCheckAll(false);
        }
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
        ((MyLocalContainerFragment) getParentFragment()).setSelectText(videoRvAdapter.selectList.size(), checkAll);
    }

    //DOWNLOADING个数
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
        this.videos = videoMessages;
        AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.finishRefresh();
            if (videoMessages.size() == 0) {
                nullLayout.setVisibility(View.VISIBLE);
                rlDeleteALL.setVisibility(View.GONE);
                videoRv.setVisibility(View.GONE);
                videoRvAdapter.setList(null);
            } else {
                nullLayout.setVisibility(View.GONE);
                rlDeleteALL.setVisibility(View.VISIBLE);
                videoRv.setVisibility(View.VISIBLE);
                videoRvAdapter.setList(videoMessages);
            }
        });
    }

    private void deleteAllData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(LocaleController.getString("tv_delete_dialog_title", R.string.tv_delete_dialog_title));
        builder.setMessage(LocaleController.getString("tv_delete_dialog_content", R.string.tv_delete_dialog_content));
        builder.setPositiveButton(LocaleController.getString("tv_delete_dialog_positive_button", R.string.tv_delete_dialog_positive_button), (dialogInterface, i) -> {
            final AlertDialog progressDialog = new AlertDialog(mActivity, 3);
            progressDialog.show();
            new Thread(() -> {
                List<KKFileMessage> dataList = new ArrayList<>(videoRvAdapter.getData());
                for (KKFileMessage kkFileMessage : dataList) {
                    KKFileMessage message = videoRvAdapter.getMessageByFileName(kkFileMessage.getDownloadFileName());
                    KKVideoDataManager.getInstance().removeLocalFile(kkFileMessage.getDownloadFileName());
                    if (message != null && message.getDownloadStatus() != null) {
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_ITEM, new DeleteVideoItemEvent(message.getDownloadStatus().getVideoFile().toString())));
                    }
                }
                AndroidUtilities.runOnUIThread(() -> {
                    Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
                    progressDialog.dismiss();
                });
            }).start();
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
        }
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
                KKLocalFileManager.getInstance().refresh();
                break;
        }
    }

}
