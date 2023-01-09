package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;
import java.util.Map;

import teleblock.model.GifStickerEntity;
import teleblock.ui.fragment.DeleteMessageFragment;
import teleblock.ui.fragment.StickerGifFragment;

public class StickerGfiPage2Adapter extends FragmentStateAdapter {
    Map<String, StickerGifFragment> placeHolder = new HashMap<>();
    String[] tabs;

    public StickerGfiPage2Adapter(@NonNull FragmentActivity fragmentActivity, String[] tabs) {
        super(fragmentActivity);
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String fragmentTag = ":" + position;
        if (placeHolder.get(fragmentTag) != null) {
            return placeHolder.get(fragmentTag);
        } else {
            int type = -1;
            if (position == 0) {
                type = GifStickerEntity.TYPE_GIF;
            } else if (position == 1) {
                type = GifStickerEntity.TYPE_STICKER;
            } else if (position == 2) {
                type = 99;
            }
            StickerGifFragment fragment = StickerGifFragment.instance(type);
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
