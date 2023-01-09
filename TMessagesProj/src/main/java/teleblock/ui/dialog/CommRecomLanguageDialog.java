package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCommrecomdLanguageBinding;

import java.util.List;

import teleblock.model.SystemEntity;
import teleblock.ui.adapter.ComRecomdLanguageAdp;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/8/11
 * Author:Perry
 * Description：社群推荐
 */
public class CommRecomLanguageDialog extends Dialog {

    private DialogCommrecomdLanguageBinding binding;
    private List<SystemEntity.Language> language;

    private ComRecomdLanguageAdp mComRecomdLanguageAdp;

    public CommRecomLanguageDialog(@NonNull Context context, CommRecomLanguageDialogListener listener) {
        super(context, R.style.dialog2);
        setCanceledOnTouchOutside(true);

        //获取数据源
        language = MMKVUtil.getSystemMsg().language;
        if (language != null && language.size() > 0) {
            language.get(0).selector = true;
        }

        binding = DialogCommrecomdLanguageBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        //设置card高度
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.card.getLayoutParams();
        if (language != null && language.size() > 0) {
            params.height = SizeUtils.dp2px(language.size() * 56f);
        }
        binding.card.setLayoutParams(params);

        binding.rvLanguage.setLayoutManager(new LinearLayoutManager(context));
        mComRecomdLanguageAdp = new ComRecomdLanguageAdp();
        binding.rvLanguage.setAdapter(mComRecomdLanguageAdp);
        if (language != null && language.size() > 0) {
            mComRecomdLanguageAdp.setList(language);
        }

        mComRecomdLanguageAdp.setOnItemClickListener((adapter, view, position) -> {
            if (mComRecomdLanguageAdp.getData().get(position).selector) {
                return;
            }
            mComRecomdLanguageAdp.itemClick(position);
            dismiss();
            listener.clickLanguage(
                    mComRecomdLanguageAdp.getData().get(position).value,
                    mComRecomdLanguageAdp.getData().get(position).key
            );
        });
    }

    public interface CommRecomLanguageDialogListener {
        void clickLanguage(int id, String name);
    }
}
