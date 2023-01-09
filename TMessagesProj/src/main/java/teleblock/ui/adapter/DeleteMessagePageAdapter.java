package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;
import java.util.Map;

import teleblock.ui.fragment.DeleteMessageFragment;

public class DeleteMessagePageAdapter extends FragmentStateAdapter {
    Map<String, DeleteMessageFragment> placeHolder = new HashMap<>();
    String[] tabs;
    BaseFragment baseFragment;

    public DeleteMessagePageAdapter(@NonNull FragmentActivity fragmentActivity, BaseFragment baseFragment, String[] tabs) {
        super(fragmentActivity);
        this.tabs = tabs;
        this.baseFragment = baseFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String fragmentTag = ":" + position;
        if (placeHolder.get(fragmentTag) != null) {
            return placeHolder.get(fragmentTag);
        } else {
            DeleteMessageFragment fragment = DeleteMessageFragment.instance(baseFragment,position);
            placeHolder.put(fragmentTag, fragment);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return null == tabs ? 0 : tabs.length;
    }

    public void loadData(int position) {
        String fragmentTag = ":" + position;
        if (placeHolder.get(fragmentTag) != null) {
            placeHolder.get(fragmentTag).loadData();
        }
    }
}
