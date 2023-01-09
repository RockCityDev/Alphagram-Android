package teleblock.ui.adapter;

import android.view.View;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewTgcleanListItemBinding;

import java.text.SimpleDateFormat;
import java.util.List;

import teleblock.model.TGCacheEntity;
import teleblock.util.SystemUtil;

/***
 * 列表
 */
public class TGCleanListRvAdapter extends TGCleanRvAdapter {
    private int type;
    private boolean delete;

    public TGCleanListRvAdapter(List<TGCacheEntity> list, int type) {
        super(R.layout.view_tgclean_list_item, list);
        this.type = type;
        setList(list);
    }

    @Override
    public void deleteModel(boolean delete) {
        this.delete = delete;
        notifyDataSetChanged();
    }

    @Override
    public boolean isDeleteModel() {
        return delete;
    }

    @Override
    public void deleteItem(String path) {
        int position = -1;
        for (int i = 0; i < getData().size(); i++) {
            TGCacheEntity entity = getItem(i);
            if (entity.path.equals(path)) {
                position = i;
                break;
            }
        }
        if (position > -1) {
            getData().remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TGCacheEntity entity) {
        ViewTgcleanListItemBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ViewTgcleanListItemBinding::bind);

        if (type == 1 || type == 2) {//图片 视频
            Glide.with(getContext()).load(entity.path).into(binding.ivCover);
        } else if (type == 3) {//音频
            Glide.with(getContext()).load(R.drawable.ic_type_audio).into(binding.ivCover);
        } else if (type == 4) {//文件
            Glide.with(getContext()).load(R.drawable.ic_type_file).into(binding.ivCover);
        } else if (type == 5) {//其他
            Glide.with(getContext()).load(R.drawable.ic_type_doc).into(binding.ivCover);
        }

        if (delete) {
            binding.ivCheck.setVisibility(View.VISIBLE);
        } else {
            binding.ivCheck.setVisibility(View.GONE);
        }
        if (entity.checked) {
            binding.ivCheck.setImageResource(R.drawable.ic_check_yes);
        } else {
            binding.ivCheck.setImageResource(R.drawable.ic_check_no);
        }

        binding.tvTitleText.setText(entity.name);
        binding.tvSize.setText(SystemUtil.getSizeFormat(entity.size));

        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat2", R.string.dateformat2));
        binding.tvTime.setText(formatter.format(entity.time));
    }
}
