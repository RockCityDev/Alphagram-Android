package teleblock.ui.adapter;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import java.util.List;

import teleblock.ui.fragment.BaseFragment;

/**
 * Time:2022/7/12
 * Author:Perry
 * Description：TG Fragment专用适配器
 */
public class TgFragmentVp2Adapter extends FragmentStateAdapter {

    private List<BaseFragment> fragments;

    public TgFragmentVp2Adapter(@NonNull FragmentActivity fragmentActivity, List<BaseFragment> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
