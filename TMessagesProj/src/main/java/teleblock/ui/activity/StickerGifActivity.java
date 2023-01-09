package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ConvertUtils;
import com.ruffian.library.widget.RLinearLayout;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityStickerGifBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import teleblock.ui.adapter.StickerGfiPage2Adapter;
import teleblock.widget.MCommonPagerTitleView;

public class StickerGifActivity extends BaseFragment {
    ActivityStickerGifBinding binding;
    CommonNavigator commonNavigator;
    String[] tabs = new String[]{
            LocaleController.getString("sticker_tab_gif", R.string.sticker_tab_gif),
            LocaleController.getString("sticker_tab_sticker", R.string.sticker_tab_sticker),
            LocaleController.getString("sticker_tab_collect", R.string.sticker_tab_collect)
    };
    int currentTab;
    StickerGfiPage2Adapter stickerGfiPage2Adapter;
    int position;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    public StickerGifActivity(int position) {
        this.position = position;
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_sticker_gif_title", R.string.ac_sticker_gif_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActivityStickerGifBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        initIndicator();
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                binding.magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                binding.magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                binding.magicIndicator.onPageScrollStateChanged(state);
            }
        });
        //binding.viewPager2.setUserInputEnabled(false);//禁止左右滑动
        binding.viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.viewPager2.setAdapter(stickerGfiPage2Adapter = new StickerGfiPage2Adapter(getParentActivity(), tabs));
        binding.viewPager2.setOffscreenPageLimit(tabs.length);//预加载
        binding.viewPager2.setCurrentItem(position);
    }

    private void initIndicator() {
        //MagicIndicator
        commonNavigator = new CommonNavigator(getParentActivity());
        //commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                MCommonPagerTitleView commonPagerTitleView = new MCommonPagerTitleView(context);
                RLinearLayout itemContentView = (RLinearLayout) LayoutInflater.from(context).inflate(R.layout.view_sticker_tab_item, null);
                TextView textView = itemContentView.findViewById(R.id.tv_title);
                textView.setText(tabs[index]);
                commonPagerTitleView.setContentView(itemContentView);
                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                    @Override
                    public void onSelected(int index, int totalCount) {
                        if (index == currentTab) return;
                        currentTab = index;
                        itemContentView.getHelper().setBackgroundColorNormal(Theme.getColor(Theme.key_actionBarDefault));
                        textView.setTextColor(Color.parseColor("#ffffff"));
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        itemContentView.getHelper().setBackgroundColorNormal(Color.parseColor("#F7F8F9"));
                        textView.setTextColor(Color.parseColor("#56565C"));
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
                        //渐变色
                        //textView.setTextColor(ArgbEvaluatorHolder.eval(leavePercent, textSelectColor, textNormalColor));
                        //textNumber.setTextColor(ArgbEvaluatorHolder.eval(leavePercent, numberSelectColor, numberNormalColor));
                    }

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
                        //渐变色
                        //textView.setTextColor(ArgbEvaluatorHolder.eval(enterPercent, textNormalColor, textSelectColor));
                        //textNumber.setTextColor(ArgbEvaluatorHolder.eval(enterPercent, numberNormalColor, numberSelectColor));
                    }
                });
                commonPagerTitleView.setOnClickListener(v -> {
                    binding.viewPager2.setCurrentItem(index,false);
                });
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                float navigatorHeight = ConvertUtils.dp2px(40);
                //indicator.setLineHeight(navigatorHeight);
                indicator.setLineHeight(0);
                indicator.setRoundRadius(navigatorHeight / 2);
                indicator.setYOffset(0);
                indicator.setColors(Theme.getColor(Theme.key_actionBarDefault));
                return indicator;
            }
        });
        binding.magicIndicator.setNavigator(commonNavigator);

        //item间隔 must after setNavigator
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleContainer.setDividerPadding(UIUtil.dip2px(getParentActivity(), 10));
        titleContainer.setDividerDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_dialog_tab_splitter));
    }
}
