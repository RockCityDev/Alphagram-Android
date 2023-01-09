package teleblock.ui.adapter;


import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.blankj.utilcode.util.ResourceUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdapterToolstabItemBinding;

import java.util.List;

import teleblock.model.ui.ToolsTabData;

/**
 * Time:2022/7/4
 * Author:Perry
 * Description：工具类tab栏数据
 */
public class ToolsTabAdapter extends BaseQuickAdapter<ToolsTabData, BaseViewHolder> {

    public ToolsTabAdapter(@Nullable List<ToolsTabData> data) {
        super(R.layout.adapter_toolstab_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, ToolsTabData toolsTabData) {
        AdapterToolstabItemBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdapterToolstabItemBinding::bind);
        if (toolsTabData.getColors() != null) {
            binding.bBg.getHelper()
                    .setGradientOrientation(GradientDrawable.Orientation.LEFT_RIGHT)
                    .setBackgroundColorNormalArray(toolsTabData.getColors());
        } else if (toolsTabData.getColor() != 0) {
            binding.bBg.getHelper().setBackgroundColorNormal(toolsTabData.getColor());
        } else if (toolsTabData.getBackground() != 0) {
            binding.bBg.getHelper().setBackgroundDrawableNormal(ResourceUtils.getDrawable(toolsTabData.getBackground()));
        }
        binding.ivIcon.setImageResource(toolsTabData.getIcon());
        binding.tvName.setText(toolsTabData.getName());
    }
}
