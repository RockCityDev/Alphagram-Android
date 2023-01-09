package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.ui.fragment.BaseFragment;
import teleblock.ui.fragment.ThemeFragment;


public class ThemePageAdapter extends FragmentStateAdapter {
    private List<String> list;
    private Map<String, BaseFragment> placeHolder = new HashMap<>();

    public ThemePageAdapter(@NonNull FragmentActivity fragmentActivity, List<String> list) {
        super(fragmentActivity);
        this.list = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (placeHolder.get("" + position) != null) {
            return placeHolder.get(position + "");
        } else {
            BaseFragment fragment = ThemeFragment.instance(position);
            placeHolder.put(position + "", fragment);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }
}
