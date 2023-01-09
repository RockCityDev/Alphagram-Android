package teleblock.ui.adapter;

import android.view.View;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewThemeItemBinding;

import teleblock.model.ThemeEntity;

/**
 * 主题adapter
 */
public class ThemeRvAdapter extends BaseQuickAdapter<ThemeEntity.ItemEntity, BaseViewHolder> implements LoadMoreModule {

    public ThemeRvAdapter() {
        super(R.layout.view_theme_item);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ThemeEntity.ItemEntity entity) {
        ViewThemeItemBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ViewThemeItemBinding::bind);
        binding.tvTitle.setText(entity.title);
        binding.viewHolder.setVisibility(baseViewHolder.getAdapterPosition() == 0 || baseViewHolder.getAdapterPosition() == 1 ? View.VISIBLE : View.GONE);
        if (entity.getType() == 1) {
            Glide.with(getContext()).load(entity.avatarId).into(binding.ivThemePreview);
        } else {
            Glide.with(getContext()).load(entity.avatar).into(binding.ivThemePreview);
        }
        binding.tvCount.setText(entity.used + "" + LocaleController.getString("view_theme_used_count", R.string.view_theme_used_count));
    }
}
