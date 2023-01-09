package teleblock.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewBottomTabsBinding;
import org.telegram.ui.ActionBar.Theme;

import teleblock.util.ColorUtil;


/**
 * 创建日期：2022/4/19
 * 描述：首页底部导航栏
 */
public class BottomTabsView extends LinearLayout {

    private ViewBottomTabsBinding binding;
    public TabsViewDelegate delegate;

    public BottomTabsView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        binding = ViewBottomTabsBinding.inflate(LayoutInflater.from(getContext()), this, true);
        LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_tabs, this);

        for (int i = 0; i < binding.llTabs.getChildCount(); i++) {
            int finalI = i;
            RelativeLayout relativeLayout = (RelativeLayout) binding.llTabs.getChildAt(i);
            relativeLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (int i = 0; i < binding.llTabs.getChildCount(); i++) {
                        RelativeLayout relativeLayout = (RelativeLayout) binding.llTabs.getChildAt(i);
                        ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
                        ColorUtil.setBottomTabState(imageView, false);
                    }
                    ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
                    ColorUtil.setBottomTabState(imageView, true);
                    if (delegate != null) {
                        delegate.onTabClick(finalI);
                    }
                }
            });
        }

        updateStyle(0);
    }

    public void updateStyle(int tabIndex) {
        setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        binding.dir.setBackgroundColor(Theme.getColor(Theme.key_divider));
        for (int i = 0; i < binding.llTabs.getChildCount(); i++) {
            RelativeLayout relativeLayout = (RelativeLayout) binding.llTabs.getChildAt(i);
            ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
            ColorUtil.setBottomTabState(imageView, false);
        }
        RelativeLayout relativeLayout = (RelativeLayout) binding.llTabs.getChildAt(tabIndex);
        ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
        ColorUtil.setBottomTabState(imageView, true);
    }

    public void setDelegate(TabsViewDelegate tabsViewDelegate) {
        delegate = tabsViewDelegate;
    }

    public interface TabsViewDelegate {

        void onTabClick(int index);
    }
}