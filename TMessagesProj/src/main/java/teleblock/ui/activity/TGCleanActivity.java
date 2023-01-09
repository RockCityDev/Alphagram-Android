package teleblock.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.blankj.utilcode.util.BarUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.databinding.ActTgcleanBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StorageDiagramView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ui.CacheSizeData;
import teleblock.util.CacheUtil;
import teleblock.util.TGLog;

/**
 * Time:2022/7/5
 * Author:Perry
 * Description：缓存清理页面
 */
public class TGCleanActivity extends BaseFragment {
    private Context context;

    private ActTgcleanBinding binding;

    private long cacheSize = -1;
    private long documentsSize = -1;
    private long audioSize = -1;
    private long musicSize = -1;
    private long photoSize = -1;
    private long videoSize = -1;
    private long stickersSize = -1;

    private int checkBoxCellId = 10000;
    private boolean refresh = false;

    private StorageDiagramView.ClearViewData[] clearViewData = new StorageDiagramView.ClearViewData[7];
    private long totalSize = 0L;

    private List<CacheSizeData> cacheSizeData = new ArrayList<>();

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refresh) {
            refresh = false;
            calcSize();
            initView();
        }
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_close_white);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_title_storage_clean", R.string.ac_title_storage_clean));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActTgcleanBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        calcSize();
        initView();
        return fragmentView;
    }

    private void initView() {
        binding.flStorageDiagram.removeAllViews();
        binding.llAddCalc.removeAllViews();
        binding.flClear.removeAllViews();

        //可视化的存储占用视图
        StorageDiagramView circleDiagramView = new StorageDiagramView(context, true);
        binding.flStorageDiagram.addView(circleDiagramView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 16));
        //列表循环
        CheckBoxCell lastCreatedCheckbox = null;
        for (int i = 0; i < cacheSizeData.size(); i++) {
            clearViewData[i] = new StorageDiagramView.ClearViewData(circleDiagramView);
            clearViewData[i].size = cacheSizeData.get(i).getSize();
            clearViewData[i].color = cacheSizeData.get(i).getColor();
            CheckBoxCell checkBoxCell = new CheckBoxCell(context, true);
            lastCreatedCheckbox = checkBoxCell;
            checkBoxCell.setTag(i);
            checkBoxCell.setId(checkBoxCellId + i);
            checkBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            binding.llAddCalc.addView(checkBoxCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
            checkBoxCell.setText(cacheSizeData.get(i).getName(), AndroidUtilities.formatFileSize(cacheSizeData.get(i).getSize()), true, true);
            checkBoxCell.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            checkBoxCell.setCheckBoxColor(cacheSizeData.get(i).getColor(), Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_checkboxCheck);
            clearViewData[i].setClear(cacheSizeData.get(i).isIfClChecked());
            checkBoxCell.setChecked(cacheSizeData.get(i).isIfClChecked(), true);
            int finalI = i;
            checkBoxCell.setOnClickListener(v -> {
                boolean ifChecked = cacheSizeData.get(finalI).isIfClChecked();
                int enabledCount = 0;
                for (int a = 0; a < clearViewData.length; a++) {
                    if (clearViewData[a] != null && clearViewData[a].clear) {
                        enabledCount++;
                    }
                }
                CheckBoxCell cell = (CheckBoxCell) v;
                int num = (Integer) cell.getTag();
                if (enabledCount == 1 && clearViewData[num].clear) {
                    AndroidUtilities.shakeView(((CheckBoxCell) v).getCheckBoxView());
                    return;
                }
                cacheSizeData.get(finalI).setIfClChecked(!ifChecked);
                clearViewData[num].setClear(!ifChecked);
                cell.setChecked(!ifChecked, true);
            });

            final int index = i;
            checkBoxCell.getRightImageView().setOnClickListener(View -> {
                int type = 1;
                if (index == 0 || index == 5) {
                    type = 1;
                } else if (index == 1) {
                    type = 2;
                } else if (index == 2) {
                    type = 3;
                } else if (index == 3 || index == 4) {
                    type = 4;
                } else if (index == 6) {
                    type = 5;
                }
                context.startActivity(new Intent(context, CacheCleanDetailActivity.class).putExtra("type", type));
            });
        }

        if (lastCreatedCheckbox != null) {
            lastCreatedCheckbox.setNeedDivider(false);
        }

        circleDiagramView.setData(clearViewData);
        BottomSheet.BottomSheetCell cell = new BottomSheet.BottomSheetCell(getParentActivity(), 2, context.getResources().getColor(R.color.theme_color));
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndIcon(LocaleController.getString("ClearMediaCache", R.string.ClearMediaCache), 0);
        cell.getTextView().setOnClickListener(v -> {//清除缓存
            cleanupFolders();
        });
        binding.flClear.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));

        AndroidUtilities.runOnUIThread(() -> {
            if (clearViewData.length > 0) {
                for (int i = 0; i < clearViewData.length; i++) {
                    if (clearViewData[i] != null) {
                        clearViewData[i].forceRefresh();//刷新下顶部界面
                        break;
                    }
                }
            }
        }, 200);

    }

    /**
     * 计算缓存大小
     */
    private void calcSize() {
        photoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), 0);
        videoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), 0);
        documentsSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 1);
        musicSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 2);
        audioSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), 0);
        stickersSize = CacheUtil.getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), 0);
        cacheSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), 0);

        if (cacheSizeData.isEmpty()) {
            cacheSizeData.add(new CacheSizeData(photoSize, LocaleController.getString("LocalPhotoCache", R.string.LocalPhotoCache), Theme.key_statisticChartLine_blue, FileLoader.MEDIA_DIR_IMAGE, true));
            cacheSizeData.add(new CacheSizeData(videoSize, LocaleController.getString("LocalVideoCache", R.string.LocalVideoCache), Theme.key_statisticChartLine_golden, FileLoader.MEDIA_DIR_VIDEO, true));
            cacheSizeData.add(new CacheSizeData(documentsSize, LocaleController.getString("LocalDocumentCache", R.string.LocalDocumentCache), Theme.key_statisticChartLine_green, FileLoader.MEDIA_DIR_DOCUMENT, true));
            cacheSizeData.add(new CacheSizeData(musicSize, LocaleController.getString("LocalMusicCache", R.string.LocalMusicCache), Theme.key_statisticChartLine_indigo, FileLoader.MEDIA_DIR_DOCUMENT, true));
            cacheSizeData.add(new CacheSizeData(audioSize, LocaleController.getString("LocalAudioCache", R.string.LocalAudioCache), Theme.key_statisticChartLine_red, FileLoader.MEDIA_DIR_AUDIO, true));
            cacheSizeData.add(new CacheSizeData(stickersSize, LocaleController.getString("AnimatedStickers", R.string.AnimatedStickers), Theme.key_statisticChartLine_lightgreen, FileLoader.MEDIA_DIR_CACHE, true));
            cacheSizeData.add(new CacheSizeData(cacheSize, LocaleController.getString("LocalCache", R.string.LocalCache), Theme.key_statisticChartLine_lightblue, FileLoader.MEDIA_DIR_CACHE, true));
        } else {
            cacheSizeData.get(0).setSize(photoSize);
            cacheSizeData.get(1).setSize(videoSize);
            cacheSizeData.get(2).setSize(documentsSize);
            cacheSizeData.get(3).setSize(musicSize);
            cacheSizeData.get(4).setSize(audioSize);
            cacheSizeData.get(5).setSize(stickersSize);
            cacheSizeData.get(6).setSize(cacheSize);
        }
    }

    private void cleanupFolders() {
        AlertDialog progressDialog = new AlertDialog(context, 3);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        AlertDialog finalProgressDialog = progressDialog;

        Utilities.globalQueue.postRunnable(() -> {
            boolean imagesCleared = false;
            long clearedSize = 0;
            for (int a = 0; a < cacheSizeData.size(); a++) {
                if (cacheSizeData.get(a).isIfClChecked()) {
                    int type = -1;
                    int documentsMusicType = 0;
                    if (a == 5) {
                        type = 100;
                    } else {
                        type = cacheSizeData.get(a).getType();
                    }

                    if (a == 2) {
                        documentsMusicType = 1;
                    }

                    if (a == 3) {
                        documentsMusicType = 2;
                    }

                    clearedSize += cacheSizeData.get(a).getSize();
                    File file;
                    if (type == 100) {
                        file = new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache");
                    } else {
                        file = FileLoader.checkDirectory(type);
                    }
                    if (file != null) {
                        Utilities.clearDir(file.getAbsolutePath(), documentsMusicType, Long.MAX_VALUE, false);
                    }
                    if (type == FileLoader.MEDIA_DIR_CACHE) {
                        cacheSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), documentsMusicType);
                        imagesCleared = true;
                    } else if (type == FileLoader.MEDIA_DIR_AUDIO) {
                        audioSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), documentsMusicType);
                    } else if (type == FileLoader.MEDIA_DIR_DOCUMENT) {
                        if (documentsMusicType == 1) {
                            documentsSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), documentsMusicType);
                        } else {
                            musicSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), documentsMusicType);
                        }
                    } else if (type == FileLoader.MEDIA_DIR_IMAGE) {
                        imagesCleared = true;
                        photoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), documentsMusicType);
                    } else if (type == FileLoader.MEDIA_DIR_VIDEO) {
                        videoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), documentsMusicType);
                    } else if (type == 100) {
                        imagesCleared = true;
                        stickersSize = CacheUtil.getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), documentsMusicType);
                    }
                }
            }

            final boolean imagesClearedFinal = imagesCleared;

            long finalClearedSize = clearedSize;
            AndroidUtilities.runOnUIThread(() -> {
                if (imagesClearedFinal) {
                    ImageLoader.getInstance().clearMemory();
                }
                if (finalProgressDialog.isShowing()) {
                    finalProgressDialog.dismiss();
                }
                calcSize();
                initView();
                Toast.makeText(context, LocaleController.formatString("CacheWasCleared", R.string.CacheWasCleared, AndroidUtilities.formatFileSize(finalClearedSize)), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTGCleanOkEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.CLEAR_CACHE_OK:
                if (event.getData() instanceof Boolean) {
                    if ((Boolean) event.getData()) {
                        refresh = true;
                    }
                }
                break;
        }
    }


}
