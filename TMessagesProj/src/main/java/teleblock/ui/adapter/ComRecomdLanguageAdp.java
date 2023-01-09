package teleblock.ui.adapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;
import com.ruffian.library.widget.helper.RTextViewHelper;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.AdpComrecomdLanguageBinding;

import teleblock.model.SystemEntity;

/**
 * Time:2022/8/11
 * Author:Perry
 * Description：社群推荐语言选择页面
 */
public class ComRecomdLanguageAdp extends BaseQuickAdapter<SystemEntity.Language, BaseViewHolder> {

    public ComRecomdLanguageAdp() {
        super(R.layout.adp_comrecomd_language);
    }

    private int oldClickPostion = 0;

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, SystemEntity.Language language) {
        AdpComrecomdLanguageBinding binding = BaseViewHolderUtilKt.getBinding(viewHolder, AdpComrecomdLanguageBinding::bind);
        binding.tvName.setText(language.key);

        RTextViewHelper helper = binding.tvName.getHelper();
        if (language.selector) {
            helper.setIconNormalLeft(ContextCompat.getDrawable(getContext(), R.drawable.btn_radio_selected));
        } else {
            helper.setIconNormalLeft(ContextCompat.getDrawable(getContext(), R.drawable.btn_radio_disselected));
        }
    }

    public void itemClick(int position) {
        if (oldClickPostion != position) {
            getData().get(oldClickPostion).selector = false;
            getData().get(position).selector = true;
            notifyDataSetChanged();
            oldClickPostion = position;
        }
    }
}
