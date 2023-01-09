package teleblock.ui.adapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewTgcleanGridItemBinding;

import java.util.List;

import teleblock.model.TGCacheEntity;


/***
 * 列表
 */
public class TGCleanGridRvAdapter extends TGCleanRvAdapter {
    private int type;
    private boolean delete;

    public TGCleanGridRvAdapter(List<TGCacheEntity> list, int type) {
        super(R.layout.view_tgclean_grid_item, list);
        this.type = type;
        setList(list);
    }

    @Override
    public void deleteModel(boolean delete) {
        this.delete = delete;
        this.notifyDataSetChanged();
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
        ViewTgcleanGridItemBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ViewTgcleanGridItemBinding::bind);

        binding.viewBackHolder.setVisibility(View.GONE);
        binding.ivPlay.setVisibility(View.GONE);
        if (type == 1 || type == 2) {//图片 视频
            binding.ivCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext()).load(entity.path).into(binding.ivCover);
            if (2 == type) {
                binding.ivPlay.setVisibility(View.VISIBLE);
            }
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.imageContainer.getLayoutParams();
            params.width = AndroidUtilities.dp(69);
            params.height = AndroidUtilities.dp(90);
            binding.imageContainer.setLayoutParams(params);
            binding.ivCover.setScaleType(ImageView.ScaleType.FIT_CENTER);

            FrameLayout.LayoutParams ivParams = (FrameLayout.LayoutParams) binding.ivCheck.getLayoutParams();
            ivParams.bottomMargin = AndroidUtilities.dp(15);
            ivParams.rightMargin = AndroidUtilities.dp(12);

            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) binding.tvTitleText.getLayoutParams();
            textParams.topMargin = AndroidUtilities.dp(-5);

            if (type == 3) {//音频
                Glide.with(getContext()).load(R.drawable.ic_type_audio).into(binding.ivCover);
            } else if (type == 4) {//文件
                Glide.with(getContext()).load(R.drawable.ic_type_file).into(binding.ivCover);
            } else if (type == 5) {//其他
                Glide.with(getContext()).load(R.drawable.ic_type_doc).into(binding.ivCover);
            }
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
    }
}
