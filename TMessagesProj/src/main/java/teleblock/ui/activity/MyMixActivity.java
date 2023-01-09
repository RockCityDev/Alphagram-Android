package teleblock.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;


import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SizeUtils;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import teleblock.file.KKFileMessage;
import teleblock.file.ManualDownloadFileManager;
import teleblock.ui.adapter.MyMixPageAdapter;
import teleblock.ui.fragment.BaseFragment;
import teleblock.ui.fragment.MyCollectFragment;
import teleblock.ui.fragment.MyDownloadContainerFragment;
import teleblock.ui.fragment.MyLocalContainerFragment;
import teleblock.util.ColorUtil;
import teleblock.util.DrawableColorChange;
import teleblock.util.EventUtil;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDownloadListener;


/**
 * Created by LSD on 2021/5/18.
 * Desc
 */
public class MyMixActivity extends BaseActivity implements ManualDownloadFileManager.Listener<KKFileMessage>, KKVideoDownloadListener {
    MagicIndicator magicIndicator;
    ViewPager2 viewpager2;
    final int[] SELECT_DRAWABLE = new int[]{R.drawable.my_collect1, R.drawable.my_download1, R.drawable.my_local1};
    final int[] UN_SELECT_DRAWABLE = new int[]{R.drawable.my_collect2, R.drawable.my_download2, R.drawable.my_local2};

    String target = "";
    int downloadPageTab;
    int currentItem;

    MyMixPageAdapter myMixPageAdapter;
    boolean page1Event;
    boolean page2Event;
    private List<KKFileMessage> downloadList = new ArrayList<>();
    private List<KKFileMessage> saveAlbumList = new ArrayList<>();

    public static void start(Context context, int currentItem, String target) {
        Intent intent = new Intent(context, MyMixActivity.class);
        intent.putExtra("currentItem", currentItem);
        intent.putExtra("target", target);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mymix);
        //添加监听下载中的文件
        ManualDownloadFileManager.getInstance().addLocalVideoFilesListener(this, this);
        //initTitleBarStyle();

