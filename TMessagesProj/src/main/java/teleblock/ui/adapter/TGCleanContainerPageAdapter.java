package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

import teleblock.model.TGCacheFindEntity;
import teleblock.ui.fragment.BaseFragment;
import teleblock.ui.fragment.CacheCleanDetailFragment;


public class TGCleanContainerPageAdapter extends FragmentStateAdapter {
    String[] tabs;
    Map<Integer, TGCacheFindEntity> dataMap;
    Map<String, BaseFragment> placeHolder = new HashMap<>();

    public TGCleanContainerPageAdapter(@NonNull FragmentActivity fragmentActivity, String[] tabs, Map<Integer, TGCacheFindEntity> dataMap) {
        super(fragmentActivity);
        this.tabs = tabs;
        this.dataMap = dataMap;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String fragmentTag = tabs[position];
        if (placeHolder.get(fragmentTag) != null) {
            return placeHolder.get(fragmentTag);
        } else {
            BaseFragment fragment = CacheCleanDetailFragment.instance(dataMap.get(position + 1), position + 1);
            placeHolder.put(fragmentTag, fragment);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return null == tabs ? 0 : tabs.length;
    }

    public void notifyEditChange(int position,boolean isEdit) {
        String fragmentTag = tabs[position];
        CacheCleanDetailFragment tgCleanDetailFragment = (CacheCleanDetailFragment) placeHolder.get(fragmentTag);
        if (tgCleanDetailFragment != null) {
            tgCleanDetailFragment.notifyEditChange(isEdit);
        }
    }
}
