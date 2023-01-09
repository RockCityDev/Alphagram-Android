package teleblock.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ConvertUtils;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityDeleteMsgGroupBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import teleblock.manager.DeletedMessageManager;
import teleblock.ui.adapter.DeleteMessagePageAdapter;
import teleblock.util.MMKVUtil;
import teleblock.widget.ScaleTransitionPagerTitleView;

/**
 * Created by LSD on 2022/1/12.
 * Desc
 */
public class DeleteMsgGroupActivity extends BaseFragment {
    ActivityDeleteMsgGroupBinding binding;
    private DeleteMessagePageAdapter deleteMessagePageAdapter;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        initStyle();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_delete_msg_group_title", R.string.ac_delete_msg_group_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActivityDeleteMsgGroupBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.checkText.setText(LocaleController.getString("delete_msg_by_time", R.string.delete_msg_by_time));
        binding.tvDeleteBtn.setText(LocaleController.getString("delete_msg_group_all", R.string.delete_msg_group_all));
        //先不适配主题了
        //Switch aSwitch = new Switch(getContext());
        //aSwitch.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        //binding.checkFrame.addView(aSwitch, LayoutHelper.createFrame(37, 20));
        //binding.checkFrame.setOnClickListener(view -> {
        //    aSwitch.setChecked(!aSwitch.isChecked(), true);
        //    MMKVUtil.deleteMessageSwitch(aSwitch.isChecked());
        //});
        Switch mSwitch = new Switch(getContext());
        mSwitch.setChecked(MMKVUtil.deleteMessageSwitch());
        mSwitch.setOnCheckedChangeListener((compoundButton, b) -> MMKVUtil.deleteMessageSwitch(b));
        binding.checkFrame.addView(mSwitch, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        binding.tvDeleteBtn.setOnClickListener(view -> {
            deleteAllData();
        });

        String[] tabs = LocaleController.getString("array_delete_msg_tabs", R.string.array_delete_msg_tabs).split("\\|");
        //Navigator
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdjustMode(true);//等分
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(Color.parseColor("#90000000"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#000000"));
                simplePagerTitleView.setText(tabs[index]);
                simplePagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                simplePagerTitleView.setOnClickListener(v -> binding.viewPager2.setCurrentItem(index));
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setLineHeight(ConvertUtils.dp2px(4));
                linePagerIndicator.setColors(Color.parseColor("#40000000"));
                return linePagerIndicator;
            }
        });
        binding.magicIndicator.setNavigator(commonNavigator);
        binding.magicIndicator.setBackgroundColor(Color.WHITE);//先不适配主题了
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
        //viewpager2.setUserInputEnabled(false);//禁止左右滑动
        binding.viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.viewPager2.setAdapter(deleteMessagePageAdapter = new DeleteMessagePageAdapter(getParentActivity(), this, tabs));
        binding.viewPager2.setOffscreenPageLimit(tabs.length);//预加载
    }

    private void initStyle() {//临时先不适配主题了
        actionBar.setBackgroundColor(Color.parseColor("#ffffff"));
        actionBar.setTitleColor(Color.parseColor("#000000"));
        actionBar.getBackButton().setColorFilter(Color.BLACK);
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(),true),200);
    }

    private void deleteAllData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(LocaleController.getString("delete_msg_dialog_title", R.string.delete_msg_dialog_title));
        builder.setMessage(LocaleController.getString("delete_msg_dialog_group_content", R.string.delete_msg_dialog_group_content));
        builder.setPositiveButton(LocaleController.getString("delete_msg_dialog_confirm", R.string.delete_msg_dialog_confirm), (dialogInterface, i) -> {
            final AlertDialog progressDialog = new AlertDialog(getContext(), 3);
            progressDialog.show();
            DeletedMessageManager.getInstance().deleteAllDeletedMessage(() -> {
                AndroidUtilities.runOnUIThread(() -> {
                    progressDialog.dismiss();
                    finishFragment();
                });
            });
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        showDialog(dialog);
        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
        }
    }
}