        getExtra();
        initView();
        changePage();
    }

    private void initTitleBarStyle() {
        //背景
        int color = Theme.getColor(Theme.key_actionBarDefault);
        findViewById(R.id.layout_title).setBackgroundColor(color);
        BarUtils.setStatusBarColor(mActivity, ColorUtil.getDarkerColor(color));

        //标题
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));

        //返回
        ImageView ivBack = findViewById(R.id.iv_back);
        DrawableColorChange drawableColorChange = new DrawableColorChange(mActivity);
        ivBack.setImageDrawable(drawableColorChange.changeColorByColor(R.drawable.calls_back, Theme.getColor(Theme.key_actionBarDefaultIcon)));
    }

    private void getExtra() {
        currentItem = getIntent().getIntExtra("currentItem", 0);
        target = getIntent().getStringExtra("target");
        if ("downloading".equals(target)) {
            downloadPageTab = 0;
        } else if ("save_album".equals(target)) {
            downloadPageTab = 1;
        }
    }

    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(view -> onBackPressed());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(LocaleController.getString("title_my", R.string.title_my));
        viewpager2 = findViewById(R.id.viewpager2);
        magicIndicator = findViewById(R.id.magic_indicator);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        String[] tabs = LocaleController.getString("array_my_mix_tables", R.string.array_my_mix_tables).split("\\|");//tabs
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                View customLayout = LayoutInflater.from(context).inflate(R.layout.simple_pager_title_layout, null);
                final ImageView titleImg = customLayout.findViewById(R.id.title_img);
                final TextView titleText = customLayout.findViewById(R.id.title_text);
                titleImg.setImageResource(UN_SELECT_DRAWABLE[index]);//默认
                titleText.setText(tabs[index]);
                commonPagerTitleView.setContentView(customLayout);
                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {

                    @Override
                    public void onSelected(int index, int totalCount) {
                        titleText.setTextColor(Color.parseColor("#FFFFFF"));
                        titleImg.setImageResource(SELECT_DRAWABLE[index]);
                        if (index == 1) {
                            if (!page1Event) {
                                page1Event = true;
                            }
                        } else if (index == 2) {
                            if (!page2Event) {
                                page2Event = true;
                            }
                        }
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        titleText.setTextColor(Color.parseColor("#B3FFFFFF"));
                        titleImg.setImageResource(UN_SELECT_DRAWABLE[index]);
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
                    }

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
                    }
                });
                commonPagerTitleView.setOnClickListener(v -> viewpager2.setCurrentItem(index));
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                linePagerIndicator.setLineHeight(10);
                linePagerIndicator.setLineWidth(SizeUtils.dp2px(60));
                linePagerIndicator.setColors(Theme.getColor(Theme.key_actionBarTabLine));
                return linePagerIndicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
        viewpager2.setUserInputEnabled(false);//禁止左右滑动
        //viewpager2.setOffscreenPageLimit(1);
        viewpager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewpager2.setAdapter(myMixPageAdapter = new MyMixPageAdapter((FragmentActivity) mActivity, Arrays.asList(tabs), downloadPageTab));
    }

    private void changePage() {
        if (currentItem != 0) {
            viewpager2.post(() -> viewpager2.setCurrentItem(currentItem));
        }
    }

    @Override
    public void onLocalVideoFilesUpdate(List<KKFileMessage> videoMessages, String messageFlag) {
        if ("save_album".equals(messageFlag)) {
            saveAlbumList = videoMessages;
        } else if ("user".equals(messageFlag)){
            downloadList = videoMessages;
        }
        AndroidUtilities.runOnUIThread(() -> {
            List<KKFileMessage> result = new ArrayList<>();
            for (KKFileMessage kkVideoMessage : downloadList) {
                if (!kkVideoMessage.isDateMessage() && KKFileDownloadStatus.Status.DOWNLOADED != kkVideoMessage.getDownloadStatus().getStatus()) {
                    result.add(kkVideoMessage);
                }
            }
            for (KKFileMessage kkVideoMessage : saveAlbumList) {
                if (!kkVideoMessage.isDateMessage() && KKFileDownloadStatus.Status.DOWNLOADED != kkVideoMessage.getDownloadStatus().getStatus()) {
                    result.add(kkVideoMessage);
                }
            }
            CommonNavigator commonNavigator = (CommonNavigator) magicIndicator.getNavigator();
            CommonPagerTitleView commonPagerTitleView = (CommonPagerTitleView) commonNavigator.getPagerTitleView(1);
            TextView textView = commonPagerTitleView.findViewById(R.id.view_tips);
            if (textView != null) {
                int size = result.size();
                if (size != 0) {
                    if (size > 99) size = 99;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(size + "");
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
    }

    @Override
    public void onBackPressed() {
        if (canBack()) {
            super.onBackPressed();
        }
    }

    private boolean canBack() {
        BaseFragment baseFragment = myMixPageAdapter.placeHolder.get(viewpager2.getCurrentItem() + "");
        if (baseFragment instanceof MyCollectFragment) {
            MyCollectFragment fragment = (MyCollectFragment) baseFragment;
            if (fragment.edit) {
                fragment.ivEdit.performClick();
                return false;
            }
        } else if (baseFragment instanceof MyDownloadContainerFragment) {
            MyDownloadContainerFragment fragment = (MyDownloadContainerFragment) baseFragment;
            if (fragment.edit) {
                fragment.tvCancel.performClick();
                return false;
            }
        } else if (baseFragment instanceof MyLocalContainerFragment) {
            MyLocalContainerFragment fragment = (MyLocalContainerFragment) baseFragment;
            if (fragment.edit) {
                fragment.tvCancel.performClick();
                return false;
            }
        }
        return true;
    }
}
