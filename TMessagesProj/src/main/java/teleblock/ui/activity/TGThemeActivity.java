package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.yalantis.ucrop.util.DensityUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.ThemePreviewActivity;

import java.io.File;
import java.util.Arrays;

import teleblock.event.data.ThemePreviewEvent;
import teleblock.ui.adapter.ThemePageAdapter;
import teleblock.util.DrawableColorChange;
import teleblock.util.EventUtil;

/**
 * Created by LSD on 2021/9/8.
 * Desc
 */
public class TGThemeActivity extends BaseFragment {
    int tabIndex = 0;
    View rootView;
    Context context;

    MagicIndicator magicIndicator;
    ViewPager2 viewPager2;
    ThemePageAdapter themePageAdapter;
    ImageView ivVideoPaper;
    AnimationDrawable animVideo;

    boolean newEvent;
    boolean liveEvent;

    public TGThemeActivity() {
    }

    public TGThemeActivity(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
        if (animVideo != null) animVideo.stop();
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.parseColor("#151C29"),true);
        this.context = context;
        actionBar.setItemsColor(0, false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_title_theme", R.string.ac_title_theme));
        actionBar.setBackgroundColor(Color.parseColor("#232C3D"));
        actionBar.setTitleColor(Color.parseColor("#ffffff"));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        actionBar.setItemsBackgroundColor(Color.parseColor("#00000000"), false);

        if (rootView == null) {
            EventUtil.track(context, EventUtil.Even.主题页面热门展示, new ArrayMap<>());
            rootView = LayoutInflater.from(context).inflate(R.layout.activity_theme, null);
            initView();
        }
        return rootView;
    }

    private void initView() {
        TextView tv_title = rootView.findViewById(R.id.tv_title);
        tv_title.setText(LocaleController.getString("ac_title_theme", R.string.ac_title_theme));
        viewPager2 = rootView.findViewById(R.id.viewPager2);
        magicIndicator = rootView.findViewById(R.id.magic_indicator);
        rootView.findViewById(R.id.iv_theme_entry).setOnClickListener(view -> {
            EventUtil.track(context, EventUtil.Even.自定义主题入口按钮点击, new ArrayMap<>());
            presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
        });
        (ivVideoPaper = rootView.findViewById(R.id.iv_video_wallpaper)).setOnClickListener(view -> {
            EventUtil.track(context, EventUtil.Even.主题页动态主题入口按钮点击, new ArrayMap<>());
            //presentFragment(new TGVideoWallpaperActivity());
        });
        ivVideoPaper.setBackgroundResource(R.drawable.ic_video_wallpaper_anim);
        animVideo = (AnimationDrawable) ivVideoPaper.getBackground();
        if (animVideo != null) animVideo.start();

        //初始化
        //magicIndicator.setBackgroundColor(Color.parseColor("#ffffff"));
        String[] tabs = LocaleController.getString("array_theme_tables", R.string.array_theme_tables).split("\\|");//tabs
        CommonNavigator commonNavigator = new CommonNavigator(context);
        //commonNavigator.setAdjustMode(true);//水平等分
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                View customLayout = LayoutInflater.from(context).inflate(R.layout.simple_pager_video_theme, null);
                ImageView iv_tab_image = customLayout.findViewById(R.id.iv_tab_image);
                TextView tv_tab_text = customLayout.findViewById(R.id.tv_tab_text);
                if (index == 0) {
                    iv_tab_image.setVisibility(View.VISIBLE);
                    tv_tab_text.setVisibility(View.GONE);
                    iv_tab_image.setImageResource(R.drawable.ic_tab_hot);
                } else if (index == 1) {
                    iv_tab_image.setVisibility(View.VISIBLE);
                    tv_tab_text.setVisibility(View.GONE);
                    iv_tab_image.setImageResource(R.drawable.ic_tab_new);
                } else if (index == 2) {
                    iv_tab_image.setVisibility(View.GONE);
                    tv_tab_text.setVisibility(View.VISIBLE);
                    tv_tab_text.setText(tabs[2]);
                }
                commonPagerTitleView.setContentView(customLayout);
                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {

                    @Override
                    public void onSelected(int index, int totalCount) {
                        DrawableColorChange drawableColorChange = new DrawableColorChange(context);
                        if (index == 0) {
                            iv_tab_image.setImageDrawable(drawableColorChange.changeColorById(R.drawable.ic_tab_hot, R.color.tg_main_color));
                        } else if (index == 1) {
                            if (!newEvent) {
                                newEvent = true;
                                EventUtil.track(context, EventUtil.Even.主题页面最新展示, new ArrayMap<>());
                            }
                            iv_tab_image.setImageDrawable(drawableColorChange.changeColorById(R.drawable.ic_tab_new, R.color.tg_main_color));
                        } else if (index == 2) {
                            if (!liveEvent) {
                                liveEvent = true;
                                EventUtil.track(context, EventUtil.Even.主题页面动态主题展示, new ArrayMap<>());
                            }
                            tv_tab_text.setTextColor(context.getResources().getColor(R.color.tg_main_color));
                        }
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        DrawableColorChange drawableColorChange = new DrawableColorChange(context);
                        if (index == 0) {
                            iv_tab_image.setImageDrawable(drawableColorChange.changeColorById(R.drawable.ic_tab_hot, R.color.half_black));
                        } else if (index == 1) {
                            iv_tab_image.setImageDrawable(drawableColorChange.changeColorById(R.drawable.ic_tab_new, R.color.half_black));
                        } else if (index == 2) {
                            tv_tab_text.setTextColor(context.getResources().getColor(R.color.half_black));
                        }
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
                    }

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
                    }
                });
                commonPagerTitleView.setOnClickListener(v -> viewPager2.setCurrentItem(index));
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                linePagerIndicator.setLineHeight(8);
                linePagerIndicator.setLineWidth(DensityUtil.dip2px(context, 37));
                linePagerIndicator.setRoundRadius(4);
                linePagerIndicator.setColors(context.getResources().getColor(R.color.tg_main_color));
                return linePagerIndicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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
        //viewPager2.setUserInputEnabled(false);//禁止左右滑动
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setAdapter(themePageAdapter = new ThemePageAdapter((FragmentActivity) context, Arrays.asList(tabs)));
        if (tabIndex != 0) {
            AndroidUtilities.runOnUIThread(() -> viewPager2.setCurrentItem(tabIndex), 100);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThemePreviewEvent(ThemePreviewEvent event) {
        Theme.ThemeInfo themeInfo = Theme.applyThemeFile(new File(event.path), event.name, null, true);
        if (themeInfo != null) {
            presentFragment(new ThemePreviewActivity(themeInfo));
        }
    }
}
