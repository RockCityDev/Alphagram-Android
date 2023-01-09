package teleblock.ui.activity;

import android.os.Bundle;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityTgcleanDetailBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.TGCacheManager;
import teleblock.model.TGCacheEntity;
import teleblock.model.TGCacheFindEntity;
import teleblock.ui.adapter.TGCleanContainerPageAdapter;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.util.TGLog;
import teleblock.util.ViewUtil;


/**
 * Time:2022/7/5
 * Author:Perry
 * Description：缓存清理单文件详情页面
 */
public class CacheCleanDetailActivity extends BaseActivity {

    private ActivityTgcleanDetailBinding binding;

    private Map<Integer, TGCacheFindEntity> tempMap;
    private TGCleanContainerPageAdapter tgCleanContainerPageAdapter;
    private boolean isEdit = false;
    private int currentPosition = -1;

    private int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityTgcleanDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        BarUtils.setStatusBarColor(mActivity, getResources().getColor(R.color.grey));
//        BarUtils.setStatusBarLightMode(mActivity, false);

        type = getIntent().getExtras().getInt("type");
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setView() {
        binding.ivBack.setOnClickListener(view -> finish());
        binding.tvTitle.setText(LocaleController.getString("ac_tgclean_title_image",R.string.ac_tgclean_title_image));
        binding.ivEdit.setOnClickListener(view -> {
            isEdit = !isEdit;
            tgCleanContainerPageAdapter.notifyEditChange(binding.viewPager2.getCurrentItem(), isEdit);
        });

        String[] tabs = LocaleController.getString("array_tgclean_type_tables", R.string.array_tgclean_type_tables).split("\\|");
        List<String> tabList = Arrays.asList(tabs);

        ViewUtil.vbBindMitab(mActivity, binding.magicIndicator, tabList, binding.viewPager2, position -> {
            if (currentPosition != -1) {
                tgCleanContainerPageAdapter.notifyEditChange(currentPosition, false);
            }
            currentPosition = position;
            binding.tvTitle.setText(tabList.get(position));
        });

        tgCleanContainerPageAdapter = new TGCleanContainerPageAdapter((FragmentActivity) mActivity, tabs, tempMap);
        binding.viewPager2.setAdapter(tgCleanContainerPageAdapter);
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        LoadingDialog dialog = new LoadingDialog(mActivity, LocaleController.getString("ac_tgclean_loading_text",R.string.ac_tgclean_loading_text));
        dialog.show();
        new TGCacheManager().scanFile(new TGCacheManager.TGCacheListener() {
            @Override
            public void onFind(TGCacheEntity tgCacheEntity) {
            }

            @Override
            public void onFinish(Map<Integer, TGCacheFindEntity> cacheMap) {
                long duration = System.currentTimeMillis() - start;
                if (duration < 1000) {
                    AndroidUtilities.runOnUIThread(() -> {
                        dialog.dismiss();
                        setDataToView(cacheMap);
                    }, 1000 - duration);
                } else {
                    dialog.dismiss();
                    setDataToView(cacheMap);
                }
            }
        });
    }

    private void setDataToView(Map<Integer, TGCacheFindEntity> cacheMap) {
        tempMap = cacheMap;
        setView();
        if (type > -1) {
            binding.viewPager2.postDelayed(() -> {
                binding.viewPager2.setCurrentItem(type - 1);
            }, 20);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTGCleanOkEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.CLEAR_CACHE_OK:
                isEdit = false;
                tgCleanContainerPageAdapter.notifyEditChange(binding.viewPager2.getCurrentItem(), isEdit);
                break;
        }
    }
}
