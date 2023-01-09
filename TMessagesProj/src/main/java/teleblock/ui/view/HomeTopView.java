package teleblock.ui.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewHomeTopBinding;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.DialogsActivity;

import java.util.HashMap;

import teleblock.util.EventUtil;

/**
 * 创建日期：2022/4/19
 * 描述：
 */
public class HomeTopView extends FrameLayout implements View.OnClickListener {

    private DialogsActivity fragment;
    public ViewHomeTopBinding binding;

    public HomeTopView(@NonNull DialogsActivity fragment) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        initView();
        initData();
        updateStyle();
        setOnClickListener(v -> {
        });
    }

    private void initView() {
        binding = ViewHomeTopBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.llHomeSearch.setOnClickListener(this);
    }

    public void initData() {
        binding.tvHomeSearch.setText("Search for messages or users");
    }

    public void updateStyle(){
        binding.llHomeSearch.getHelper().setBackgroundColorNormal(Theme.getColor(Theme.key_windowBackgroundGray));
        binding.ivHomeTopSearch.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        binding.tvHomeSearch.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_home_search:
                EventUtil.track(getContext(), EventUtil.Even.搜索点击, new HashMap<>());
                fragment.searchItem.performClick();
                break;
        }
    }

}