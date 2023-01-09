package teleblock.ui.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import teleblock.model.TGCacheEntity;


public abstract class TGCleanRvAdapter extends BaseQuickAdapter<TGCacheEntity, BaseViewHolder> {

    public TGCleanRvAdapter(int layoutResId, @Nullable List<TGCacheEntity> data) {
        super(layoutResId, data);
    }

    public boolean isDeleteModel() {
        return false;
    }

    public void deleteModel(boolean delete) {
    }

    public void deleteItem(String path) {
    }

}
