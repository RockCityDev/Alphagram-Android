package teleblock.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.SizeUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.DeleteVideoItemEvent;
import teleblock.file.KKFileTypes;
import teleblock.manager.FileScanManager;
import teleblock.model.LocalVideoEntity;
import teleblock.model.MiddleData;
import teleblock.ui.activity.VideoPlayActivity;
import teleblock.ui.adapter.MyLocalScanRvAdapter;
import teleblock.util.LocalVideoSort;
import teleblock.util.ShareUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TGLog;


/**
 * Created by LSD on 2021/3/20.
 * Desc 本地扫描的
 */
public class MyLocalScanFragment extends BaseFragment {
    private RecyclerView fileRv;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout nullLayout;
    private LinearLayout bottomLayout;
    private RelativeLayout rlDeleteALL;
    private TextView tvEmpty;
    private TextView tvSaveToGallery;
    private TextView tvDoDelete;
    private TextView tvDoShare;

    private MyLocalScanRvAdapter localFileRvAdapter;
    private List<String> selectList = new ArrayList<>();

    public static MyLocalScanFragment instance() {
        MyLocalScanFragment fragment = new MyLocalScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_mylocal_scan, null);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onViewCreated() {
        EventBus.getDefault().register(this);

        initView();
        loadLocalData();
    }

