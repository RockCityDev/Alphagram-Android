package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.ui.fragment.BaseFragment;
import teleblock.ui.fragment.MyCollectFragment;
import teleblock.ui.fragment.MyDownloadContainerFragment;
import teleblock.ui.fragment.MyLocalContainerFragment;


public class MyMixPageAdapter extends FragmentStateAdapter {
    List<String> list;
    int downloadPageTab;
    public Map<String, BaseFragment> placeHolder = new HashMap<>();

    public MyMixPageAdapter(@NonNull FragmentActivity fragmentActivity, List<String> list, int downloadPageTab) {
        super(fragmentActivity);
        this.list = list;
        this.downloadPageTab = downloadPageTab;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (placeHolder.get("" + position) != null) {
            return placeHolder.get(position + "");
        } else {
            BaseFragment fragment = null;
            if (position == 0) {
                fragment = MyCollectFragment.instance();
            } else if (position == 1) {
                fragment = MyDownloadContainerFragment.instance(downloadPageTab);
            } else if (position == 2) {
                fragment = MyLocalContainerFragment.instance();
            }
            placeHolder.put(position + "", fragment);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }
}
