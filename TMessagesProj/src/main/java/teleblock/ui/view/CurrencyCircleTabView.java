package teleblock.ui.view;

import static teleblock.util.ViewUtil.vbBindMiTabListener;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

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
import org.telegram.messenger.databinding.CurrencyCircleTitleBinding;
import org.telegram.messenger.databinding.ViewTabCurrencyCircleBinding;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.ui.adapter.TgFragmentVp2Adapter;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.ui.fragment.BaseFragment;
import teleblock.util.MMKVUtil;
import teleblock.util.ViewUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/10/11
 * Author:Perry
 * Description：币圈社群页面
 */
public class CurrencyCircleTabView extends FrameLayout {

    private ViewTabCurrencyCircleBinding binding;
    private DialogsActivity parentFragment;

    //存储子页面的集合
    private List<BaseFragment> pageFragmentView = new ArrayList<>();
    //vp2适配器
    private TgFragmentVp2Adapter mTgFragmentVp2Adapter;

    //所有的链数据
    private List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> chainData;

    //加载窗
    private LoadingDialog loadingDialog;

    //打开次数
    private int openCount = 0;

    public CurrencyCircleTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        parentFragment = dialogsActivity;
        initView();
        setVisibility(GONE);
    }

    public void initData() {
        if (parentFragment == null) {
            return;
        }

        if (openCount == 0) {
            loadingDialog.show();
            FragmentTransaction ft = parentFragment.getParentActivity().getSupportFragmentManager().beginTransaction();
            if (pageFragmentView.size() > 0) {
                for (Fragment fragment : pageFragmentView) {
                    ft.remove(fragment);
                }

                ft.commitNow();
                pageFragmentView.clear();
                mTgFragmentVp2Adapter.notifyDataSetChanged();
            }

            //获取钱包配置数据
            WalletUtil.requestWalletNetworkConfigData(() -> {
                WalletNetworkConfigEntity walletNetworkConfig = MMKVUtil.getWalletNetworkConfigEntity();
                //获取链数据
                chainData = walletNetworkConfig.getChainType();

                //循环创建子view
                for (WalletNetworkConfigEntity.WalletNetworkConfigChainType chainItemData : chainData) {
                    CurrencyChildPageView childFragment = new CurrencyChildPageView();
                    childFragment.setParentFragment(parentFragment);
                    childFragment.setChainData(chainItemData);
                    pageFragmentView.add(childFragment);
                }

                //初始化适配器
                mTgFragmentVp2Adapter = new TgFragmentVp2Adapter(parentFragment.getParentActivity(), pageFragmentView);
                binding.viewPager2.setAdapter(mTgFragmentVp2Adapter);

                //vb绑定mitab
                vbBindMitabStyle(binding.magical, chainData, binding.viewPager2, position -> {});

                loadingDialog.dismiss();
                openCount++;
            });
        }
    }

    private void initView() {
        setOnClickListener(view -> {});
        binding = ViewTabCurrencyCircleBinding.inflate(LayoutInflater.from(parentFragment.getParentActivity()), this, true);
        binding.ll.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        loadingDialog = new LoadingDialog(getContext(), LocaleController.getString("Loading", R.string.Loading));
    }

    /**
     * vb绑定mitab
     * @param magicIndicator
     * @param chainData
     * @param viewPager2
     * @param mVPOnPageChangeCallback
     */
    private void vbBindMitabStyle(
            MagicIndicator magicIndicator,
            List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> chainData,
            ViewPager2 viewPager2,
            ViewUtil.VPOnPageChangeCallback mVPOnPageChangeCallback
    ) {
        CommonNavigator commonNavigator = new CommonNavigator(parentFragment.getParentActivity());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return chainData.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                CurrencyCircleTitleBinding binding = CurrencyCircleTitleBinding.inflate(LayoutInflater.from(context));
                commonPagerTitleView.setContentView(binding.getRoot());
                int padding = SizeUtils.dp2px(10);
                commonPagerTitleView.setPadding(padding, 0, padding, 0);

                //标题名称
                binding.tvTitle.setText(chainData.get(index).getName());
                //标题图标
                GlideHelper.getDrawableGlide(context, chainData.get(index).getIcon(), drawable -> {
                    binding.tvTitle.getHelper().setIconNormalLeft(drawable);
                });

                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                    @Override
                    public void onSelected(int index, int totalCount) {
                        binding.tvTitle.getHelper().setTextColorNormal(Color.parseColor("#4B5BFF"));
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        binding.tvTitle.getHelper().setTextColorNormal(Color.parseColor("#4F4F4F"));
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {}

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {}
                });

                commonPagerTitleView.setOnClickListener(v -> viewPager2.setCurrentItem(index));
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setLineHeight(SizeUtils.dp2px(3));
                linePagerIndicator.setColors(Color.parseColor("#4B5BFF"));
                return linePagerIndicator;
            }
        });

        magicIndicator.setNavigator(commonNavigator);
        vbBindMiTabListener(magicIndicator, viewPager2, mVPOnPageChangeCallback);
    }

}