    private void initView() {
        nullLayout = rootView.findViewById(R.id.null_layout);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        fileRv = rootView.findViewById(R.id.local_scan_rv);
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

        //删除
        tvDoDelete.setOnClickListener(view -> {
            if (selectList.size() > 0) {
                for (String path : selectList) {
                    SystemUtil.deleteFile(path);
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_ITEM, new DeleteVideoItemEvent(path)));
                }
                selectList.clear();
                Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                tvDoDelete.setText(LocaleController.getString("ac_downed_text_delete", R.string.ac_downed_text_delete));
                EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
            }
        });

        //删除全部
        rlDeleteALL.setOnClickListener(view -> {
            deleteAllData();
        });

        //保存到相册
        tvSaveToGallery.setOnClickListener(view -> {
            if (selectList == null || selectList.size() == 0) return;
            final boolean[] showToast = {false};
            for (String path : selectList) {
                MediaController.saveFile(path, ApplicationLoader.applicationContext, 1, null, null, new Runnable() {
                    @Override
                    public void run() {
                        if (showToast[0]) return;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SAVE_GALLERY);
                        showToast[0] = true;
                    }
                });
            }
            selectList.clear();
            EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_OK));
        });

        //分享
        tvDoShare.setOnClickListener(view -> {
            if (selectList.size() > 0) {
                ShareUtil.shareVideo2(mActivity, selectList.get(0));
            }
        });

        //下拉刷新
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadLocalData();
        });
        refreshLayout.setEnableLoadMore(false);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        fileRv.setLayoutManager(layoutManager);
        fileRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = SizeUtils.dp2px(5);
            int halfSpacing = spacing >> 1;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = halfSpacing;
                outRect.right = halfSpacing;
                outRect.bottom = halfSpacing;
                outRect.left = halfSpacing;
            }
        });
        fileRv.setAdapter(localFileRvAdapter = new MyLocalScanRvAdapter(mActivity));
        localFileRvAdapter.getLoadMoreModule().setEnableLoadMore(false);
        localFileRvAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            MyLocalContainerFragment myLocalContainerFragment = (MyLocalContainerFragment) getParentFragment();
            myLocalContainerFragment.ivEdit.performClick();
            return true;
        });
        localFileRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            LocalVideoEntity itemEntity = localFileRvAdapter.getItem(position);
            if (localFileRvAdapter.isDeleteModel()) {//删除模式
                itemEntity.deleteSelect = !itemEntity.deleteSelect;
                if (itemEntity.deleteSelect) {
                    selectList.add(itemEntity.path);
                } else {
                    selectList.remove(itemEntity.path);
                }
                localFileRvAdapter.notifyItemChanged(position);
                ((MyLocalContainerFragment) getParentFragment()).setSelectText(selectList.size(), selectList.size() == localFileRvAdapter.getItemCount());
            } else {//普通模式点击
                if (itemEntity.fileType == KKFileTypes.VIDEO_FILTER || itemEntity.fileType == KKFileTypes.MKV || itemEntity.fileType == KKFileTypes.MOV || itemEntity.fileType == KKFileTypes.MP4) {
                    //媒体
                    int p_position = 0;
                    List<String> dataList = new ArrayList<>();
                    List<LocalVideoEntity> list = localFileRvAdapter.getDownloadedMedia();
                    for (int i = 0; i < list.size(); i++) {
                        LocalVideoEntity temp = list.get(i);
                        dataList.add(temp.path);
                        if (itemEntity.path.equals(temp.path)) {
                            p_position = i;
                        }
                    }
                    if (dataList.size() == 0) return;
                    MiddleData.getInstance().playList = dataList;
                    startActivity(new Intent(mActivity, VideoPlayActivity.class).putExtra("position", p_position));
                } else {//其他
                    try {
                        AndroidUtilities.openForView(new File(itemEntity.path), mActivity);
                    } catch (Exception e) {
                        TGLog.erro(e.getMessage());
                    }
                }
            }
        });
    }

    public void notifyEditChange(boolean isEdit) {
        localFileRvAdapter.setEdit(isEdit);
        if (isEdit) {
            bottomLayout.setVisibility(localFileRvAdapter.getData().isEmpty() ? View.GONE : View.VISIBLE);
            rlDeleteALL.setVisibility(View.GONE);
            refreshLayout.setEnableRefresh(false);
        } else {
            bottomLayout.setVisibility(View.GONE);
            rlDeleteALL.setVisibility(localFileRvAdapter.getData().isEmpty() ? View.GONE : View.VISIBLE);
            refreshLayout.setEnableRefresh(true);
            checkAllOrUnCheckAll(false);
        }
    }

    private void deleteAllData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(LocaleController.getString("tv_delete_dialog_title", R.string.tv_delete_dialog_title));
        builder.setMessage(LocaleController.getString("tv_delete_dialog_content", R.string.tv_delete_dialog_content));
        builder.setPositiveButton(LocaleController.getString("tv_delete_dialog_positive_button", R.string.tv_delete_dialog_positive_button), (dialogInterface, i) -> {
            final AlertDialog progressDialog = new AlertDialog(mActivity, 3);
            progressDialog.show();
            new Thread(() -> {
                List<LocalVideoEntity> dataList = new ArrayList<>(localFileRvAdapter.getData());
                for (LocalVideoEntity entity : dataList) {
                    SystemUtil.deleteFile(entity.path);
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_VIDEO_ITEM, new DeleteVideoItemEvent(entity.path)));
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

    public void checkAllOrUnCheckAll(boolean checkAll) {
        List<LocalVideoEntity> list = localFileRvAdapter.getData();
        selectList.clear();
        for (LocalVideoEntity entity : list) {
            if (checkAll) {
                selectList.add(entity.path);
            }
            entity.deleteSelect = checkAll;
        }
        localFileRvAdapter.notifyDataSetChanged();
        ((MyLocalContainerFragment) getParentFragment()).setSelectText(selectList.size(), checkAll);
    }

    private void loadLocalData() {
        final SparseArray<File> paths = ImageLoader.getInstance().createMediaPaths();
        List<String> dirs = new ArrayList<>();
        if (paths.get(FileLoader.MEDIA_DIR_VIDEO) != null) {
            dirs.add(paths.get(FileLoader.MEDIA_DIR_VIDEO).getAbsolutePath());
        }
        if (paths.get(FileLoader.MEDIA_DIR_DOCUMENT) != null) {
            dirs.add(paths.get(FileLoader.MEDIA_DIR_DOCUMENT).getAbsolutePath());
        }
        if (paths.get(FileLoader.MEDIA_DIR_CACHE) != null) {
            dirs.add(paths.get(FileLoader.MEDIA_DIR_CACHE).getAbsolutePath());
        }
        FileScanManager.scanFile(dirs, new FileScanManager.FileListener() {
            @Override
            public void onFind(List<LocalVideoEntity> list) {
            }

            @Override
            public void onFinish(List<LocalVideoEntity> list) {
                refreshLayout.finishRefresh();
                AndroidUtilities.runOnUIThread(() -> {
                    checkVideo(list);
                });
            }
        });
    }

    private void checkVideo(List<LocalVideoEntity> list) {
        if (list.size() == 0) {
            localFileRvAdapter.setList(null);
            nullLayout.setVisibility(View.VISIBLE);
            rlDeleteALL.setVisibility(View.GONE);
            fileRv.setVisibility(View.GONE);
        } else {
            Collections.sort(list, new LocalVideoSort());//排序
            nullLayout.setVisibility(View.GONE);
            rlDeleteALL.setVisibility(View.VISIBLE);
            fileRv.setVisibility(View.VISIBLE);
            localFileRvAdapter.setList(list);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBus(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DELETE_VIDEO_OK:
                loadLocalData();
                break;
        }
    }
}
