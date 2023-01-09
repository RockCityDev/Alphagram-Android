package teleblock.ui.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.LanguageUtils;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.FragmentCommontoolsBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.NewContactActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.config.Constants;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ui.ToolsTabData;
import teleblock.ui.adapter.ToolsTabAdapter;
import teleblock.util.CacheUtil;
import teleblock.util.EventUtil;
import teleblock.util.TGLog;
import teleblock.widget.divider.CustomItemDecoration;

/**
 * Time:2022/7/4
 * Author:Perry
 * Description：常用工具页面
 */
public class CommonToolsActivity extends BaseFragment {

    private FragmentCommontoolsBinding binding;
    //系统工具数据
    private List<ToolsTabData> systemToolsData = new ArrayList<>();
    private ToolsTabAdapter systemToolsTabAdapter;

    //内容推荐数据
    private List<ToolsTabData> contentRecommendData = new ArrayList<>();
    private ToolsTabAdapter contentRecommendToolsTabAdapter;


    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE,true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("home_commontools", R.string.home_commontools));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = FragmentCommontoolsBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        initData();
        initView();
        return fragmentView;
    }

    private void initData() {
        systemToolsData.clear();
        systemToolsData.add(new ToolsTabData(0, LocaleController.getString("commontools_scan_qrcode", R.string.commontools_scan_qrcode), R.drawable.saomiaoerweima, "#46BDFE"));
        systemToolsData.add(new ToolsTabData(1, LocaleController.getString("commontools_add_friend", R.string.commontools_add_friend), R.drawable.tianjiahaoyou, "#3402FD"));
        systemToolsData.add(new ToolsTabData(2, LocaleController.getString("view_home_message_folder", R.string.view_home_message_folder), R.drawable.xiaoxifenzu, "#AB61D9"));

//        systemToolsData.add(new ToolsTabData(3, LocaleController.getString(R.string.commontools_resource_navigation), R.drawable.ziyuandaohang, "#43ABFD"));
//        systemToolsData.add(new ToolsTabData(3, LocaleController.getString(R.string.commontools_official_vipgroup), R.drawable.icon_groups_crypto_good_tools, new int[]{Color.parseColor("#01B4FF"),Color.parseColor("#8836DF")}));
//        systemToolsData.add(new ToolsTabData(4, LocaleController.getString("commontools_group_recommend", R.string.commontools_group_recommend), R.drawable.qunzutuijian, "#2F57D6"));
//        systemToolsData.add(new ToolsTabData(5, LocaleController.getString("commontools_channel_recommend", R.string.commontools_channel_recommend), R.drawable.pingdaotuijian, "#9102FD"));

        if (LanguageUtils.getSystemLanguage().getLanguage().equals("zh")) {
            systemToolsData.add(new ToolsTabData(6, LocaleController.getString("commontools_official_group_ch", R.string.commontools_official_group_ch), R.drawable.icon_official_group_good_tools, R.drawable.btn_official_group_good_tools_chinese));
//            systemToolsData.add(new ToolsTabData(7, LocaleController.getString(R.string.commontools_official_channel_ch), R.drawable.icon_official_channel_good_tools, R.drawable.btn_official_group_good_tools_chinese));
        } else {
            systemToolsData.add(new ToolsTabData(8, LocaleController.getString("commontools_official_group_ex", R.string.commontools_official_group_ex), R.drawable.icon_official_group_good_tools, R.drawable.btn_official_group_good_tools_chinese));
//            systemToolsData.add(new ToolsTabData(9, LocaleController.getString(R.string.commontools_official_channel_ex), R.drawable.icon_official_channel_good_tools, R.drawable.btn_official_group_good_tools_chinese));
        }

//        systemToolsData.add(new ToolsTabData(10, LocaleController.getString("commontools_official_transfertransaction", R.string.commontools_official_transfertransaction), R.drawable.transfer_icon,"#02ABFF"));
        systemToolsTabAdapter = new ToolsTabAdapter(systemToolsData);
    }

    private void initView() {
        binding.clearCache.setText(LocaleController.getString("ac_title_storage_clean", R.string.ac_title_storage_clean));
        binding.localcacheccupied.setText(LocaleController.getString("commontools_local_cache_size_tips", R.string.commontools_local_cache_size_tips));
        binding.tvClearCache.setText(LocaleController.getString("commontools_oneclick_cleanup", R.string.commontools_oneclick_cleanup));

        //显示系统工具适配器
        binding.rvSystemTool.setLayoutManager(new GridLayoutManager(fragmentView.getContext(), 3));
        binding.rvSystemTool.addItemDecoration(new CustomItemDecoration(3, 0f, 30f));
        binding.rvSystemTool.setAdapter(systemToolsTabAdapter);

        //显示内容推荐适配器
        binding.rvContentRecommend.setLayoutManager(new GridLayoutManager(fragmentView.getContext(), 3));
        binding.rvContentRecommend.addItemDecoration(new CustomItemDecoration(3, 0f, 15f));
        binding.rvContentRecommend.setAdapter(contentRecommendToolsTabAdapter);

        //一键清理
        binding.tvClearCache.setOnClickListener(view -> {
            EventUtil.track(getParentActivity(), EventUtil.Even.好工具清理缓存点击, new HashMap<>());
            presentFragment(new TGCleanActivity());
        });

        //系统工具点击事件
        systemToolsTabAdapter.setOnItemClickListener((adapter, view, position) -> {
            int id = systemToolsTabAdapter.getData().get(position).getId();
            switch (id) {
                case 0://扫一扫
                    EventUtil.track(getParentActivity(), EventUtil.Even.好工具扫一扫, new HashMap<>());
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_CAMERA_SCAN));
                    break;
                case 1://邀请好友
                    EventUtil.track(getParentActivity(), EventUtil.Even.好工具邀请好友, new HashMap<>());
                    presentFragment(new NewContactActivity());
                    break;
                case 2://消息分组
                    EventUtil.track(getParentActivity(), EventUtil.Even.好工具消息分组, new HashMap<>());
                    presentFragment(new FiltersSetupActivity());
                    break;
                case 3://币圈社群
                    break;
                case 4://群组推荐
                    presentFragment(new AllRecommendActivity(AllRecommendActivity.GROUP_TYPE));
                    break;
                case 5://频道推荐
                    presentFragment(new AllRecommendActivity(AllRecommendActivity.CHANNEL_TYPE));
                    break;
                case 6:
                case 8://中文群 英文群
                    EventUtil.track(getParentActivity(), EventUtil.Even.好工具官方群, new HashMap<>());
                    Browser.openUrl(getParentActivity(), Constants.getOfficialGroup(), true);
                    break;
                case 7:
                case 9://中文频道，英文频道
                    EventUtil.track(getParentActivity(), EventUtil.Even.好工具官方频道, new HashMap<>());
                    Browser.openUrl(getParentActivity(), Constants.getOfficialChannel(), true);
                    break;

                case 10://转账交易
                    presentFragment(new TransferActivity());
                    break;
            }
        });
    }

    /**
     * 缓存大小
     */
    private void cacheSize() {
        //显示缓存数据
        Utilities.globalQueue.postRunnable(() -> {
            long cacheSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), 0);
            long photoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), 0);
            long videoSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), 0);
            long documentsSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 1);
            long musicSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 2);
            long stickersSize = CacheUtil.getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), 0);
            long audioSize = CacheUtil.getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), 0);
            long totalSize = cacheSize + videoSize + audioSize + photoSize + documentsSize + musicSize + stickersSize;
            AndroidUtilities.runOnUIThread(() -> {
                if (totalSize > 1024 * 1024 * 5) {
                    String size = AndroidUtilities.formatFileSize(totalSize, false);
                    String[] sizeArr = size.split(" ");
                    if (sizeArr.length > 0) {
                        binding.tvCacheSize.setText(sizeArr[0]);
                    }
                    if (sizeArr.length > 1) {
                        binding.tvCacheSizeUnit.setText(sizeArr[1]);
                    }
                } else {
                    binding.tvCacheSize.setText("--");
                    binding.tvCacheSizeUnit.setText("");
                }
            });
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        cacheSize();
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        if (requestCode == ActionIntroActivity.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_CAMERA_SCAN));
            }
        }
    }
}